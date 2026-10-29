package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Language
import com.example.data.model.ProblemSeverity
import com.example.ui.BottomPanelTab
import com.example.ui.MainViewModel
import com.example.ui.SidebarTab
import com.example.ui.components.ActivityBar
import com.example.ui.components.AiChatPane
import com.example.ui.components.CodeEditorView
import com.example.ui.components.ColorOsHelpDialog
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.ExportApkDialog
import com.example.ui.components.ExtraKeyboardRow
import com.example.ui.components.FileExplorerPane
import com.example.ui.components.LivePreviewPane
import com.example.ui.components.NewFileDialog
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.ProblemsPane
import com.example.ui.components.QuickFixDiffDialog
import com.example.ui.components.SearchPane
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StatusBar
import com.example.ui.components.TabBar
import com.example.ui.components.TerminalOutputPane
import com.example.ui.theme.CodeForgeTheme
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeYellow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeForgeTheme {
                CodeForgeApp()
            }
        }
    }
}

@Composable
fun CodeForgeApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    // Handle back button when sidebars or bottom panels are open
    BackHandler(enabled = state.activeSidebar != null || state.activeBottomPanel != BottomPanelTab.NONE) {
        if (state.activeSidebar != null) {
            viewModel.setSidebarTab(null)
        } else if (state.activeBottomPanel != BottomPanelTab.NONE) {
            viewModel.setBottomPanelTab(BottomPanelTab.NONE)
        }
    }

    val context = LocalContext.current

    val activeLang = remember(state.activeFile?.name) {
        Language.fromExtension(state.activeFile?.name ?: "txt")
    }

    val errorCount = remember(state.problems) {
        state.problems.count { it.severity == ProblemSeverity.ERROR }
    }
    val warningCount = remember(state.problems) {
        state.problems.count { it.severity == ProblemSeverity.WARNING }
    }

    // Calculate cursor line & col
    val cursorOffset = state.activeCodeValue.selection.start
    val codeText = state.activeCodeValue.text
    val cursorLine = remember(cursorOffset, codeText) {
        if (codeText.isEmpty() || cursorOffset <= 0) 1
        else codeText.take(cursorOffset.coerceAtMost(codeText.length)).count { it == '\n' } + 1
    }
    val cursorCol = remember(cursorOffset, codeText) {
        if (codeText.isEmpty() || cursorOffset <= 0) 1
        else {
            val prefix = codeText.take(cursorOffset.coerceAtMost(codeText.length))
            val lastNewline = prefix.lastIndexOf('\n')
            if (lastNewline == -1) prefix.length + 1 else prefix.length - lastNewline
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(VsCodeBg),
        color = VsCodeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // App Header Toolbar
            TopAppBarRow(
                currentProjectName = state.currentProject?.name ?: "CodeForge AI",
                onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                onToggleFind = { viewModel.toggleFindBar(!state.isFindBarVisible) },
                onScanBugs = { viewModel.scanCodeWithAI() },
                onRunPreview = { viewModel.setBottomPanelTab(BottomPanelTab.LIVE_PREVIEW) },
                onExportApk = { viewModel.toggleExportDialog(true) },
                onOpenSettings = { viewModel.toggleSettingsDialog(true) }
            )

            // Main Editor Workspace (ActivityBar + Sidebar Pane + Editor Column)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Activity Bar (Left icon dock)
                ActivityBar(
                    activeTab = state.activeSidebar,
                    problemCount = state.problems.size,
                    errorCount = errorCount,
                    onTabSelected = { tab ->
                        if (tab == SidebarTab.SETTINGS) {
                            viewModel.toggleSettingsDialog(true)
                        } else {
                            viewModel.setSidebarTab(tab)
                        }
                    },
                    onColorOsHelpClick = { viewModel.toggleColorOsHelp(true) }
                )

                // Expandable Sidebar Pane
                AnimatedVisibility(
                    visible = state.activeSidebar != null,
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                    ) {
                        when (state.activeSidebar) {
                            SidebarTab.EXPLORER -> {
                                FileExplorerPane(
                                    projects = state.projects,
                                    currentProject = state.currentProject,
                                    files = state.projectFiles,
                                    activeFile = state.activeFile,
                                    onSelectProject = { viewModel.selectProject(it) },
                                    onOpenFile = {
                                        viewModel.openFile(it)
                                        viewModel.setSidebarTab(null)
                                    },
                                    onDeleteFile = { viewModel.deleteFile(it) },
                                    onNewFileClick = { viewModel.toggleNewFileDialog(true) },
                                    onNewProjectClick = { viewModel.toggleNewProjectDialog(true) },
                                    onCloseSidebar = { viewModel.setSidebarTab(null) }
                                )
                            }
                            SidebarTab.SEARCH -> {
                                SearchPane(
                                    query = state.searchQuery,
                                    results = state.searchResults,
                                    onQueryChange = { viewModel.searchInFiles(it) },
                                    onNavigateToFileLine = { file, line ->
                                        viewModel.openFile(file)
                                        viewModel.setSidebarTab(null)
                                    },
                                    onCloseSidebar = { viewModel.setSidebarTab(null) }
                                )
                            }
                            SidebarTab.AI_CHAT -> {
                                AiChatPane(
                                    messages = state.chatMessages,
                                    isGenerating = state.isChatGenerating,
                                    onSendMessage = { viewModel.sendChatMessage(it) },
                                    onExplainCode = { viewModel.explainActiveCode() },
                                    onScanBugs = { viewModel.scanCodeWithAI() },
                                    onGenerateSnippet = { viewModel.generateCodeFromPrompt(it) },
                                    onClearChat = { viewModel.clearChat() },
                                    onCloseSidebar = { viewModel.setSidebarTab(null) }
                                )
                            }
                            SidebarTab.PROBLEMS -> {
                                ProblemsPane(
                                    problems = state.problems,
                                    isScanning = state.isScanningBugs,
                                    onQuickFix = { viewModel.requestQuickFix(it) },
                                    onFixAll = { viewModel.fixAllProblems() },
                                    onRescan = { viewModel.scanCodeWithAI() },
                                    onNavigateToLine = {
                                        viewModel.setSidebarTab(null)
                                    }
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // Main Editor Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Multi-tab bar
                    TabBar(
                        tabs = state.openTabs,
                        activeFile = state.activeFile,
                        onTabClick = { viewModel.openFile(it) },
                        onCloseTab = { viewModel.closeTab(it) },
                        onNewFileClick = { viewModel.toggleNewFileDialog(true) },
                        onRunPreviewClick = { viewModel.setBottomPanelTab(BottomPanelTab.LIVE_PREVIEW) },
                        onAiScanClick = { viewModel.scanCodeWithAI() }
                    )

                    // Code Editor Canvas
                    Box(
                        modifier = Modifier
                            .weight(if (state.activeBottomPanel != BottomPanelTab.NONE) 0.55f else 1f)
                            .fillMaxWidth()
                    ) {
                        if (state.activeFile != null) {
                            CodeEditorView(
                                codeValue = state.activeCodeValue,
                                language = activeLang,
                                problems = state.problems,
                                baseFontSize = state.settings.fontSize,
                                onCodeChange = { viewModel.onCodeChange(it) },
                                isFindBarVisible = state.isFindBarVisible,
                                findText = state.findText,
                                replaceText = state.replaceText,
                                useRegex = state.useRegex,
                                onFindTextChange = { viewModel.updateFindQuery(it) },
                                onReplaceTextChange = { viewModel.updateReplaceQuery(it) },
                                onFindNext = { viewModel.findNext() },
                                onReplaceCurrent = { viewModel.replaceCurrent() },
                                onReplaceAll = { viewModel.replaceAll() },
                                onCloseFindBar = { viewModel.toggleFindBar(false) }
                            )
                        } else {
                            // Empty workspace placeholder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = VsCodeYellow,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No File Open",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Open Explorer or tap '+' to create a file",
                                        fontSize = 13.sp,
                                        color = Color(0xFF909090)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Panel (Problems, Terminal/Output, LivePreview)
                    if (state.activeBottomPanel != BottomPanelTab.NONE) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxWidth()
                                .background(VsCodeSurface)
                                .border(1.dp, VsCodeBorder)
                        ) {
                            // Panel header tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BottomTabItem(
                                        label = "Problems (${state.problems.size})",
                                        isSelected = state.activeBottomPanel == BottomPanelTab.PROBLEMS,
                                        onClick = { viewModel.setBottomPanelTab(BottomPanelTab.PROBLEMS) }
                                    )
                                    BottomTabItem(
                                        label = "Output / Logs",
                                        isSelected = state.activeBottomPanel == BottomPanelTab.OUTPUT,
                                        onClick = { viewModel.setBottomPanelTab(BottomPanelTab.OUTPUT) }
                                    )
                                    BottomTabItem(
                                        label = "Live Preview",
                                        isSelected = state.activeBottomPanel == BottomPanelTab.LIVE_PREVIEW,
                                        onClick = { viewModel.setBottomPanelTab(BottomPanelTab.LIVE_PREVIEW) }
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.setBottomPanelTab(BottomPanelTab.NONE) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Close Panel", tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                when (state.activeBottomPanel) {
                                    BottomPanelTab.PROBLEMS -> {
                                        ProblemsPane(
                                            problems = state.problems,
                                            isScanning = state.isScanningBugs,
                                            onQuickFix = { viewModel.requestQuickFix(it) },
                                            onFixAll = { viewModel.fixAllProblems() },
                                            onRescan = { viewModel.scanCodeWithAI() },
                                            onNavigateToLine = { }
                                        )
                                    }
                                    BottomPanelTab.OUTPUT -> {
                                        TerminalOutputPane(
                                            logs = state.terminalLogs,
                                            onClearLogs = { viewModel.appendTerminal("Terminal cleared.") }
                                        )
                                    }
                                    BottomPanelTab.LIVE_PREVIEW -> {
                                        val htmlFile = state.projectFiles.find { it.name.endsWith(".html") } ?: state.activeFile
                                        LivePreviewPane(
                                            htmlFile = htmlFile,
                                            allFiles = state.projectFiles,
                                            onConsoleLog = { viewModel.appendTerminal(it) },
                                            onClose = { viewModel.setBottomPanelTab(BottomPanelTab.NONE) }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }

            // Mobile-Friendly Extra Keyboard Row (Smoothly adjusted above IME)
            ExtraKeyboardRow(
                tabSize = state.settings.tabSize,
                onInsertText = { text, offset -> viewModel.insertTextAtCursor(text, offset) },
                onMoveCursor = { delta -> viewModel.moveCursor(delta) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onQuickFixClick = {
                    val firstProblem = state.problems.firstOrNull()
                    if (firstProblem != null) {
                        viewModel.requestQuickFix(firstProblem)
                    } else {
                        viewModel.scanCodeWithAI()
                    }
                },
                onAiWriteClick = {
                    viewModel.generateCodeFromPrompt("Add helper function with comments")
                },
                modifier = Modifier.imePadding()
            )

            // Bottom Status Bar
            StatusBar(
                language = activeLang,
                cursorLine = cursorLine,
                cursorCol = cursorCol,
                tabSize = state.settings.tabSize,
                errorCount = errorCount,
                warningCount = warningCount,
                aiStatus = state.aiStatusMessage,
                onProblemsClick = { viewModel.setBottomPanelTab(BottomPanelTab.PROBLEMS) },
                onStatusClick = { viewModel.toggleCommandPalette(true) },
                modifier = Modifier.navigationBarsPadding()
            )
        }

        // Dialogs
        state.quickFixDialogData?.let { quickFix ->
            QuickFixDiffDialog(
                quickFix = quickFix,
                onAccept = { viewModel.applyQuickFix(quickFix) },
                onReject = { viewModel.dismissQuickFix() },
                onFixAll = { viewModel.fixAllProblems() }
            )
        }

        if (state.isCommandPaletteOpen) {
            CommandPaletteDialog(
                onDismiss = { viewModel.toggleCommandPalette(false) },
                onFindBugs = { viewModel.scanCodeWithAI() },
                onFixAll = { viewModel.fixAllProblems() },
                onExplain = { viewModel.explainActiveCode() },
                onLivePreview = { viewModel.setBottomPanelTab(BottomPanelTab.LIVE_PREVIEW) },
                onNewFile = { viewModel.toggleNewFileDialog(true) },
                onNewProject = { viewModel.toggleNewProjectDialog(true) },
                onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                onColorOsHelp = { viewModel.toggleColorOsHelp(true) },
                onExportApk = { viewModel.toggleExportDialog(true) }
            )
        }

        if (state.isExportDialogOpen) {
            ExportApkDialog(
                step = state.exportPipelineStep,
                resultMsg = state.exportResultMsg,
                onStartPipeline = { viewModel.runExportPipeline(context) },
                onShareApk = { viewModel.shareExportedApk(context) },
                onShareZip = { viewModel.shareProjectZip(context) },
                onDismiss = { viewModel.toggleExportDialog(false) }
            )
        }

        if (state.isSettingsOpen) {
            SettingsDialog(
                currentSettings = state.settings,
                onSave = { viewModel.saveSettings(it) },
                onDismiss = { viewModel.toggleSettingsDialog(false) }
            )
        }

        if (state.isColorOsHelpOpen) {
            ColorOsHelpDialog(
                onDismiss = { viewModel.toggleColorOsHelp(false) }
            )
        }

        if (state.isNewFileDialogOpen) {
            NewFileDialog(
                onDismiss = { viewModel.toggleNewFileDialog(false) },
                onCreate = { viewModel.createFile(it) }
            )
        }

        if (state.isNewProjectDialogOpen) {
            NewProjectDialog(
                onDismiss = { viewModel.toggleNewProjectDialog(false) },
                onCreate = { name, tpl -> viewModel.createProject(name, tpl) }
            )
        }
    }
}

@Composable
private fun TopAppBarRow(
    currentProjectName: String,
    onOpenCommandPalette: () -> Unit,
    onToggleFind: () -> Unit,
    onScanBugs: () -> Unit,
    onRunPreview: () -> Unit,
    onExportApk: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(VsCodeSidebar)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Current Project
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onOpenCommandPalette)
                .testTag("app_header_title_bar")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = VsCodeBlueLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CodeForge",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = " AI",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VsCodeBlueLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "— $currentProjectName",
                fontSize = 12.sp,
                color = Color(0xFF909090)
            )
        }

        // Header Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenCommandPalette,
                modifier = Modifier.size(34.dp).testTag("header_command_palette_btn")
            ) {
                Icon(Icons.Default.Search, "Command Palette", tint = Color(0xFFD4D4D4), modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onToggleFind,
                modifier = Modifier.size(34.dp).testTag("header_find_btn")
            ) {
                Icon(Icons.Default.FindReplace, "Find and Replace", tint = Color(0xFFD4D4D4), modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onScanBugs,
                modifier = Modifier.size(34.dp).testTag("header_scan_bugs_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Bug Finder", tint = VsCodeBlueLight, modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onRunPreview,
                modifier = Modifier.size(34.dp).testTag("header_run_preview_btn")
            ) {
                Icon(Icons.Default.PlayArrow, "Live Preview", tint = Color(0xFF4ADE80), modifier = Modifier.size(22.dp))
            }

            IconButton(
                onClick = onExportApk,
                modifier = Modifier.size(34.dp).testTag("header_export_apk_btn")
            ) {
                Icon(Icons.Default.Download, "Build & Export APK", tint = VsCodeBlueLight, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}
