package com.mpvp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.mpvp.model.DanmakuDisplayState
import com.mpvp.model.DanmakuType
import com.mpvp.player.DanmakuManager

@Composable
fun DanmakuLayer(
    danmakuManager: DanmakuManager,
    modifier: Modifier = Modifier
) {
    val danmakus by danmakuManager.displayDanmakus.collectAsState()
    val opacity by danmakuManager.opacity.collectAsState()
    val enabled by danmakuManager.enabled.collectAsState()

    if (!enabled || danmakus.isEmpty()) return

    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            danmakus.forEach { danmaku ->
                Text(
                    text = danmaku.content,
                    color = Color(danmaku.color).copy(alpha = opacity),
                    fontSize = danmaku.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = when (danmaku.type) {
                        DanmakuType.SCROLL -> TextAlign.Left
                        DanmakuType.TOP, DanmakuType.BOTTOM -> TextAlign.Center
                    }
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            danmakus.forEachIndexed { index, danmaku ->
                val placeable = placeables[index]
                when (danmaku.type) {
                    DanmakuType.SCROLL -> {
                        placeable.placeRelative(
                            x = danmaku.x.toInt(),
                            y = danmaku.y.toInt()
                        )
                    }
                    DanmakuType.TOP -> {
                        val x = (constraints.maxWidth - placeable.width) / 2
                        placeable.placeRelative(
                            x = x,
                            y = danmaku.y.toInt()
                        )
                    }
                    DanmakuType.BOTTOM -> {
                        val x = (constraints.maxWidth - placeable.width) / 2
                        placeable.placeRelative(
                            x = x,
                            y = danmaku.y.toInt() - placeable.height
                        )
                    }
                }
            }
        }
    }
}