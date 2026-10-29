package com.example

import com.example.data.model.Language
import com.example.data.model.ProblemSeverity
import com.example.editor.syntax.OfflineLinter
import com.example.editor.syntax.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testOfflineLinter_detectsMismatchedBrackets() {
        val badCode = "function test() { return (5 + 2]; }"
        val problems = OfflineLinter.analyze(badCode, Language.JAVASCRIPT)
        assertTrue(problems.isNotEmpty())
        assertTrue(problems.any { it.message.contains("Mismatched bracket") || it.severity == ProblemSeverity.ERROR })
    }

    @Test
    fun testOfflineLinter_detectsPythonMissingColon() {
        val badPython = "def calculate(x, y)\n    return x + y"
        val problems = OfflineLinter.analyze(badPython, Language.PYTHON)
        assertTrue(problems.any { it.rule == "python/missing-colon" })
    }

    @Test
    fun testOfflineLinter_detectsEvalSecurityRisk() {
        val jsWithEval = "const result = eval('2 + 2');"
        val problems = OfflineLinter.analyze(jsWithEval, Language.JAVASCRIPT)
        assertTrue(problems.any { it.rule == "security/no-eval" })
    }

    @Test
    fun testSyntaxHighlighter_producesSpans() {
        val sampleJs = "const message = 'Hello World';\nfunction greet() { return message; }"
        val annotated = SyntaxHighlighter.highlight(sampleJs, Language.JAVASCRIPT)
        assertEquals(sampleJs, annotated.text)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }
}
