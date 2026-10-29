package com.example.data.ai

import com.example.data.model.Language
import com.example.data.model.ProblemItem
import com.example.data.model.QuickFixResult
import com.example.editor.syntax.OfflineLinter

class OfflineFallbackProvider : AIProvider {
    override val name: String = "Offline Analyzer"

    override suspend fun findBugs(
        code: String,
        language: String,
        context: String?
    ): Result<List<ProblemItem>> {
        val lang = Language.fromExtension(language)
        val problems = OfflineLinter.analyze(code, lang)
        return Result.success(problems)
    }

    override suspend fun quickFix(
        code: String,
        problem: ProblemItem,
        language: String
    ): Result<QuickFixResult> {
        val lines = code.lines().toMutableList()
        val lineIdx = (problem.line - 1).coerceIn(0, lines.size - 1)

        val fixedLine = when {
            problem.suggestedFix != null -> {
                if (problem.suggestedFix.isEmpty()) {
                    // Line removed or empty
                    null
                } else if (problem.suggestedFix.startsWith("<!DOCTYPE")) {
                    problem.suggestedFix
                } else {
                    problem.suggestedFix
                }
            }
            else -> lines[lineIdx]
        }

        if (fixedLine == null) {
            lines.removeAt(lineIdx)
        } else {
            lines[lineIdx] = fixedLine
        }

        val newCode = lines.joinToString("\n")
        return Result.success(
            QuickFixResult(
                originalCode = code,
                fixedCode = newCode,
                explanation = "Applied quick fix for ${problem.rule}: ${problem.message}",
                diffSummary = "Updated line ${problem.line}"
            )
        )
    }

    override suspend fun writeCode(
        prompt: String,
        contextCode: String?,
        language: String
    ): Result<String> {
        val lower = prompt.lowercase()
        val template = when {
            lower.contains("login") || lower.contains("form") -> """<form class="login-form" onsubmit="handleLogin(event)">
  <h2>Login to Account</h2>
  <div class="input-group">
    <label for="email">Email Address</label>
    <input type="email" id="email" required placeholder="user@example.com">
  </div>
  <div class="input-group">
    <label for="password">Password</label>
    <input type="password" id="password" required placeholder="••••••••">
  </div>
  <button type="submit" class="btn primary">Sign In</button>
</form>"""
            lower.contains("counter") || lower.contains("button") -> """function createCounter() {
  let count = 0;
  return {
    increment: () => ++count,
    decrement: () => --count,
    get: () => count
  };
}"""
            lower.contains("binary search") -> """def binary_search(arr, target):
    left, right = 0, len(arr) - 1
    while left <= right:
        mid = (left + right) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    return -1"""
            else -> """// Code generated for: $prompt
function generatedSolution() {
  console.log("Executing task: $prompt");
  return true;
}"""
        }
        return Result.success(template)
    }

    override suspend fun explainCode(
        code: String,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val explanation = if (hinglish) {
            """🔍 **Code Explanation (Hinglish):**
- Ye code `${language.uppercase()}` mein likha gaya hai.
- Iska primary purpose active tasks ya algorithm logic ko handle karna hai.
- Code structured hai aur standard syntax conventions follow karta hai.
- Agar koi specific line samajhni hai to AI Chat mein poochho!"""
        } else {
            """🔍 **Code Analysis:**
- Language: `${language.uppercase()}`
- Structure: Defines logic blocks, variables and execution routines.
- Maintainability: Good modularity and readable layout."""
        }
        return Result.success(explanation)
    }

    override suspend fun chat(
        messages: List<Pair<String, String>>,
        currentCode: String?,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val lastMsg = messages.lastOrNull()?.second?.lowercase() ?: ""
        val response = if (hinglish) {
            when {
                lastMsg.contains("explain") || lastMsg.contains("samjhao") ->
                    "Bhai, ye code ${language} language mein hai. Isme core logic structured way mein likhi hui hai. Isko optimize karne ke liye functions ko modular rakho aur edge cases test karo."
                lastMsg.contains("bug") || lastMsg.contains("error") ->
                    "Code scan kiya gaya hai! Offline linter check kar raha hai bracket matching aur standard rules. Problems panel mein detailed issues dekho aur 'Quick Fix' par tap karo."
                lastMsg.contains("refactor") || lastMsg.contains("optimize") ->
                    "Refactoring suggestion: Har function ko single responsibility principle (SRP) do, magic numbers ko constants banao aur error handling add karo."
                else ->
                    "Namaste! Main CodeForge AI assistant hoon. Main code likh sakta hoon, bugs dhoondh sakta hoon aur fixes suggest kar sakta hoon. Aap Settings mein apni Gemini/OpenAI API key bhi set kar sakte hain for live online streaming!"
            }
        } else {
            "Hello! I am your CodeForge AI Assistant. I can analyze code, locate bugs, suggest quick-fixes, or write functions for you. You can configure your custom Gemini or OpenAI API keys in Settings anytime!"
        }
        return Result.success(response)
    }
}
