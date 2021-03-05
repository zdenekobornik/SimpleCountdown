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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _isRunning = MutableLiveData<Boolean>()
    val isRunning: LiveData<Boolean> get() = _isRunning

    val isResetVisible: LiveData<Boolean> get() = timeInSeconds.map { time ->
        time != TIME_LIMIT
    }

    private val _timeInSeconds = MutableLiveData<Long>()
    val timeInSeconds: LiveData<Long> get() = _timeInSeconds

    val progress: LiveData<Float> = timeInSeconds.map { time ->
        time / TIME_LIMIT.toFloat()
    }

    private var timerJob: Job? = null

    fun toggleTimer() {
        val isCurrentlyRunning = isRunning.value ?: false

        if (!isCurrentlyRunning) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        if (getCurrentTime() <= 0) {
            return
        }

        stopTimer()

        _isRunning.value = true

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)

                val newTime = getCurrentTime() - 1

                _timeInSeconds.value = newTime

                if (newTime <= 0) {
                    stopTimer()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        _isRunning.value = false

        timerJob?.cancel()
    }

    private fun getCurrentTime(): Long {
        return _timeInSeconds.value ?: TIME_LIMIT
    }

    fun reset() {
        stopTimer()

        _timeInSeconds.value = TIME_LIMIT
    }

    fun setTime(time: Int) {
        _timeInSeconds.value = time.toLong()
    }

    companion object {
        const val TIME_LIMIT = 60L
    }
}
