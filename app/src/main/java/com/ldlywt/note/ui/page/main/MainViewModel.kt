package com.ldlywt.note.ui.page.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.biometric.AppBioMetricManager
import com.ldlywt.note.biometric.BiometricAuthListener
import com.ldlywt.note.utils.SharedPreferencesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appBioMetricManager: AppBioMetricManager
) : ViewModel() {

    private val _initAuth = MutableStateFlow(false)
    val initAuth: StateFlow<Boolean> = _initAuth.asStateFlow()

    private val _finishActivity = MutableStateFlow(false)
    val finishActivity: StateFlow<Boolean> = _finishActivity.asStateFlow()

    private val _showBioMetric = MutableStateFlow(true)
    val showBioMetric: StateFlow<Boolean> = _showBioMetric.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (SharedPreferencesUtils.useSafe.first() && appBioMetricManager.canAuthenticate()) {
                _initAuth.emit(true)
            } else {
                _showBioMetric.emit(false)
            }
        }
    }

    fun showBiometricPrompt(mainActivity: MainActivity, success: (Boolean) -> Unit) {
        appBioMetricManager.initBiometricPrompt(activity = mainActivity, listener = object : BiometricAuthListener {
            override fun onBiometricAuthSuccess() {
                viewModelScope.launch {
                    _showBioMetric.emit(false)
                    success(true)
                }
            }

            override fun onUserCancelled() {
                finishActivity()
            }

            override fun onErrorOccurred() {
                finishActivity()
            }
        })
    }

    private fun finishActivity() {
        viewModelScope.launch {
            _finishActivity.emit(true)
        }
    }
}