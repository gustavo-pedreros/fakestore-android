package cl.gus.labs.fakestore.core.designsystem

import android.app.Application
import coil3.ColorImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.test.FakeImageLoaderEngine
import java.io.IOException
import kotlinx.coroutines.awaitCancellation

// Set as robolectric.properties' application=, so every design-system test resolves images through
// this fake engine and never reaches the network.
class DesignSystemTestApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val engine = FakeImageLoaderEngine.Builder()
            .intercept(TestImages.Loaded, ColorImage(0xFF4C6B4C.toInt()))
            .intercept({ it == TestImages.Broken }) { chain ->
                ErrorResult(
                    image = null,
                    request = chain.request,
                    throwable = IOException("broken test image"),
                )
            }
            .intercept({ it == TestImages.Pending }) { awaitCancellation() }
            .build()

        return ImageLoader.Builder(context)
            .components { add(engine) }
            .build()
    }
}
