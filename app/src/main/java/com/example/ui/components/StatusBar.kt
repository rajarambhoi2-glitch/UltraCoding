package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.theme.DiagnosticError
import com.example.ui.theme.DiagnosticWarning
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBlueLight

@Composable
fun StatusBar(
    language: Language,
    cursorLine: Int,
    cursorCol: Int,
    tabSize: Int,
    errorCount: Int,
    warningCount: Int,
    aiStatus: String,
    onProblemsClick: () -> Unit,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(VsCodeBlue)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Problems status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onProblemsClick)
                .testTag("status_bar_problems")
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$errorCount",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (errorCount > 0) Color(0xFFFFD1D1) else Color.White
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFDE68A),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$warningCount",
                fontSize = 11.sp,
                color = Color.White
            )
        }

        // Middle: AI status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onStatusClick)
                .testTag("status_bar_ai_status")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF93C5FD),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = aiStatus,
                fontSize = 11.sp,
                color = Color.White
            )
        }

        // Right: Ln, Col, Spaces, UTF-8, Language
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onStatusClick)
        ) {
            Text(
                text = "Ln $cursorLine, Col $cursorCol",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Spaces: $tabSize",
                fontSize = 10.sp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "UTF-8",
                fontSize = 10.sp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = language.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
