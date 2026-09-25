package cl.gus.labs.fakestore.convention

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

/**
 * Root-only: keeps `docs/ui-gallery.md` in step with the screenshot baselines.
 *
 * `uiGallery` writes the page after recording screenshots. `uiGalleryCheck` runs with `check` and
 * fails when a preview was added, renamed or removed without regenerating it.
 */
class UiGalleryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("base")

        val baselines = files(
            subprojects.map { fileTree(it.projectDir.resolve("src/test/screenshots")) { include("**/*.png") } },
        )
        val page = layout.projectDirectory.file("docs/ui-gallery.md")

        tasks.register<UiGalleryTask>("uiGallery") {
            group = "documentation"
            description = "Writes docs/ui-gallery.md from the screenshot baselines."
            this.baselines.from(baselines)
            root.set(layout.projectDirectory)
            gallery.set(page)
        }
        val verify = tasks.register<UiGalleryCheckTask>("uiGalleryCheck") {
            group = "verification"
            description = "Fails if docs/ui-gallery.md does not match the screenshot baselines."
            this.baselines.from(baselines)
            root.set(layout.projectDirectory)
            gallery.set(page)
        }
        tasks.named("check").configure { dependsOn(verify) }
    }
}

abstract class UiGalleryTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselines: ConfigurableFileCollection

    @get:Internal
    abstract val root: DirectoryProperty

    @get:OutputFile
    abstract val gallery: RegularFileProperty

    @TaskAction
    fun write() = gallery.get().asFile.writeText(renderUiGallery(root.get().asFile, baselines.files))
}

abstract class UiGalleryCheckTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselines: ConfigurableFileCollection

    @get:Internal
    abstract val root: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val gallery: RegularFileProperty

    @TaskAction
    fun verify() {
        val expected = renderUiGallery(root.get().asFile, baselines.files)
        if (gallery.get().asFile.readText() != expected) {
            throw GradleException("docs/ui-gallery.md is stale: run ./gradlew uiGallery and commit it.")
        }
    }
}
