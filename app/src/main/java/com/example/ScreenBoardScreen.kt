package com.example

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ScreenBoardApp: A distraction-free, pure white canvas designed specifically for
 * screen recording, live presentations, and teaching/explaining concepts.
 */
@Composable
fun ScreenBoardScreen(
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Automatically focus the input and open the system keyboard when the app opens
    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Automatically scroll so the latest typed line remains visible as the user types
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Insets tracking for keyboard and system bars
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navBottom = WindowInsets.navigationBars.getBottom(density)
    val statusBarTop = WindowInsets.statusBars.getTop(density)

    val imeBottomDp = with(density) { imeBottom.toDp() }
    val navBottomDp = with(density) { navBottom.toDp() }
    val statusBarTopDp = with(density) { statusBarTop.toDp() }

    val isKeyboardOpen = imeBottom > 0

    // Dynamic vertical positioning for the Delete button:
    // When keyboard is OPEN: immediately above the keyboard with 16dp spacing
    // When keyboard is CLOSED: at bottom-right corner with 20dp margin above system navigation area
    val targetDeleteBottomDp = if (isKeyboardOpen) {
        imeBottomDp + 16.dp
    } else {
        navBottomDp + 20.dp
    }

    val animatedDeleteBottomDp by animateDpAsState(
        targetValue = targetDeleteBottomDp,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "delete_button_bottom_offset"
    )

    // Ensure scrollable content has enough bottom padding so the typed text
    // is never covered by the floating delete button or the keyboard
    val contentBottomPadding = if (isKeyboardOpen) {
        imeBottomDp + 84.dp
    } else {
        navBottomDp + 84.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("canvas_screen")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere on the blank canvas re-focuses and shows keyboard
                focusRequester.requestFocus()
                keyboardController?.show()
            }
    ) {
        // Main text canvas: distraction-free, large typography
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarTopDp + 24.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = contentBottomPadding
                )
                .verticalScroll(scrollState)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("canvas_text_input"),
                textStyle = TextStyle(
                    color = Color(0xFF111111),
                    fontSize = 28.sp,
                    lineHeight = 40.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                ),
                cursorBrush = SolidColor(Color(0xFF111111)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                decorationBox = { innerTextField ->
                    // Completely blank canvas: no borders, toolbars, or placeholders
                    innerTextField()
                }
            )
        }

        // Floating Delete Button: dynamically positioned above keyboard or at bottom-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    end = 20.dp,
                    bottom = animatedDeleteBottomDp
                ),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Instantly clear ALL typed text
                    textFieldValue = TextFieldValue(text = "", selection = TextRange(0))
                    // Keep keyboard open if it was already open
                    coroutineScope.launch {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        spotColor = Color(0x33000000),
                        ambientColor = Color(0x1F000000)
                    )
                    .testTag("delete_button"),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.clear_text),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
