package cl.gus.labs.fakestore.analytics.guardrails

import cl.gus.labs.fakestore.analytics.guardrails.AnalyticsSource.EventContainer
import cl.gus.labs.fakestore.analytics.guardrails.AnalyticsSource.EventDeclaration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * Architecture rules for analytics events, as one dynamic test per rule per event.
 *
 * One test per event rather than one test for all of them is deliberate: a failure names the
 * offending event in the report, so whoever broke it can fix it without reading this file.
 *
 * Rules are configured in `analytics-conventions.properties` at the repo root.
 */
@DisplayName("Analytics guardrails")
class AnalyticsGuardrailsTest {

    private val conventions = AnalyticsSource.conventions
    private val containers = AnalyticsSource.containers

    @TestFactory
    @DisplayName("event containers")
    fun containerRules(): List<DynamicNode> {
        if (containers.isEmpty()) {
            return listOf(
                DynamicTest.dynamicTest("at least one analytics event exists") {
                    assertTrue(false) {
                        "No event containers found. Expected sealed types implementing " +
                            "${conventions.markerSimpleName}. If analytics has not been added yet, " +
                            "delete this guardrail module rather than leaving it passing vacuously."
                    }
                },
            )
        }
        return containers.map { container ->
            DynamicContainer.dynamicContainer(
                container.name,
                buildList {
                    add(namingRule(container))
                    add(hasTestRule(container.name, container.name))
                    add(noForbiddenParameters(container.name, container.header, container))
                    add(nestedTypesAreEvents(container))
                    container.events.forEach { event ->
                        add(
                            DynamicContainer.dynamicContainer(
                                event.name,
                                listOf(
                                    hasTestRule(event.name, container.name),
                                    declaresAudience(container, event),
                                    propertyKeysAreConstants(container, event),
                                    noForbiddenParameters(event.name, event.header, container),
                                ),
                            ),
                        )
                    }
                },
            )
        }
    }

    @TestFactory
    @DisplayName("reserved suffix")
    fun reservedSuffixRule(): List<DynamicNode> {
        if (!conventions.suffixReserved) {
            return listOf(
                DynamicTest.dynamicTest("suffix reservation is disabled") { /* opted out */ },
            )
        }
        val eventNames = buildSet {
            containers.forEach { container ->
                add(container.name)
                container.events.forEach { add(it.name) }
            }
        }
        val suffix = conventions.containerSuffix
        return AnalyticsSource.allDeclarations
            .filter { (name, _) -> name.endsWith(suffix) || name.endsWith("${suffix}s") }
            .filter { (name, _) -> name !in eventNames }
            .map { (name, file) ->
                DynamicTest.dynamicTest("$name must be an analytics event or be renamed") {
                    assertTrue(false) {
                        "'$name' in ${file.relativeTo(AnalyticsSource.repoRoot)} ends with " +
                            "'$suffix', which is reserved for analytics events. Rename it, or " +
                            "set container.suffix.reserved=false if the reservation is not worth it."
                    }
                }
            }
            .ifEmpty { listOf(DynamicTest.dynamicTest("no class misuses the reserved suffix") {}) }
    }

    // --- rules ----------------------------------------------------------------------------

    /**
     * Catches the quiet failure: a nested type that was meant to be an event but forgot to
     * extend the container. It compiles, it looks right, and it escapes every other rule here
     * because nothing identifies it as an event.
     */
    private fun nestedTypesAreEvents(container: EventContainer) =
        DynamicTest.dynamicTest("${container.name} has no nested type that forgot to extend it") {
            val eventNames = container.events.map { it.name }.toSet()
            // `enum class` / `sealed class` helpers and plain `object` constant holders are
            // legitimate members of a container, so only real candidates are considered.
            val orphans = Regex("""\b(enum\s+|sealed\s+|annotation\s+)?(?:data\s+class|data\s+object|class)\s+(\w+)""")
                .findAll(container.body)
                .filter { it.groupValues[1].isBlank() }
                .map { it.groupValues[2] }
                .filter { it !in eventNames }
                .toList()

            assertTrue(orphans.isEmpty()) {
                "In ${container.file.name}, ${orphans.joinToString()} " +
                    "${if (orphans.size == 1) "is nested inside" else "are nested inside"} " +
                    "'${container.name}' without extending it, so ${if (orphans.size == 1) "it is" else "they are"} " +
                    "not treated as ${conventions.markerSimpleName}s and no rule applies to " +
                    "${if (orphans.size == 1) "it" else "them"}. Add ': ${container.name}()', or move " +
                    "${if (orphans.size == 1) "it" else "them"} out of the container."
            }
        }

    private fun namingRule(container: EventContainer) =
        DynamicTest.dynamicTest("${container.name} follows the naming convention") {
            val suffix = conventions.containerSuffix
            val nameOk = !conventions.suffixEnforced ||
                container.name.endsWith(suffix) || container.name.endsWith("${suffix}s")
            val packageOk = conventions.containerPackage.isEmpty() ||
                container.packageName.endsWith(conventions.containerPackage)

            assertTrue(nameOk) {
                "'${container.name}' should end with '$suffix' or '${suffix}s'. " +
                    "Change container.suffix in analytics-conventions.properties if the project " +
                    "has settled on a different word."
            }
            assertTrue(packageOk) {
                "'${container.name}' is in package '${container.packageName}', which should end " +
                    "with '${conventions.containerPackage}'."
            }
        }

    private fun hasTestRule(subject: String, containerName: String) =
        DynamicTest.dynamicTest("$subject has a unit test") {
            val candidates = listOf(
                "${containerName}Test", "${containerName}Tests",
                "${containerName}RobotTest", "${containerName}RobotTests",
            )
            val covered = candidates.any { candidate ->
                AnalyticsSource.testSources[candidate]?.contains(subject) == true
            }
            assertTrue(covered) {
                "No test covering '$subject'. Expected one of ${candidates.joinToString()} to " +
                    "mention it. An analytics event with no test is a contract nobody checked: " +
                    "renaming it or dropping a property would ship silently."
            }
        }

    private fun declaresAudience(container: EventContainer, event: EventDeclaration) =
        DynamicTest.dynamicTest("${event.name} declares an audience") {
            // The container fallback covers a container that declares `audiences` once for all
            // of its events. It must exclude the events' own bodies, or a single sibling that
            // declares one would satisfy the rule for every event in the file.
            val containerOwnBody = container.events.fold(container.body) { acc, e -> acc.replace(e.body, "") }
            val declared = event.body.contains("audiences") || containerOwnBody.contains("audiences")
            assertTrue(declared) {
                "'${event.name}' does not declare 'audiences'. Without one the dispatcher has " +
                    "nothing to route on and the event reaches no vendor at all."
            }
        }

    private fun propertyKeysAreConstants(container: EventContainer, event: EventDeclaration) =
        DynamicTest.dynamicTest("${event.name} builds properties from constants") {
            val inlineKey = Regex(""""[^"]*"\s+to\b""").find(event.rawBody)
            assertTrue(inlineKey == null) {
                "'${event.name}' in ${container.file.name} uses an inline string as a property " +
                    "key (${inlineKey?.value?.trim()}). Declare it in the container's " +
                    "EventProperty object instead — a typo in a literal is invisible until the " +
                    "data team notices a missing column."
            }
        }

    private fun noForbiddenParameters(subject: String, header: String, container: EventContainer) =
        DynamicTest.dynamicTest("$subject takes no Android types") {
            val params = header.substringAfter('(', "").substringBeforeLast(')', "")
            val offender = conventions.forbiddenParameterTypes.firstOrNull { type ->
                Regex("""[:\s<]$type\b""").containsMatchIn(params)
            }
            assertTrue(offender == null) {
                "'$subject' in ${container.file.name} accepts '$offender'. An event that needs a " +
                    "framework object to describe itself can only be created where that object " +
                    "exists — in practice the UI layer — and cannot be unit tested without static " +
                    "mocking. Pass the value it actually needs instead."
            }
        }
}
