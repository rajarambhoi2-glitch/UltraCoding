package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardTab
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar

@Composable
fun ExtraKeyboardRow(
    tabSize: Int,
    onInsertText: (String, Int) -> Unit, // text, cursorOffset
    onMoveCursor: (Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onQuickFixClick: () -> Unit,
    onAiWriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val tabSpaces = " ".repeat(tabSize)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(VsCodeSidebar)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab key
        KeyButton(
            text = "Tab",
            testTag = "key_tab",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onInsertText(tabSpaces, tabSpaces.length)
            }
        )

        // Arrow keys
        IconButtonKey(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Left",
            testTag = "key_cursor_left",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMoveCursor(-1)
            }
        )

        IconButtonKey(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Right",
            testTag = "key_cursor_right",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMoveCursor(1)
            }
        )

        // Undo / Redo
        IconButtonKey(
            icon = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            testTag = "key_undo",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onUndo()
            }
        )

        IconButtonKey(
            icon = Icons.AutoMirrored.Filled.Redo,
            contentDescription = "Redo",
            testTag = "key_redo",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onRedo()
            }
        )

        Spacer(modifier = Modifier.width(4.dp))

        // AI Quick Actions
        ActionKey(
            icon = Icons.Default.Bolt,
            label = "Fix",
            color = Color(0xFFFBBF24),
            testTag = "key_quick_fix",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onQuickFixClick()
            }
        )

        ActionKey(
            icon = Icons.Default.AutoAwesome,
            label = "AI",
            color = VsCodeBlueLight,
            testTag = "key_ai_write",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAiWriteClick()
            }
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Bracket pairs with smart cursor offset inside
        KeyButton(text = "{ }", testTag = "key_curly_brackets", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("{}", 1)
        })
        KeyButton(text = "( )", testTag = "key_parentheses", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("()", 1)
        })
        KeyButton(text = "[ ]", testTag = "key_brackets", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("[]", 1)
        })
        KeyButton(text = "\" \"", testTag = "key_double_quotes", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("\"\"", 1)
        })
        KeyButton(text = "' '", testTag = "key_single_quotes", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("''", 1)
        })
        KeyButton(text = "< >", testTag = "key_angle_brackets", onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onInsertText("<>", 1)
        })

        // Common coding symbols
        val symbols = listOf(";", ":", "=", "+", "-", "*", "/", "\\", "_", ",", ".", "!", "@", "#", "$", "%", "&", "|", "?", "`")
        symbols.forEach { sym ->
            KeyButton(
                text = sym,
                testTag = "key_$sym",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertText(sym, sym.length)
                }
            )
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2C2C2E))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun IconButtonKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2C2C2E))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFFD4D4D4),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ActionKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
