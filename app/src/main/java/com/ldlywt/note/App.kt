package com.ldlywt.note

import android.app.Application
import androidx.lifecycle.asLiveData
import com.ldlywt.note.backup.BackupScheduler
import com.ldlywt.note.utils.SettingsPreferences
import com.ldlywt.note.utils.SharedPreferencesUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun getAppName(): String {
    return "IdeaMemo"
}


@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        val localAutoBackup = SharedPreferencesUtils.localAutoBackup.asLiveData().value
        if (localAutoBackup == true) {
            BackupScheduler.scheduleDailyBackup(this)
        } else {
            BackupScheduler.cancelDailyBackup(this)
        }

        GlobalScope.launch(Dispatchers.Main) {
            SettingsPreferences.themeMode.collect {
                SettingsPreferences.applyAppCompatThemeMode(it)
            }
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}