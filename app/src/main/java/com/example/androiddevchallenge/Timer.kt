/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Timer(
    state: TimerState,
    startColor: Color,
    endColor: Color,
    disabledColor: Color,
    modifier: Modifier = Modifier,
    numberOfTicks: Long = TimerState.TIME_LIMIT,
    content: @Composable BoxScope.() -> Unit
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    var position by remember { mutableStateOf(Offset.Zero) }
    var touchInProgress by remember { mutableStateOf(false) }

    val animatedProgress = remember { Animatable(state.progress) }

    LaunchedEffect(state.progress) {
        if (touchInProgress) {
            animatedProgress.snapTo(
                targetValue = state.progress
            )
        } else {
            animatedProgress.animateTo(
                targetValue = state.progress,
                animationSpec = tween(300)
            )
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { origin = it.center.toOffset() }
            .circleProgress(
                startColor,
                endColor,
                disabledColor,
                animatedProgress.value,
                numberOfTicks
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchInProgress = true
                        position = offset

                        state.setTimeByPosition(origin, position)
                    },
                    onDragEnd = {
                        touchInProgress = false
                    },
                    onDragCancel = {
                        touchInProgress = false
                    },
                    onDrag = { change, amount ->
                        if (state.isRunning) return@detectDragGestures

                        position += amount
                        state.setTimeByPosition(origin, position)

                        change.consumeAllChanges()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TimerText(text: String, isTimerRunning: Boolean, modifier: Modifier = Modifier) {
    val timeTextScale = remember { Animatable(1f) }

    LaunchedEffect(text) {
        if (isTimerRunning) {
            // Didn't work with the repeatable :/. Maybe I need to try something different than Animatable
            timeTextScale.animateTo(
                1.5f,
                animationSpec = tween(150)
            )
            timeTextScale.animateTo(
                1f,
                animationSpec = tween(150)
            )
        }
    }

    Text(
        text = text,
        modifier = modifier.scale(timeTextScale.value),
        style = MaterialTheme.typography.h2,
        color = MaterialTheme.colors.onBackground
    )
}

private fun Modifier.circleProgress(
    startColor: Color,
    endColor: Color,
    disabledColor: Color,
    progress: Float,
    numberOfTicks: Long,
    strokeWidth: Dp = 4.dp
) = drawWithCache {
    // For the next time I would choose the way to calculate the color for each step (instead of using gradient)
    val brush = Brush.sweepGradient(
        0f to startColor,
        0.3f to startColor,
        0.99f to endColor,
        1f to startColor,
        center = size.center
    )

    // Math ... It works! Somehow...
    val circleSize = 360
    val stepSize = circleSize / numberOfTicks
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val centerOffset = size.width / 3
    val radius = min(size.center.x, size.center.y)

    val totalNumberOfTicks = circleSize / stepSize
    val numberOfTicksToShow = progress * totalNumberOfTicks

    onDrawBehind {
        // Need to rotate the canvas, because it's impossible to rotate the gradient only
        rotate(-90f) {

            for (i in 0 until totalNumberOfTicks) {
                val currentAngle = i * stepSize

                val cos = cos(currentAngle * PI / 180)
                val sin = sin(currentAngle * PI / 180)

                val startX = cos * centerOffset
                val startY = sin * centerOffset

                val targetProgress = (numberOfTicksToShow - i).coerceIn(0f, 1f)
                val targetRadius = centerOffset + (radius - centerOffset) * targetProgress

                val targetX = cos * targetRadius
                val targetY = sin * targetRadius

                if (targetProgress < 1f) {
                    drawLine(
                        color = disabledColor,
                        start = Offset(centerX + startX.toFloat(), centerY + startY.toFloat()),
                        end = Offset(
                            centerX + (cos * radius).toFloat(),
                            centerY + (sin * radius).toFloat()
                        ),
                        cap = StrokeCap.Round,
                        strokeWidth = strokeWidth.toPx() / 2
                    )
                }

                if (targetProgress > 0f) {
                    drawLine(
                        brush = brush,
                        start = Offset(centerX + startX.toFloat(), centerY + startY.toFloat()),
                        end = Offset(centerX + targetX.toFloat(), centerY + targetY.toFloat()),
                        cap = StrokeCap.Round,
                        strokeWidth = strokeWidth.toPx()
                    )
                }
            }
        }
    }
}
