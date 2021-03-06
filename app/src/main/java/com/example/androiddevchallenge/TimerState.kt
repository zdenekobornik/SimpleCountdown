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

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.round

class TimerState(
    private val scope: CoroutineScope
) {
    var isRunning by mutableStateOf(false)
        private set

    var timeInSeconds by mutableStateOf(TIME_LIMIT)
        private set

    val progress by derivedStateOf { timeInSeconds / TIME_LIMIT.toFloat() }
    val isResetVisible by derivedStateOf { timeInSeconds != TIME_LIMIT }

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (!isRunning) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    fun reset() {
        stopTimer()

        timeInSeconds = TIME_LIMIT
    }

    fun setTimeByPosition(center: Offset, position: Offset) {
        val degrees = (
            atan2(
                center.y - position.y.toDouble(),
                center.x - position.x.toDouble()
            ) * 180.0 / PI
            ).toFloat()

        val cleanDegrees = (degrees - 90 + 360) % 360

        timeInSeconds = round(cleanDegrees / (360 / TIME_LIMIT)).toLong()
    }

    private fun startTimer() {
        if (timeInSeconds <= 0) {
            return
        }

        stopTimer()

        isRunning = true

        timerJob = scope.launch {
            while (isActive) {
                delay(1000)

                timeInSeconds -= 1

                if (timeInSeconds <= 0) {
                    stopTimer()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        isRunning = false

        timerJob?.cancel()
    }

    companion object {
        const val TIME_LIMIT = 60L
    }
}
