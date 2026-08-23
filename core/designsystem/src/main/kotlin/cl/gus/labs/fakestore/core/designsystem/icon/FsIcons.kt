package cl.gus.labs.fakestore.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import cl.gus.labs.fakestore.core.designsystem.R

object FsIcons {

    val Alert: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_alert)

    val ArrowBack: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_arrow_back)

    val Box: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_box)

    val HeartFilled: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_heart_filled)

    val HeartOutline: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_heart_outline)

    val ImageMissing: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_image_missing)

    val Moon: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_moon)

    val Share: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_share)

    val Star: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_star)

    val WifiOff: Painter
        @Composable get() = painterResource(R.drawable.ic_fs_wifi_off)
}
