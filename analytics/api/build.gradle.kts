// :analytics:api
//
// Pure Kotlin. This module must never gain an Android dependency, a DI dependency, or a
// vendor SDK. That constraint is what lets every feature module depend on it freely.
plugins {
    id("fakestore.jvm.library")
    id("fakestore.testing")
}
