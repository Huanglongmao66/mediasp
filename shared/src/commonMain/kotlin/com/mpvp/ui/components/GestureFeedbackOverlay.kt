package com.mpvp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpvp.utils.TimeFormatter

/**
 * 手势反馈类型
 */
enum class GestureFeedbackType {
    NONE,
    VOLUME,
    BRIGHTNESS,
    PROGRESS_FORWARD,
    PROGRESS_BACKWARD,
    LONG_PRESS_SPEED
}

/**
 * 手势反馈数据
 *
 * @property type 反馈类型
 * @property value 当前值（0-1用于音量/亮度，毫秒用于进度）
 * @property maxValue 最大值
 */
data class GestureFeedback(
    val type: GestureFeedbackType,
    val value: Float,
    val maxValue: Float = 1f
)

/**
 * 手势反馈UI组件
 *
 * 在用户进行手势操作时显示对应的视觉反馈
 *
 * @param feedback 当前手势反馈数据
 * @param modifier 修饰符
 */
@Composable
fun GestureFeedbackOverlay(
    feedback: GestureFeedback?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = feedback != null && feedback.type != GestureFeedbackType.NONE,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (feedback == null) return@AnimatedVisibility

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (feedback.type) {
                GestureFeedbackType.VOLUME -> VolumeFeedback(
                    value = feedback.value,
                    maxValue = feedback.maxValue
                )
                GestureFeedbackType.BRIGHTNESS -> BrightnessFeedback(
                    value = feedback.value,
                    maxValue = feedback.maxValue
                )
                GestureFeedbackType.PROGRESS_FORWARD,
                GestureFeedbackType.PROGRESS_BACKWARD -> ProgressFeedback(
                    isForward = feedback.type == GestureFeedbackType.PROGRESS_FORWARD,
                    positionMs = feedback.value.toLong(),
                    durationMs = feedback.maxValue.toLong()
                )
                GestureFeedbackType.LONG_PRESS_SPEED -> LongPressSpeedFeedback()
                else -> {}
            }
        }
    }
}

/**
 * 音量反馈
 */
@Composable
private fun VolumeFeedback(
    value: Float,
    maxValue: Float
) {
    val percent = ((value / maxValue) * 100).toInt().coerceIn(0, 100)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$percent%",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = value / maxValue,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

/**
 * 亮度反馈
 */
@Composable
private fun BrightnessFeedback(
    value: Float,
    maxValue: Float
) {
    val percent = ((value / maxValue) * 100).toInt().coerceIn(0, 100)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.BrightnessHigh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$percent%",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = value / maxValue,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

/**
 * 进度反馈
 */
@Composable
private fun ProgressFeedback(
    isForward: Boolean,
    positionMs: Long,
    durationMs: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isForward) Icons.Filled.FastForward else Icons.Filled.FastRewind,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = TimeFormatter.formatDuration(positionMs),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "/ ${TimeFormatter.formatDuration(durationMs)}",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

/**
 * 长按倍速反馈
 */
@Composable
private fun LongPressSpeedFeedback() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 40.dp, vertical = 28.dp)
    ) {
        Text(
            text = "3x",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "倍速播放中",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}
