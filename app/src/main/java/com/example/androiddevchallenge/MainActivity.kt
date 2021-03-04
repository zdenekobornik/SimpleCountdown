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

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.TimerViewModel.Companion.TIME_LIMIT
import com.example.androiddevchallenge.ui.theme.MyTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyTheme {
                ProvideWindowInsets {
                    MyApp()
                }
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    val viewModel: TimerViewModel = viewModel()
    val isTimerRunning by viewModel.isRunning.observeAsState(false)
    val currentTime by viewModel.timeInSeconds.observeAsState(TIME_LIMIT)
    val progress by viewModel.progress.observeAsState(1f)

    val animatedProgress = remember { Animatable(1f) }

    LaunchedEffect(currentTime) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(300)
        )
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = {
                    Text(text = "Simple Countdown", style = MaterialTheme.typography.h6)
                }
            )
        },
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Timer(
                    startColor = MaterialTheme.colors.secondary,
                    endColor = MaterialTheme.colors.primary,
                    disabledColor = MaterialTheme.colors.onBackground.copy(alpha = .1f),
                    progress = animatedProgress.value,
                    numberOfTicks = TIME_LIMIT,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(56.dp)
                        .align(Alignment.TopCenter)
                ) {
                    TimerText(text = DateUtils.formatElapsedTime(currentTime))
                }

                ControlPanel(
                    isTimerRunning = isTimerRunning,
                    onToggleClick = { viewModel.toggleTimer() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ControlPanel(
    isTimerRunning: Boolean,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Button(
            onClick = onToggleClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape
        ) {
            if (isTimerRunning) {
                Icon(imageVector = Icons.Filled.Stop, contentDescription = "Stop")
            } else {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start")
            }
        }
    }
}

@Composable
fun Timer(
    startColor: Color,
    endColor: Color,
    progress: Float,
    disabledColor: Color,
    numberOfTicks: Long,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.circleProgress(
            startColor,
            endColor,
            disabledColor,
            progress,
            numberOfTicks
        ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TimerText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h4,
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
    val brush = Brush.sweepGradient(
        0f to startColor,
        0.3f to startColor,
        0.99f to endColor,
        1f to startColor,
        center = size.center
    )

    val circleSize = 360
    val stepSize = circleSize / numberOfTicks
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val centerOffset = size.width / 3
    val radius = min(size.center.x, size.center.y)

    val totalNumberOfTicks = circleSize / stepSize
    val numberOfTicksToShow = progress * totalNumberOfTicks

    onDrawBehind {
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

/**
 * A wrapper around [TopAppBar] which uses [Modifier.statusBarsPadding] to shift the app bar's
 * contents down, but still draws the background behind the status bar too.
 */
@Composable
fun CustomAppBar(
    title: @Composable () -> Unit,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    elevation: Dp = 4.dp
) {
    Surface(
        color = backgroundColor,
        elevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            title()
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        ProvideWindowInsets {
            MyApp()
        }
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        ProvideWindowInsets {
            MyApp()
        }
    }
}
