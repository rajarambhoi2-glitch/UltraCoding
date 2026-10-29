package com.example.editor.syntax

import com.example.data.model.Language
import com.example.data.model.ProblemItem
import com.example.data.model.ProblemSeverity

object OfflineLinter {

    fun analyze(code: String, language: Language, filePath: String? = null): List<ProblemItem> {
        val problems = mutableListOf<ProblemItem>()
        val lines = code.lines()

        // 1. Bracket & Parentheses Mismatch Check
        val bracketStack = mutableListOf<Triple<Char, Int, Int>>() // char, line, col
        for ((lineIdx, lineText) in lines.withIndex()) {
            val lineNum = lineIdx + 1
            var inStr = false
            var strChar = ' '

            for ((colIdx, ch) in lineText.withIndex()) {
                val colNum = colIdx + 1
                if (!inStr) {
                    if (ch == '"' || ch == '\'' || ch == '`') {
                        inStr = true
                        strChar = ch
                    } else if (ch == '(' || ch == '{' || ch == '[') {
                        bracketStack.add(Triple(ch, lineNum, colNum))
                    } else if (ch == ')' || ch == '}' || ch == ']') {
                        if (bracketStack.isEmpty()) {
                            problems.add(
                                ProblemItem(
                                    line = lineNum,
                                    column = colNum,
                                    severity = ProblemSeverity.ERROR,
                                    message = "Unmatched closing bracket '$ch'",
                                    rule = "syntax/unmatched-bracket",
                                    suggestedFix = "",
                                    filePath = filePath
                                )
                            )
                        } else {
                            val top = bracketStack.removeAt(bracketStack.lastIndex)
                            val expected = when (top.first) {
                                '(' -> ')'
                                '{' -> '}'
                                '[' -> ']'
                                else -> ' '
                            }
                            if (ch != expected) {
                                problems.add(
                                    ProblemItem(
                                        line = lineNum,
                                        column = colNum,
                                        severity = ProblemSeverity.ERROR,
                                        message = "Mismatched bracket: expected '$expected' for '${top.first}' opened at line ${top.second}, but found '$ch'",
                                        rule = "syntax/bracket-mismatch",
                                        suggestedFix = expected.toString(),
                                        filePath = filePath
                                    )
                                )
                            }
                        }
                    }
                } else {
                    if (ch == strChar && (colIdx == 0 || lineText[colIdx - 1] != '\\')) {
                        inStr = false
                    }
                }
            }
        }

        // Leftover unclosed opening brackets
        for (unclosed in bracketStack) {
            val expected = when (unclosed.first) {
                '(' -> ')'
                '{' -> '}'
                '[' -> ']'
                else -> ' '
            }
            problems.add(
                ProblemItem(
                    line = unclosed.second,
                    column = unclosed.third,
                    severity = ProblemSeverity.ERROR,
                    message = "Unclosed bracket '${unclosed.first}', expected '$expected'",
                    rule = "syntax/unclosed-bracket",
                    suggestedFix = expected.toString(),
                    filePath = filePath
                )
            )
        }

        // 2. Line by Line Inspection
        for ((lineIdx, lineText) in lines.withIndex()) {
            val lineNum = lineIdx + 1
            val trimmed = lineText.trim()

            // Security: Hardcoded API keys or secrets
            if (trimmed.contains(Regex("(api[_-]?key|secret|token|password|auth_token)\\s*=\\s*['\"][a-zA-Z0-9_\\-]{16,}['\"]", RegexOption.IGNORE_CASE))) {
                problems.add(
                    ProblemItem(
                        line = lineNum,
                        severity = ProblemSeverity.WARNING,
                        message = "Possible hardcoded secret or API key. Store keys in secure environment variables or Android Secrets.",
                        rule = "security/hardcoded-credentials",
                        suggestedFix = "// TODO: Move secret to BuildConfig or Environment Variable",
                        filePath = filePath
                    )
                )
            }

            // Security: SQL Injection via concatenation
            if (trimmed.contains(Regex("(SELECT|UPDATE|DELETE|INSERT).*\\+\\s*[a-zA-Z0-9_]+", RegexOption.IGNORE_CASE))) {
                problems.add(
                    ProblemItem(
                        line = lineNum,
                        severity = ProblemSeverity.WARNING,
                        message = "Potential SQL injection risk detected in query string concatenation. Use parameterized queries.",
                        rule = "security/sql-injection-risk",
                        suggestedFix = null,
                        filePath = filePath
                    )
                )
            }

            // Language specific checks
            when (language) {
                Language.PYTHON -> {
                    // Python: Missing colon after def/if/elif/else/for/while/class
                    if (trimmed.matches(Regex("^(def|if|elif|else|for|while|class|with|try|except|finally)\\b.*")) &&
                        !trimmed.endsWith(":") && !trimmed.contains("#")
                    ) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.ERROR,
                                message = "Python syntax error: missing colon ':' at end of statement",
                                rule = "python/missing-colon",
                                suggestedFix = "$lineText:",
                                filePath = filePath
                            )
                        )
                    }

                    // Python: print as statement instead of function in py3
                    if (trimmed.matches(Regex("^print\\s+[\"'].*"))) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.WARNING,
                                message = "Python 2 style print statement detected. Use print(...) function for Python 3.",
                                rule = "python/print-function",
                                suggestedFix = lineText.replace("print ", "print(").trimEnd() + ")",
                                filePath = filePath
                            )
                        )
                    }
                }

                Language.JAVASCRIPT, Language.TYPESCRIPT -> {
                    // JS: innerHTML XSS risk
                    if (trimmed.contains(".innerHTML") && trimmed.contains("=")) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.WARNING,
                                message = "Assigning to 'innerHTML' poses Cross-Site Scripting (XSS) risks. Consider using 'textContent' or sanitizing input.",
                                rule = "security/xss-innerhtml",
                                suggestedFix = lineText.replace(".innerHTML", ".textContent"),
                                filePath = filePath
                            )
                        )
                    }

                    // JS: eval() security hazard
                    if (trimmed.contains(Regex("\\beval\\s*\\("))) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.ERROR,
                                message = "'eval()' allows arbitrary code execution and is a severe security vulnerability.",
                                rule = "security/no-eval",
                                suggestedFix = "// Refactor to eliminate eval()",
                                filePath = filePath
                            )
                        )
                    }

                    // JS: Loose equality == instead of ===
                    if (trimmed.contains(Regex("[^!=]==[^=]"))) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.INFO,
                                message = "Expected '===' and instead saw '=='. Prefer strict equality comparisons.",
                                rule = "js/strict-equality",
                                suggestedFix = lineText.replace("==", "==="),
                                filePath = filePath
                            )
                        )
                    }

                    // JS: Debugger statement in code
                    if (trimmed == "debugger;" || trimmed == "debugger") {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.WARNING,
                                message = "Unexpected 'debugger' statement in production code.",
                                rule = "js/no-debugger",
                                suggestedFix = "",
                                filePath = filePath
                            )
                        )
                    }
                }

                Language.HTML -> {
                    // HTML: unclosed self-closing tag or doctype check
                    if (lineNum == 1 && !trimmed.lowercase().startsWith("<!doctype html") && trimmed.contains("<html")) {
                        problems.add(
                            ProblemItem(
                                line = 1,
                                severity = ProblemSeverity.INFO,
                                message = "Missing modern HTML5 '<!DOCTYPE html>' declaration at line 1.",
                                rule = "html/doctype",
                                suggestedFix = "<!DOCTYPE html>\n$lineText",
                                filePath = filePath
                            )
                        )
                    }
                    if (trimmed.contains("<img") && !trimmed.contains("alt=")) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.INFO,
                                message = "Image <img> tag is missing an 'alt' attribute for web accessibility.",
                                rule = "html/img-alt",
                                suggestedFix = lineText.replace("<img", "<img alt=\"description\""),
                                filePath = filePath
                            )
                        )
                    }
                }

                Language.KOTLIN -> {
                    if (trimmed.contains("!!.")) {
                        problems.add(
                            ProblemItem(
                                line = lineNum,
                                severity = ProblemSeverity.WARNING,
                                message = "Unsafe non-null assertion call '!!' can cause NullPointerException. Prefer safe call '?.' or let.",
                                rule = "kotlin/unsafe-call",
                                suggestedFix = lineText.replace("!!.", "?."),
                                filePath = filePath
                            )
                        )
                    }
                }

                else -> {}
            }
        }

        return problems
    }
}
