package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpvp.model.SubtitleItem
import com.mpvp.player.SubtitleManager

/**
 * 字幕显示层
 *
 * 在播放器底部显示当前字幕
 */
@Composable
fun SubtitleLayer(
    subtitleManager: SubtitleManager,
    modifier: Modifier = Modifier
) {
    val currentSubtitle by subtitleManager.currentSubtitle.collectAsState()
    val enabled by subtitleManager.enabled.collectAsState()

    if (!enabled || currentSubtitle == null) return

    SubtitleText(
        subtitle = currentSubtitle!!,
        modifier = modifier
    )
}

@Composable
private fun SubtitleText(
    subtitle: SubtitleItem,
    modifier: Modifier = Modifier
) {
    val textColor = Color(subtitle.style.color)
    val bgColor = Color(subtitle.style.backgroundColor)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = subtitle.text,
            color = textColor,
            fontSize = subtitle.style.fontSize.sp,
            fontWeight = if (subtitle.style.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (subtitle.style.isItalic) FontStyle.Italic else FontStyle.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}