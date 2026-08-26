// :analytics:guardrails
//
// Test-only. Scans the repository's Kotlin sources and enforces the analytics rules.
// No production source set, and no dependency beyond JUnit: the point of this backend is that
// the project enforces its own rules without taking on Konsist or ArchUnit.
//
//   ./gradlew :analytics:guardrails:test
plugins {
    id("fakestore.jvm.library")
    id("fakestore.testing")
}

// The tests read Kotlin sources from the WHOLE repository, which are not inputs of this module.
// Without declaring them, Gradle marks the task UP-TO-DATE and the rules silently stop running
// on every build after the first — a guardrail that passes because it never ran.
tasks.withType<Test>().configureEach {
    inputs.files(
        fileTree(rootDir) {
            include("**/src/main/kotlin/**/*.kt", "**/src/main/java/**/*.kt")
            include("**/src/test/kotlin/**/*.kt", "**/src/test/java/**/*.kt")
            exclude("**/build/**")
        },
    ).withPropertyName("repositorySources").withPathSensitivity(PathSensitivity.RELATIVE)

    inputs.file(rootProject.file("analytics-conventions.properties"))
        .withPropertyName("analyticsConventions")
}
