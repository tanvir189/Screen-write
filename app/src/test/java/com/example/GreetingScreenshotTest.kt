package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertTextEquals
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun screen_board_canvas_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ScreenBoardScreen()
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun test_typing_and_clearing() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ScreenBoardScreen()
      }
    }

    // Type text into the canvas
    composeTestRule.onNodeWithTag("canvas_text_input").performTextInput("Explaining Architecture")
    composeTestRule.onNodeWithTag("canvas_text_input").assertTextEquals("Explaining Architecture")

    // Click Delete button
    composeTestRule.onNodeWithTag("delete_button").performClick()

    // Assert cleared to blank state
    composeTestRule.onNodeWithTag("canvas_text_input").assertTextEquals("")
  }
}
