package cl.gus.labs.fakestore.analytics.guardrails

import java.io.File
import java.util.Properties

/**
 * Reads the project's Kotlin sources and reports the analytics events it finds.
 *
 * This is a source scanner, not a compiler. It sanitises comments and string literals and then
 * matches declarations, which is enough for the rules in [AnalyticsGuardrailsTest] and needs no
 * dependency at all. It is not a full parser: heavily unusual formatting can slip past it.
 *
 * If the project already uses Konsist or ArchUnit, prefer those — they work on a real AST or on
 * bytecode. See `references/guardrails.md`. This backend exists so a project with no
 * architecture tooling can still have enforced rules without taking on a new dependency.
 */
object AnalyticsSource {

    data class Conventions(
        val markerFqn: String,
        val containerSuffix: String,
        val containerPackage: String,
        val suffixEnforced: Boolean,
        val suffixReserved: Boolean,
        val forbiddenParameterTypes: List<String>,
        val scanExclude: List<String>,
    ) {
        val markerSimpleName: String get() = markerFqn.substringAfterLast('.')
    }

    /** A nested event declaration inside a container. */
    data class EventDeclaration(
        val name: String,
        val header: String,
        val body: String,
        val rawBody: String,
    )

    /** A sealed container implementing the marker interface. */
    data class EventContainer(
        val name: String,
        /** The declaration line, sanitised — never scan rawBody for code, it contains comments. */
        val header: String,
        val packageName: String,
        val file: File,
        val body: String,
        val rawBody: String,
        val events: List<EventDeclaration>,
    )

    val repoRoot: File by lazy {
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        while (dir != null) {
            val hasSettings = File(dir, "settings.gradle").exists() ||
                File(dir, "settings.gradle.kts").exists()
            if (hasSettings) return@lazy dir
            dir = dir.parentFile
        }
        error("Could not locate the repository root: no settings.gradle(.kts) above ${System.getProperty("user.dir")}")
    }

    val conventions: Conventions by lazy {
        val file = File(repoRoot, "analytics-conventions.properties")
        check(file.exists()) {
            "Missing ${file.path}. The guardrails read their configuration from it."
        }
        val props = Properties().apply { file.inputStream().use { load(it) } }
        fun str(key: String, default: String = "") = props.getProperty(key, default).trim()
        fun list(key: String) = str(key).split(',').map(String::trim).filter(String::isNotEmpty)
        Conventions(
            markerFqn = str("marker").ifEmpty { error("analytics-conventions.properties: 'marker' is required") },
            containerSuffix = str("container.suffix", "AnalyticsEvents"),
            containerPackage = str("container.package"),
            suffixEnforced = str("container.suffix.enforced", "true").toBoolean(),
            suffixReserved = str("container.suffix.reserved", "false").toBoolean(),
            forbiddenParameterTypes = list("forbidden.parameter.types"),
            scanExclude = list("scan.exclude").ifEmpty { listOf("build", ".git", ".gradle") },
        )
    }

    val productionFiles: List<File> by lazy { kotlinFiles("main") }

    val testFiles: List<File> by lazy { kotlinFiles("test") }

    /** Test class simple name -> file text, for the "every event has a test" rule. */
    val testSources: Map<String, String> by lazy {
        buildMap {
            testFiles.forEach { file ->
                val text = file.readText()
                Regex("""\bclass\s+(\w+)""").findAll(sanitize(text)).forEach { match ->
                    put(match.groupValues[1], text)
                }
            }
        }
    }

    val containers: List<EventContainer> by lazy {
        productionFiles.flatMap { file -> containersIn(file) }
    }

    /** Every declared class/interface/object name, for the reserved-suffix rule. */
    val allDeclarations: List<Pair<String, File>> by lazy {
        productionFiles.flatMap { file ->
            val sanitized = sanitize(file.readText())
            Regex("""\b(?:class|interface|object)\s+(\w+)""")
                .findAll(sanitized)
                .map { it.groupValues[1] to file }
                .toList()
        }
    }

    // --- scanning -------------------------------------------------------------------------

    private fun kotlinFiles(sourceSet: String): List<File> =
        repoRoot.walkTopDown()
            .onEnter { dir -> dir.name !in conventions.scanExclude }
            .filter { it.isFile && it.extension == "kt" }
            .filter { file ->
                val path = file.invariantSeparatorsPath
                path.contains("/src/$sourceSet/kotlin/") || path.contains("/src/$sourceSet/java/")
            }
            .toList()

    private fun containersIn(file: File): List<EventContainer> {
        val raw = file.readText()
        val text = sanitize(raw)
        if (!text.contains(conventions.markerSimpleName)) return emptyList()

        val packageName = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
            .find(text)?.groupValues?.get(1).orEmpty()

        // A container is a sealed class/interface whose supertype list names the marker.
        val header = Regex(
            """\bsealed\s+(?:class|interface)\s+(\w+)\s*(?:\([^)]*\))?\s*:\s*([^{]*)\{""",
        )
        return header.findAll(text).mapNotNull { match ->
            if (conventions.markerSimpleName !in match.groupValues[2]) return@mapNotNull null
            val name = match.groupValues[1]
            val open = text.indexOf('{', match.range.first)
            val range = blockRange(text, open) ?: return@mapNotNull null
            val body = text.substring(range.first, range.last)
            val rawBody = raw.substring(range.first, range.last)
            EventContainer(
                name = name,
                header = match.value,
                packageName = packageName,
                file = file,
                body = body,
                rawBody = rawBody,
                events = eventsIn(body, rawBody, name),
            )
        }.toList()
    }

    /**
     * An event is a nested declaration whose supertype list names the container. Helper objects
     * such as `EventName` and `EventProperty` therefore fall out naturally, with no name list to
     * maintain.
     */
    private fun eventsIn(body: String, rawBody: String, container: String): List<EventDeclaration> {
        val header = Regex(
            """\b(?:data\s+)?(?:class|object)\s+(\w+)\s*(\([^)]*\))?\s*:\s*([^{]*?)\{""",
        )
        return header.findAll(body).mapNotNull { match ->
            if (!Regex("""\b$container\b""").containsMatchIn(match.groupValues[3])) return@mapNotNull null
            val open = body.indexOf('{', match.range.first)
            val range = blockRange(body, open) ?: return@mapNotNull null
            EventDeclaration(
                name = match.groupValues[1],
                header = match.value,
                body = body.substring(range.first, range.last),
                rawBody = rawBody.substring(range.first, range.last),
            )
        }.toList()
    }

    // --- text utilities -------------------------------------------------------------------

    /** Index range between a `{` and its match, or null when unbalanced. */
    private fun blockRange(text: String, openIndex: Int): IntRange? {
        if (openIndex < 0 || openIndex >= text.length) return null
        var depth = 0
        for (i in openIndex until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return (openIndex + 1)..i
            }
        }
        return null
    }

    /**
     * Blanks out comments and the contents of string literals, keeping the text the same length
     * so indices stay valid against the original. Without this, a brace or the word `class`
     * inside a string would derail the scan.
     */
    internal fun sanitize(text: String): String {
        val out = text.toCharArray()
        var i = 0
        while (i < out.size) {
            when {
                out[i] == '/' && i + 1 < out.size && out[i + 1] == '/' -> {
                    while (i < out.size && out[i] != '\n') { out[i] = ' '; i++ }
                }
                out[i] == '/' && i + 1 < out.size && out[i + 1] == '*' -> {
                    while (i < out.size && !(out[i] == '*' && i + 1 < out.size && out[i + 1] == '/')) {
                        if (out[i] != '\n') out[i] = ' '
                        i++
                    }
                    if (i + 1 < out.size) { out[i] = ' '; out[i + 1] = ' '; i += 2 }
                }
                out[i] == '"' -> {
                    val triple = i + 2 < out.size && out[i + 1] == '"' && out[i + 2] == '"'
                    val quote = if (triple) 3 else 1
                    i += quote
                    while (i < out.size) {
                        if (!triple && out[i] == '\\') { out[i] = ' '; if (i + 1 < out.size) out[i + 1] = ' '; i += 2; continue }
                        val closing = if (triple) {
                            i + 2 < out.size && out[i] == '"' && out[i + 1] == '"' && out[i + 2] == '"'
                        } else {
                            out[i] == '"'
                        }
                        if (closing) { i += quote; break }
                        if (out[i] != '\n') out[i] = ' '
                        i++
                    }
                }
                else -> i++
            }
        }
        return String(out)
    }
}
