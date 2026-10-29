package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SidebarTab
import com.example.ui.theme.DiagnosticError
import com.example.ui.theme.DiagnosticWarning
import com.example.ui.theme.VsCodeBlueLight
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar

@Composable
fun ActivityBar(
    activeTab: SidebarTab?,
    problemCount: Int,
    errorCount: Int,
    onTabSelected: (SidebarTab) -> Unit,
    onColorOsHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(50.dp)
            .fillMaxHeight()
            .background(VsCodeSidebar)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityBarItem(
                icon = Icons.Default.Folder,
                contentDescription = "Explorer",
                isSelected = activeTab == SidebarTab.EXPLORER,
                testTag = "activity_bar_explorer",
                onClick = { onTabSelected(SidebarTab.EXPLORER) }
            )

            ActivityBarItem(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                isSelected = activeTab == SidebarTab.SEARCH,
                testTag = "activity_bar_search",
                onClick = { onTabSelected(SidebarTab.SEARCH) }
            )

            ActivityBarItem(
                icon = Icons.Default.AutoAwesome,
                contentDescription = "AI Assistant",
                isSelected = activeTab == SidebarTab.AI_CHAT,
                accentColor = VsCodeBlueLight,
                testTag = "activity_bar_ai_chat",
                onClick = { onTabSelected(SidebarTab.AI_CHAT) }
            )

            ActivityBarItemWithBadge(
                icon = Icons.Default.BugReport,
                contentDescription = "Problems",
                isSelected = activeTab == SidebarTab.PROBLEMS,
                badgeCount = problemCount,
                badgeColor = if (errorCount > 0) DiagnosticError else DiagnosticWarning,
                testTag = "activity_bar_problems",
                onClick = { onTabSelected(SidebarTab.PROBLEMS) }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityBarItem(
                icon = Icons.Default.BatteryAlert,
                contentDescription = "ColorOS Optimization",
                isSelected = false,
                accentColor = Color(0xFFF59E0B),
                testTag = "activity_bar_coloros",
                onClick = onColorOsHelpClick
            )

            ActivityBarItem(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                isSelected = activeTab == SidebarTab.SETTINGS,
                testTag = "activity_bar_settings",
                onClick = { onTabSelected(SidebarTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun ActivityBarItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    testTag: String,
    onClick: () -> Unit
) {
    val activeColor = accentColor ?: MaterialTheme.colorScheme.primary
    val iconColor = if (isSelected) activeColor else Color(0xFF909090)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(activeColor)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ActivityBarItemWithBadge(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    badgeCount: Int,
    badgeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF909090)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = badgeColor,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            fontSize = 9.sp
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
