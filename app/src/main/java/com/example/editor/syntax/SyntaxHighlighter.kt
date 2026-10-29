package com.example.editor.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.Language
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeLightGreen
import com.example.ui.theme.VsCodeOrange
import com.example.ui.theme.VsCodeYellow
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val numberPattern = Pattern.compile("\\b(\\d+(\\.\\d+)?|0x[0-9a-fA-F]+)\\b")
    private val functionPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)(?=\\s*\\()")
    private val typePattern = Pattern.compile("\\b[A-Z][a-zA-Z0-9_]*\\b")

    fun highlight(code: String, language: Language): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(code)

            // 1. Comments
            highlightComments(code, language)

            // 2. Strings
            highlightStrings(code, language)

            // 3. Numbers
            highlightPattern(code, numberPattern, VsCodeLightGreen)

            // 4. Keywords
            if (language.keywords.isNotEmpty()) {
                val keywordRegex = Pattern.compile("\\b(" + language.keywords.joinToString("|") { Pattern.quote(it) } + ")\\b")
                highlightPattern(code, keywordRegex, VsCodeBlue, FontWeight.SemiBold)
            }

            // 5. Types / Classes (starts with Capital letter)
            highlightPattern(code, typePattern, VsCodeCyan)

            // 6. Functions / Method calls
            highlightPattern(code, functionPattern, VsCodeYellow)

            // 7. Special Language elements
            when (language) {
                Language.HTML, Language.XML -> {
                    val tagPattern = Pattern.compile("</?[a-zA-Z0-9_-]+|/?>")
                    highlightPattern(code, tagPattern, VsCodeBlueLight)
                    val attrPattern = Pattern.compile("\\b[a-zA-Z0-9_-]+(?=\\=)")
                    highlightPattern(code, attrPattern, VsCodeCyan)
                }
                Language.CSS -> {
                    val cssPropPattern = Pattern.compile("(?m)^\\s*([a-zA-Z0-9_-]+)\\s*:")
                    highlightPattern(code, cssPropPattern, VsCodeCyan)
                }
                Language.MARKDOWN -> {
                    val headerPattern = Pattern.compile("(?m)^#{1,6}\\s.*$")
                    highlightPattern(code, headerPattern, VsCodeBlueLight, FontWeight.Bold)
                }
                else -> {}
            }
        }
    }

    private fun AnnotatedString.Builder.highlightPattern(
        text: String,
        pattern: Pattern,
        color: Color,
        fontWeight: FontWeight? = null
    ) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start in 0 until text.length && end <= text.length) {
                addStyle(
                    SpanStyle(color = color, fontWeight = fontWeight),
                    start,
                    end
                )
            }
        }
    }

    private fun AnnotatedString.Builder.highlightComments(text: String, language: Language) {
        when (language) {
            Language.PYTHON, Language.BASH -> {
                val pyComment = Pattern.compile("#.*$")
                val m = pyComment.matcher(text)
                while (m.find()) {
                    addStyle(SpanStyle(color = VsCodeGreen), m.start(), m.end())
                }
            }
            Language.HTML, Language.XML -> {
                val htmlComment = Pattern.compile("<!--[\\s\\S]*?-->")
                val m = htmlComment.matcher(text)
                while (m.find()) {
                    addStyle(SpanStyle(color = VsCodeGreen), m.start(), m.end())
                }
            }
            Language.SQL -> {
                val sqlComment = Pattern.compile("--.*$")
                val m = sqlComment.matcher(text)
                while (m.find()) {
                    addStyle(SpanStyle(color = VsCodeGreen), m.start(), m.end())
                }
            }
            else -> {
                // Line comment //
                val lineComment = Pattern.compile("//.*$")
                val m1 = lineComment.matcher(text)
                while (m1.find()) {
                    addStyle(SpanStyle(color = VsCodeGreen), m1.start(), m1.end())
                }
                // Block comment /* */
                val blockComment = Pattern.compile("/\\*[\\s\\S]*?\\*/")
                val m2 = blockComment.matcher(text)
                while (m2.find()) {
                    addStyle(SpanStyle(color = VsCodeGreen), m2.start(), m2.end())
                }
            }
        }
    }

    private fun AnnotatedString.Builder.highlightStrings(text: String, language: Language) {
        // Double quote strings
        val doubleQuote = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"")
        val m1 = doubleQuote.matcher(text)
        while (m1.find()) {
            addStyle(SpanStyle(color = VsCodeOrange), m1.start(), m1.end())
        }

        // Single quote strings
        val singleQuote = Pattern.compile("'(\\\\.|[^'\\\\])*'")
        val m2 = singleQuote.matcher(text)
        while (m2.find()) {
            addStyle(SpanStyle(color = VsCodeOrange), m2.start(), m2.end())
        }

        // Backtick strings (JS / TS)
        if (language == Language.JAVASCRIPT || language == Language.TYPESCRIPT || language == Language.GO) {
            val backtick = Pattern.compile("`(\\\\.|[^`\\\\])*`")
            val m3 = backtick.matcher(text)
            while (m3.find()) {
                addStyle(SpanStyle(color = VsCodeOrange), m3.start(), m3.end())
            }
        }
    }
}
