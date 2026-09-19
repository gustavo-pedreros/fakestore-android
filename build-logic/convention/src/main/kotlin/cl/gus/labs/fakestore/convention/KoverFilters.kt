package cl.gus.labs.fakestore.convention

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter

/**
 * What coverage does not measure. Filters never cross module boundaries, so both Kover convention
 * plugins apply this list to make a module's own report and the merged report count the same classes.
 */
internal fun KoverReportFilter.excludeGeneratedAndWiring() {
    // Dependency injection wiring: cl.gus.labs.fakestore.di plus every
    // cl.gus.labs.fakestore.<feature>.<layer>.di, subpackages included.
    packages("cl.gus.labs.fakestore*.di")

    // The one package that holds nothing but Compose previews.
    packages("cl.gus.labs.fakestore.core.designsystem.preview")

    // The rest of the previews are private functions at the bottom of production
    // files, so only an annotation filter reaches them. @PreviewLightDark and
    // friends are separate annotation classes and Kover does not resolve
    // meta-annotations, hence the wildcard.
    annotatedBy("androidx.compose.ui.tooling.preview.Preview*")

    // Finishes the job of the annotation filter above. Excluding a declaration
    // does not exclude its nested or anonymous classes, and the Compose compiler
    // hoists the constant lambdas of a preview body into `ComposableSingletons$X`,
    // which carries no annotation for `annotatedBy` to match. Measured: 21 such
    // classes, 372 lines, none of them covered, and 20 of the 21 sit in a file
    // that has previews — so nothing of value is lost today.
    classes("*ComposableSingletons*")

    // *Activity, *Fragment, *.BuildConfig, *.databinding.*
    androidGeneratedClasses()

    // KSP output: Hilt/Dagger factories and Room implementations. Patterns match
    // the fully qualified name, so each needs a leading `*`. Each also needs a
    // *trailing* `*`: without it the pattern matched nothing here, and it is
    // needed anyway to catch the nested classes KSP emits alongside the outer one
    // (`FavoriteDao_Impl$1`, `ProductDao_Impl$Companion`, ...).
    classes(
        "*_Factory*",
        "*_Impl*",
        "*Hilt_*",
        "*HiltWrapper_*",
        "*_HiltModules*",
        "*_MembersInjector*",
        "*_GeneratedInjector*",
        "*Module_*Factory*",
        "dagger.hilt.internal.*",
        "hilt_aggregated_deps.*",
    )
}