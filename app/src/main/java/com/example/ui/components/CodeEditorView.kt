package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.ProblemItem
import com.example.data.model.ProblemSeverity
import com.example.editor.syntax.SyntaxHighlighter
import com.example.ui.theme.DiagnosticError
import com.example.ui.theme.DiagnosticWarning
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeGutter
import com.example.ui.theme.VsCodeLineNumber
import com.example.ui.theme.VsCodeSurface

class SyntaxVisualTransformation(private val language: Language) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = SyntaxHighlighter.highlight(text.text, language)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}

@Composable
fun CodeEditorView(
    codeValue: TextFieldValue,
    language: Language,
    problems: List<ProblemItem>,
    baseFontSize: Int,
    onCodeChange: (TextFieldValue) -> Unit,
    isFindBarVisible: Boolean,
    findText: String,
    replaceText: String,
    useRegex: Boolean,
    onFindTextChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onCloseFindBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scaleMultiplier by remember { mutableFloatStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scaleMultiplier = (scaleMultiplier * zoomChange).coerceIn(0.75f, 2.0f)
    }

    val currentFontSize = (baseFontSize * scaleMultiplier).coerceIn(10f, 28f).sp
    val currentLineHeight = (baseFontSize * scaleMultiplier * 1.45f).coerceIn(14f, 40f).sp

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(codeValue.text) {
        val count = codeValue.text.count { it == '\n' } + 1
        (1..count).toList()
    }

    val problemLines = remember(problems) {
        problems.groupBy { it.line }
    }

    val syntaxTransformation = remember(language) {
        SyntaxVisualTransformation(language)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeBg)
            .transformable(state = transformState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers and gutter column
            Column(
                modifier = Modifier
                    .background(VsCodeGutter)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .widthIn(min = 40.dp)
            ) {
                lines.forEach { lineNum ->
                    val lineProblems = problemLines[lineNum]
                    val hasError = lineProblems?.any { it.severity == ProblemSeverity.ERROR } == true
                    val hasWarning = lineProblems?.any { it.severity == ProblemSeverity.WARNING } == true

                    Row(
                        modifier = Modifier
                            .height(with(androidx.compose.ui.platform.LocalDensity.current) { currentLineHeight.toDp() }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Diagnostic dot in gutter
                        Box(
                            modifier = Modifier.size(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasError) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(DiagnosticError)
                                )
                            } else if (hasWarning) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(DiagnosticWarning)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = lineNum.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = currentFontSize * 0.9f,
                            color = if (hasError) DiagnosticError else if (hasWarning) DiagnosticWarning else VsCodeLineNumber,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Gutter divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(VsCodeBorder)
            )

            // Code text field with horizontal scrolling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = codeValue,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_text_field"),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = currentFontSize,
                        lineHeight = currentLineHeight,
                        color = Color(0xFFD4D4D4)
                    ),
                    cursorBrush = SolidColor(Color(0xFF38BDF8)),
                    visualTransformation = syntaxTransformation
                )
            }
        }

        // Floating Find & Replace Bar
        if (isFindBarVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(8.dp),
                color = VsCodeSurface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, VsCodeBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = findText,
                            onValueChange = onFindTextChange,
                            placeholder = { Text("Find...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("find_input"),
                            textStyle = TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF007ACC),
                                unfocusedBorderColor = VsCodeBorder
                            )
                        )

                        IconButton(
                            onClick = onFindNext,
                            modifier = Modifier.size(34.dp).testTag("find_next_btn")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, "Next", tint = Color.White)
                        }

                        IconButton(
                            onClick = onCloseFindBar,
                            modifier = Modifier.size(34.dp).testTag("find_close_btn")
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF909090))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = replaceText,
                            onValueChange = onReplaceTextChange,
                            placeholder = { Text("Replace with...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("replace_input"),
                            textStyle = TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF007ACC),
                                unfocusedBorderColor = VsCodeBorder
                            )
                        )

                        TextButton(
                            onClick = onReplaceCurrent,
                            modifier = Modifier.testTag("replace_btn")
                        ) {
                            Text("Replace", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }

                        TextButton(
                            onClick = onReplaceAll,
                            modifier = Modifier.testTag("replace_all_btn")
                        ) {
                            Text("All", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }
            }
        }
    }
}
