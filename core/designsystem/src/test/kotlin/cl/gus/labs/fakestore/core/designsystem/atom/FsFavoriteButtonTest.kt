package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class FsFavoriteButtonTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `exposes the Checkbox role`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsFavoriteButton(checked = false, onCheckedChange = {}, contentDescription = "Añadir a favoritos")
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
    }

    @Test
    fun `exposes the off state`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsFavoriteButton(checked = false, onCheckedChange = {}, contentDescription = "Añadir a favoritos")
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertIsOff()
    }

    @Test
    fun `exposes the on state`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsFavoriteButton(checked = true, onCheckedChange = {}, contentDescription = "Quitar de favoritos")
            }
        }
        composeRule.onNodeWithContentDescription("Quitar de favoritos").assertIsOn()
    }

    @Test
    fun `click toggles the checked value`() {
        var checked: Boolean? = null
        composeRule.setContent {
            FakeStoreTheme {
                FsFavoriteButton(
                    checked = false,
                    onCheckedChange = { checked = it },
                    contentDescription = "Añadir a favoritos",
                )
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").performClick()
        assertTrue(checked == true)
    }

    @Test
    fun `saving disables it`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                FsFavoriteButton(
                    checked = false,
                    onCheckedChange = { clicked = true },
                    contentDescription = "Guardando favorito",
                    saving = true,
                )
            }
        }
        composeRule.onNodeWithContentDescription("Guardando favorito").assertIsNotEnabled()
        assertFalse(clicked)
    }
}
