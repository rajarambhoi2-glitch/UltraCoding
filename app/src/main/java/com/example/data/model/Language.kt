package com.example.data.model

enum class Language(
    val id: String,
    val displayName: String,
    val defaultExtension: String,
    val keywords: List<String>
) {
    HTML(
        id = "html",
        displayName = "HTML",
        defaultExtension = "html",
        keywords = listOf("html", "head", "body", "div", "span", "p", "a", "button", "input", "script", "style", "link", "meta", "title", "form", "table", "ul", "li", "h1", "h2", "h3")
    ),
    CSS(
        id = "css",
        displayName = "CSS",
        defaultExtension = "css",
        keywords = listOf("color", "background", "margin", "padding", "display", "flex", "grid", "position", "width", "height", "font-family", "border", "border-radius", "align-items", "justify-content", "transition")
    ),
    JAVASCRIPT(
        id = "javascript",
        displayName = "JavaScript",
        defaultExtension = "js",
        keywords = listOf("const", "let", "var", "function", "return", "if", "else", "for", "while", "class", "import", "export", "default", "from", "async", "await", "try", "catch", "new", "this", "typeof", "null", "undefined", "true", "false")
    ),
    TYPESCRIPT(
        id = "typescript",
        displayName = "TypeScript",
        defaultExtension = "ts",
        keywords = listOf("const", "let", "var", "function", "return", "if", "else", "for", "while", "class", "interface", "type", "import", "export", "default", "from", "async", "await", "try", "catch", "enum", "implements", "extends", "readonly", "private", "public", "protected")
    ),
    PYTHON(
        id = "python",
        displayName = "Python",
        defaultExtension = "py",
        keywords = listOf("def", "class", "return", "if", "elif", "else", "for", "while", "import", "from", "as", "try", "except", "finally", "with", "lambda", "yield", "pass", "None", "True", "False", "and", "or", "not", "is", "in", "self", "async", "await")
    ),
    KOTLIN(
        id = "kotlin",
        displayName = "Kotlin",
        defaultExtension = "kt",
        keywords = listOf("val", "var", "fun", "class", "object", "interface", "package", "import", "return", "if", "else", "when", "for", "while", "is", "in", "as", "data", "sealed", "override", "suspend", "coroutine", "null", "true", "false")
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        defaultExtension = "java",
        keywords = listOf("public", "private", "protected", "class", "interface", "void", "static", "final", "return", "if", "else", "for", "while", "new", "try", "catch", "throw", "throws", "import", "package", "this", "super", "int", "boolean", "String")
    ),
    CPP(
        id = "cpp",
        displayName = "C / C++",
        defaultExtension = "cpp",
        keywords = listOf("int", "float", "double", "char", "void", "include", "define", "struct", "class", "return", "if", "else", "for", "while", "auto", "namespace", "using", "std", "cout", "cin", "vector", "template", "const")
    ),
    JSON(
        id = "json",
        displayName = "JSON",
        defaultExtension = "json",
        keywords = listOf("true", "false", "null")
    ),
    MARKDOWN(
        id = "markdown",
        displayName = "Markdown",
        defaultExtension = "md",
        keywords = emptyList()
    ),
    SQL(
        id = "sql",
        displayName = "SQL",
        defaultExtension = "sql",
        keywords = listOf("SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "JOIN", "LEFT", "RIGHT", "INNER", "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "TABLE", "CREATE", "ALTER", "DROP", "PRIMARY", "KEY")
    ),
    XML(
        id = "xml",
        displayName = "XML",
        defaultExtension = "xml",
        keywords = emptyList()
    ),
    BASH(
        id = "bash",
        displayName = "Bash",
        defaultExtension = "sh",
        keywords = listOf("echo", "if", "then", "else", "fi", "for", "do", "done", "case", "esac", "while", "function", "return", "exit", "export", "local")
    ),
    RUST(
        id = "rust",
        displayName = "Rust",
        defaultExtension = "rs",
        keywords = listOf("fn", "let", "mut", "pub", "struct", "enum", "impl", "trait", "match", "if", "else", "loop", "while", "for", "in", "return", "use", "mod", "crate", "self", "async", "await")
    ),
    GO(
        id = "go",
        displayName = "Go",
        defaultExtension = "go",
        keywords = listOf("func", "package", "import", "var", "const", "type", "struct", "interface", "return", "if", "else", "for", "range", "go", "chan", "select", "defer", "nil", "true", "false")
    ),
    PHP(
        id = "php",
        displayName = "PHP",
        defaultExtension = "php",
        keywords = listOf("echo", "function", "return", "if", "else", "foreach", "class", "public", "private", "protected", "new", "this", "array", "namespace", "use")
    );

    companion object {
        fun fromExtension(ext: String): Language {
            val clean = ext.removePrefix(".").lowercase()
            return when (clean) {
                "html", "htm" -> HTML
                "css" -> CSS
                "js", "mjs", "cjs" -> JAVASCRIPT
                "ts", "tsx" -> TYPESCRIPT
                "py" -> PYTHON
                "kt", "kts" -> KOTLIN
                "java" -> JAVA
                "c", "cpp", "cc", "h", "hpp" -> CPP
                "json" -> JSON
                "xml" -> XML
                "md", "markdown" -> MARKDOWN
                "sql" -> SQL
                "sh", "bash" -> BASH
                "rs" -> RUST
                "go" -> GO
                "php" -> PHP
                else -> MARKDOWN
            }
        }
    }
}
