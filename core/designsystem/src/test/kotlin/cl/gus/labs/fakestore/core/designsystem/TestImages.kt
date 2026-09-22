package cl.gus.labs.fakestore.core.designsystem

// Fake URLs the DesignSystemTestApplication's Coil engine recognizes. Anything else falls through
// to FakeImageLoaderEngine's own default, which errors with "No interceptors handled this request…".
internal object TestImages {
    const val Loaded = "https://images.test/loaded.jpg"
    const val Broken = "https://images.test/broken.jpg"
    const val Pending = "https://images.test/pending.jpg"
}
