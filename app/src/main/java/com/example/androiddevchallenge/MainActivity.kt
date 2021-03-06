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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.androiddevchallenge.ui.theme.MyTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

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
    val scope = rememberCoroutineScope()
    val state = remember { TimerState(scope) }

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
                    state = state,
                    startColor = MaterialTheme.colors.secondary,
                    endColor = MaterialTheme.colors.primary,
                    disabledColor = MaterialTheme.colors.onBackground.copy(alpha = .1f),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(56.dp)
                        .align(Alignment.TopCenter)
                ) {
                    TimerText(
                        text = state.timeInSeconds.toString(),
                        isTimerRunning = state.isRunning
                    )
                }

                ControlPanel(
                    isTimerRunning = state.isRunning,
                    showReset = state.isResetVisible,
                    onToggleClick = state::toggleTimer,
                    onResetClick = state::reset,
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
    showReset: Boolean,
    onToggleClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(showReset) {
            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
            ) {
                Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = "Reset")
            }
        }

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
