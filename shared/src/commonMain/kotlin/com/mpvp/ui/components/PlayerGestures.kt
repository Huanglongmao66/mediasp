package com.mpvp.ui.components

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.mpvp.model.PlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 播放器手势控制修饰符
 *
 * 提供视频播放器的手势控制功能：
 * - 单击：显示/隐藏控制器
 * - 双击：播放/暂停
 * - 左侧垂直滑动：调节亮度
 * - 右侧垂直滑动：调节音量
 * - 水平滑动：调节播放进度
 * - 长按：3倍速播放
 *
 * @param state 播放器状态
 * @param onTogglePlayPause 切换播放暂停
 * @param onSeekTo 跳转到位置
 * @param onVolumeChanged 音量变化
 * @param onBrightnessChanged 亮度变化
 * @param onControllerToggle 控制器显示切换
 * @param onLongPressStart 长按开始
 * @param onLongPressEnd 长按结束
 */
fun Modifier.playerGestures(
    state: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onControllerToggle: () -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit
): Modifier = composed {
    val scope = rememberCoroutineScope()
    var size by remember { mutableStateOf(IntSize.Zero) }
    var isLongPressing by remember { mutableStateOf(false) }

    this
        .onSizeChanged { size = it }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { onControllerToggle() },
                onDoubleTap = { onTogglePlayPause() }
            )
        }
        .pointerInput(Unit) {
            var dragType = DragType.NONE
            var startValue = 0f
            var startPosition = Offset.Zero
            var isDragging = false

            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    startPosition = down.position
                    isDragging = false
                    dragType = DragType.NONE

                    val horizontalDrag = horizontalDrag@ { change: androidx.compose.ui.input.pointer.PointerEvent ->
                        val position = change.changes.firstOrNull()?.position ?: return@horizontalDrag
                        if (!isDragging) {
                            val dx = abs(position.x - startPosition.x)
                            val dy = abs(position.y - startPosition.y)
                            if (dx > 10f && dx > dy) {
                                isDragging = true
                                dragType = DragType.PROGRESS
                                startValue = state.currentPosition.toFloat()
                            }
                        }
                        if (isDragging && dragType == DragType.PROGRESS && size.width > 0 && state.duration > 0) {
                            val dx = position.x - startPosition.x
                            val progressDelta = (dx / size.width) * state.duration
                            val newPosition = (startValue + progressDelta).coerceIn(0f, state.duration.toFloat())
                            onSeekTo(newPosition.toLong())
                        }
                    }

                    val verticalDrag = verticalDrag@ { change: androidx.compose.ui.input.pointer.PointerEvent ->
                        val position = change.changes.firstOrNull()?.position ?: return@verticalDrag
                        if (!isDragging) {
                            val dx = abs(position.x - startPosition.x)
                            val dy = abs(position.y - startPosition.y)
                            if (dy > 10f && dy > dx) {
                                isDragging = true
                                val isLeftSide = startPosition.x < size.width / 2f
                                dragType = if (isLeftSide) DragType.BRIGHTNESS else DragType.VOLUME
                                startValue = if (isLeftSide) state.brightness else state.volume
                            }
                        }
                        if (isDragging && (dragType == DragType.VOLUME || dragType == DragType.BRIGHTNESS) && size.height > 0) {
                            val dy = -(position.y - startPosition.y)
                            val delta = (dy / size.height) * 2f
                            val newValue = (startValue + delta).coerceIn(0f, 1f)
                            when (dragType) {
                                DragType.BRIGHTNESS -> onBrightnessChanged(newValue)
                                DragType.VOLUME -> onVolumeChanged(newValue)
                                else -> {}
                            }
                        }
                    }

                    val longPressJob = scope.launch {
                        delay(500)
                        if (!isDragging) {
                            isLongPressing = true
                            onLongPressStart()
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (change.pressed) {
                            horizontalDrag(event)
                            verticalDrag(event)
                            if (isDragging) {
                                longPressJob.cancel()
                            }
                        } else {
                            longPressJob.cancel()
                            if (isLongPressing) {
                                isLongPressing = false
                                onLongPressEnd()
                            }
                            break
                        }

                        if (change.positionChange() != Offset.Zero) change.consume()
                    }
                }
            }
        }
}

/**
 * 拖拽类型枚举
 */
private enum class DragType {
    NONE,
    PROGRESS,
    VOLUME,
    BRIGHTNESS
}
