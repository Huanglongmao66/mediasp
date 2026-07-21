package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpvp.player.DanmakuManager

@Composable
fun DanmakuSettingsPanel(
    danmakuManager: DanmakuManager,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled by danmakuManager.enabled.collectAsState()
    val opacity by danmakuManager.opacity.collectAsState()
    val speed by danmakuManager.speed.collectAsState()
    val fontScale by danmakuManager.fontScale.collectAsState()
    val displayAreaRatio by danmakuManager.displayAreaRatio.collectAsState()

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color(0xFF2C2C2C), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                .width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "弹幕设置",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "关闭",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onClose() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "开启弹幕",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { danmakuManager.setEnabled(it) },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                SettingSlider(
                    label = "透明度",
                    value = opacity,
                    valueRange = 0.1f..1f,
                    steps = 9,
                    formatValue = { "${(it * 100).toInt()}%" },
                    onValueChange = { danmakuManager.setOpacity(it) }
                )

                SettingSlider(
                    label = "弹幕速度",
                    value = speed,
                    valueRange = 0.5f..3f,
                    steps = 5,
                    formatValue = { "${it}x" },
                    onValueChange = { danmakuManager.setSpeed(it) }
                )

                SettingSlider(
                    label = "字体大小",
                    value = fontScale,
                    valueRange = 0.5f..2f,
                    steps = 3,
                    formatValue = { "${(it * 100).toInt()}%" },
                    onValueChange = { danmakuManager.setFontScale(it) }
                )

                SettingSlider(
                    label = "显示区域",
                    value = displayAreaRatio,
                    valueRange = 0.2f..1f,
                    steps = 4,
                    formatValue = { "${(it * 100).toInt()}%" },
                    onValueChange = { danmakuManager.setDisplayAreaRatio(it) }
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    formatValue: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = formatValue(value),
                color = Color(0xFF00D4FF),
                fontSize = 14.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
}