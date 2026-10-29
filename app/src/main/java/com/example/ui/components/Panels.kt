package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FileEntity
import com.example.data.local.ProjectEntity
import com.example.data.model.ProblemItem
import com.example.data.model.ProblemSeverity
import com.example.ui.BottomPanelTab
import com.example.ui.theme.DiagnosticError
import com.example.ui.theme.DiagnosticInfo
import com.example.ui.theme.DiagnosticWarning
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeYellow

// 1. File Explorer Pane
@Composable
fun FileExplorerPane(
    projects: List<ProjectEntity>,
    currentProject: ProjectEntity?,
    files: List<FileEntity>,
    activeFile: FileEntity?,
    onSelectProject: (ProjectEntity) -> Unit,
    onOpenFile: (FileEntity) -> Unit,
    onDeleteFile: (FileEntity) -> Unit,
    onNewFileClick: () -> Unit,
    onNewProjectClick: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var projectDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VsCodeSurface)
            .border(width = 1.dp, color = VsCodeBorder)
            .padding(8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EXPLORER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBBBBBB),
                letterSpacing = 1.sp
            )

            Row {
                IconButton(
                    onClick = onNewFileClick,
                    modifier = Modifier.size(28.dp).testTag("explorer_new_file_btn")
                ) {
                    Icon(Icons.Default.Add, "New File", tint = Color(0xFFD4D4D4), modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.size(28.dp).testTag("explorer_close_btn")
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Project selector card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { projectDropdownOpen = true }
                .testTag("project_selector_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, null, tint = VsCodeYellow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentProject?.name ?: "Select Project",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF909090), modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = projectDropdownOpen,
            onDismissRequest = { projectDropdownOpen = false },
            modifier = Modifier.background(VsCodeSurface)
        ) {
            projects.forEach { proj ->
                DropdownMenuItem(
                    text = { Text(proj.name, color = Color.White, fontSize = 13.sp) },
                    onClick = {
                        onSelectProject(proj)
                        projectDropdownOpen = false
                    }
                )
            }
            HorizontalDivider(color = VsCodeBorder)
            DropdownMenuItem(
                text = { Text("+ Create New Project", color = VsCodeBlueLight, fontSize = 13.sp) },
                onClick = {
                    projectDropdownOpen = false
                    onNewProjectClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Files Tree List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(files, key = { it.id }) { file ->
                val isSelected = file.id == activeFile?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF094771) else Color.Transparent)
                        .clickable { onOpenFile(file) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("file_item_${file.name}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = when {
                            file.name.endsWith(".html") -> Color(0xFFE34F26)
                            file.name.endsWith(".css") -> Color(0xFF1572B6)
                            file.name.endsWith(".js") -> Color(0xFFF7DF1E)
                            file.name.endsWith(".py") -> Color(0xFF3776AB)
                            file.name.endsWith(".kt") -> Color(0xFF7F52FF)
                            else -> Color(0xFF90CAF9)
                        },
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = file.name,
                        color = if (isSelected) Color.White else Color(0xFFD4D4D4),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = { onDeleteFile(file) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// 2. Search Across Files Pane
@Composable
fun SearchPane(
    query: String,
    results: List<Pair<FileEntity, List<Pair<Int, String>>>>,
    onQueryChange: (String) -> Unit,
    onNavigateToFileLine: (FileEntity, Int) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VsCodeSurface)
            .border(width = 1.dp, color = VsCodeBorder)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SEARCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBBBBBB))
            IconButton(onClick = onCloseSidebar, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, "Close", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search files...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("search_files_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VsCodeBlue,
                unfocusedBorderColor = VsCodeBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${results.sumOf { it.second.size }} results in ${results.size} files",
            fontSize = 11.sp,
            color = Color(0xFF909090)
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(results) { (file, matches) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = file.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeBlueLight
                    )
                    matches.forEach { (lineNum, snippet) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToFileLine(file, lineNum) }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Ln $lineNum:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF858585),
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = snippet,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD4D4D4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. AI Chat Pane
@Composable
fun AiChatPane(
    messages: List<ChatMessageEntity>,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onExplainCode: () -> Unit,
    onScanBugs: () -> Unit,
    onGenerateSnippet: (String) -> Unit,
    onClearChat: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VsCodeSurface)
            .border(width = 1.dp, color = VsCodeBorder)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = VsCodeBlueLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CODEFORGE AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Row {
                IconButton(onClick = onClearChat, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Clear, "Clear", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onCloseSidebar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick AI Action Prompts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = onExplainCode,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f).height(30.dp)
            ) {
                Text("Explain", fontSize = 10.sp, color = VsCodeBlueLight)
            }
            OutlinedButton(
                onClick = onScanBugs,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f).height(30.dp)
            ) {
                Text("Find Bugs", fontSize = 10.sp, color = Color(0xFFFBBF24))
            }
            OutlinedButton(
                onClick = { onGenerateSnippet("Write unit tests for this function") },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f).height(30.dp)
            ) {
                Text("Tests", fontSize = 10.sp, color = Color(0xFF4ADE80))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF0E639C) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isUser) "You" else "CodeForge AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Color(0xFFBBE5FD) else VsCodeBlueLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = VsCodeBlueLight, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI generating response...", fontSize = 11.sp, color = Color(0xFF909090))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask in English/Hinglish...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VsCodeBlue,
                    unfocusedBorderColor = VsCodeBorder
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(VsCodeBlue)
                    .testTag("ai_chat_send_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// 4. Problems Panel
@Composable
fun ProblemsPane(
    problems: List<ProblemItem>,
    isScanning: Boolean,
    onQuickFix: (ProblemItem) -> Unit,
    onFixAll: () -> Unit,
    onRescan: () -> Unit,
    onNavigateToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSurface)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PROBLEMS (${problems.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isScanning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = VsCodeBlueLight)
                }
            }

            Row {
                if (problems.isNotEmpty()) {
                    TextButton(onClick = onFixAll, modifier = Modifier.testTag("fix_all_problems_btn")) {
                        Text("Fix All", fontSize = 11.sp, color = Color(0xFF4ADE80))
                    }
                }
                IconButton(onClick = onRescan, modifier = Modifier.size(28.dp).testTag("rescan_problems_btn")) {
                    Icon(Icons.Default.Refresh, "Rescan", tint = VsCodeBlueLight, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (problems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No problems detected in workspace", fontSize = 12.sp, color = Color(0xFF858585))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(problems, key = { it.id }) { problem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLine(problem.line) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, VsCodeBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (problem.severity) {
                                            ProblemSeverity.ERROR -> DiagnosticError
                                            ProblemSeverity.WARNING -> DiagnosticWarning
                                            ProblemSeverity.INFO -> DiagnosticInfo
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Ln ${problem.line}: ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF909090)
                                    )
                                    Text(
                                        text = problem.message,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = problem.rule,
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { onQuickFix(problem) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("quick_fix_item_btn")
                            ) {
                                Icon(Icons.Default.Bolt, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Fix", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Terminal / Output Panel
@Composable
fun TerminalOutputPane(
    logs: List<String>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TERMINAL / LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onClearLogs, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Clear, "Clear", tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true
        ) {
            items(logs.reversed()) { log ->
                Text(
                    text = log,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (log.contains("error", ignoreCase = true)) DiagnosticError else if (log.contains("warn", ignoreCase = true)) DiagnosticWarning else Color(0xFFCBD5E1),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

// 6. Live Preview Panel (Interactive HTML/CSS/JS WebView)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewPane(
    htmlFile: FileEntity?,
    allFiles: List<FileEntity>,
    onConsoleLog: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Assemble complete HTML page with linked style.css and script.js embedded
    val assembledHtml = remember(htmlFile?.content, allFiles) {
        val rawHtml = htmlFile?.content ?: "<!DOCTYPE html><html><body><h1>No HTML file selected</h1></body></html>"
        val cssFile = allFiles.find { it.name.endsWith(".css") }
        val jsFile = allFiles.find { it.name.endsWith(".js") }

        var output = rawHtml
        if (cssFile != null) {
            val styleTag = "<style>\n${cssFile.content}\n</style>"
            output = if (output.contains("</head>")) {
                output.replace("</head>", "$styleTag\n</head>")
            } else {
                "$styleTag\n$output"
            }
        }
        if (jsFile != null) {
            val scriptTag = "<script>\n${jsFile.content}\n</script>"
            output = if (output.contains("</body>")) {
                output.replace("</body>", "$scriptTag\n</body>")
            } else {
                "$output\n$scriptTag"
            }
        }
        output
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSurface)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(Color(0xFF1E293B))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE PREVIEW (${htmlFile?.name ?: "index.html"})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row {
                IconButton(
                    onClick = {
                        webViewInstance?.loadDataWithBaseURL("https://local/", assembledHtml, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.size(28.dp).testTag("preview_refresh_btn")
                ) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("preview_close_btn")
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color(0xFF909090), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Embedded WebView
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("preview_webview"),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                onConsoleLog("[Console ${consoleMessage.messageLevel()}] ${consoleMessage.message()}")
                            }
                            return true
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    loadDataWithBaseURL("https://local/", assembledHtml, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            update = { view ->
                view.loadDataWithBaseURL("https://local/", assembledHtml, "text/html", "UTF-8", null)
            }
        )
    }
}
