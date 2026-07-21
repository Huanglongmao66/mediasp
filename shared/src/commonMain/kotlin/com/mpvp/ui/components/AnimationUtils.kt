package com.mpvp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DEFAULT_ANIMATION_DURATION = 300
private const val FAST_ANIMATION_DURATION = 150
private const val SLOW_ANIMATION_DURATION = 500

@Composable
fun FadeInTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = durationMillis, easing = LinearOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ScaleInTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    initialScale: Float = 0.9f,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        ) + scaleIn(
            initialScale = initialScale,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = durationMillis / 2)
        ) + scaleOut(
            targetScale = initialScale,
            animationSpec = tween(durationMillis = durationMillis / 2)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideInFromBottomTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = durationMillis / 2)
        ) + slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = durationMillis / 2)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideInFromRightTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        ) + slideInHorizontally(
            initialOffsetX = { it / 4 },
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = durationMillis / 2)
        ) + slideOutHorizontally(
            targetOffsetX = { it / 4 },
            animationSpec = tween(durationMillis = durationMillis / 2)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ListItemAnimated(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItem: Int = 50,
    initialOffsetY: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * delayPerItem.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = DEFAULT_ANIMATION_DURATION)
        ) + slideInVertically(
            initialOffsetY = { with(density) { initialOffsetY.roundToPx() } },
            animationSpec = tween(durationMillis = DEFAULT_ANIMATION_DURATION, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = FAST_ANIMATION_DURATION)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun PressableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    minScale: Float = 0.95f,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) minScale else 1f,
        animationSpec = tween(durationMillis = FAST_ANIMATION_DURATION)
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        content()
    }
}

@Composable
fun HoverCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    hoverScale: Float = 1.02f,
    content: @Composable () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) hoverScale else 1f,
        animationSpec = tween(durationMillis = FAST_ANIMATION_DURATION)
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = shape,
        color = backgroundColor,
        tonalElevation = if (isHovered) 8.dp else 2.dp
    ) {
        content()
    }
}

@Composable
fun BreatheAnimation(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    minAlpha: Float = 0.5f,
    size: Dp = 64.dp,
    durationMillis: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SpinAnimation(
    icon: ImageVector = Icons.Filled.Refresh,
    contentDescription: String? = "加载中",
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    durationMillis: Int = 1000,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "spin_rotation"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation },
        tint = color
    )
}

@Composable
fun PulsingDot(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    durationMillis: Int = 1200
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha)
            .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(durationMillis = durationMillis / 2)
            ) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = durationMillis / 2)
            ) togetherWith fadeOut(
                animationSpec = tween(durationMillis = durationMillis / 2)
            ) + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = durationMillis / 2)
            )
        },
        modifier = modifier,
        label = "counter"
    ) { targetCount ->
        Text(
            text = targetCount.toString(),
            style = textStyle
        )
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    durationMillis: Int = 1500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = alpha),
                shape = MaterialTheme.shapes.medium
            )
    )
}

@Composable
fun ExpandableSection(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(
            animationSpec = tween(durationMillis = durationMillis)
        ) + expandVertically(
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = durationMillis / 2)
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = durationMillis / 2)
        ),
        modifier = modifier
    ) {
        content()
    }
}

private fun expandVertically(
    animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> = tween()
): androidx.compose.animation.EnterTransition {
    return androidx.compose.animation.expandVertically(
        animationSpec = animationSpec,
        expandFrom = Alignment.Top
    )
}

private fun shrinkVertically(
    animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> = tween()
): androidx.compose.animation.ExitTransition {
    return androidx.compose.animation.shrinkVertically(
        animationSpec = animationSpec,
        shrinkTowards = Alignment.Top
    )
}