package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mpvp.model.DanmakuDisplayState
import com.mpvp.model.DanmakuType
import com.mpvp.player.DanmakuManager

/**
 * 弹幕显示层组件
 *
 * 显示滚动弹幕、顶部弹幕、底部弹幕
 *
 * @param danmakuManager 弹幕管理器
 * @param modifier 修饰符
 */
@Composable
fun DanmakuLayer(
    danmakuManager: DanmakuManager,
    modifier: Modifier = Modifier
) {
    val danmakus by danmakuManager.displayDanmakus.collectAsState()
    val opacity by danmakuManager.opacity.collectAsState()
    val enabled by danmakuManager.enabled.collectAsState()

    if (!enabled) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        danmakus.forEach { danmaku ->
            DanmakuText(
                danmaku = danmaku,
                opacity = opacity,
                modifier = Modifier
            )
        }
    }
}

/**
 * 单条弹幕文本组件
 */
@Composable
private fun DanmakuText(
    danmaku: DanmakuDisplayState,
    opacity: Float,
    modifier: Modifier = Modifier
) {
    val textColor = Color(danmaku.color).copy(alpha = opacity)

    val alignment = when (danmaku.type) {
        DanmakuType.SCROLL -> Alignment.CenterStart
        DanmakuType.TOP -> Alignment.TopCenter
        DanmakuType.BOTTOM -> Alignment.BottomCenter
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Text(
            text = danmaku.content,
            color = textColor,
            fontSize = danmaku.fontSize.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}
