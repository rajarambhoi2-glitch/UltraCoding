package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FileEntity
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeTabActive
import com.example.ui.theme.VsCodeTabInactive

@Composable
fun TabBar(
    tabs: List<FileEntity>,
    activeFile: FileEntity?,
    onTabClick: (FileEntity) -> Unit,
    onCloseTab: (FileEntity) -> Unit,
    onNewFileClick: () -> Unit,
    onRunPreviewClick: () -> Unit,
    onAiScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .background(VsCodeTabInactive),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { file ->
                val isActive = file.id == activeFile?.id
                TabItem(
                    file = file,
                    isActive = isActive,
                    onClick = { onTabClick(file) },
                    onClose = { onCloseTab(file) }
                )
            }

            IconButton(
                onClick = onNewFileClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("tab_bar_new_file_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New File",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Quick Action icons on top right of tab bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            IconButton(
                onClick = onAiScanClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("tab_bar_ai_scan_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Bug Scan",
                    tint = VsCodeBlueLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRunPreviewClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("tab_bar_run_preview_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run Live Preview",
                    tint = Color(0xFF4ADE80),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    file: FileEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val bgColor = if (isActive) VsCodeTabActive else VsCodeTabInactive
    val textColor = if (isActive) Color.White else Color(0xFF969696)

    Box(
        modifier = Modifier
            .height(38.dp)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
            .testTag("tab_${file.name}"),
        contentAlignment = Alignment.CenterStart
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxHeight(0.08f)
                    .width(48.dp)
                    .background(VsCodeBlue)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = file.name,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (file.isDirty) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .testTag("close_tab_${file.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tab",
                        tint = Color(0xFF808080),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
