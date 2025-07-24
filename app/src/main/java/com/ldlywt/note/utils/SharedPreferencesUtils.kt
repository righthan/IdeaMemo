package com.ldlywt.note.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ldlywt.note.App
import com.ldlywt.note.ui.page.SortTime
import kotlinx.coroutines.flow.Flow

private const val SHARED_PREFERENCES_STORE_NAME = "SHARED_PREFERENCES"
private val Context.sharedDataStore by preferencesDataStore(name = SHARED_PREFERENCES_STORE_NAME)

object SharedPreferencesUtils {
    private object PreferencesKeys {
        val SORT_TIME = stringPreferencesKey("sort_time")
        val USE_SAFE = booleanPreferencesKey("use_safe")
        val LOCAL_AUTO_BACKUP = booleanPreferencesKey("local_auto_backup")
        val LOCAL_BACKUP_URI = stringPreferencesKey("local_backup_uri")
        val DAV_LOGIN_SUCCESS = booleanPreferencesKey("dav_login_success")
        val DAV_SERVER_URL = stringPreferencesKey("dav_server_url")
        val DAV_USER_NAME = stringPreferencesKey("dav_user_name")
        val DAV_PASSWORD = stringPreferencesKey("dav_password")
        val MEMOS_LOGIN_SUCCESS = booleanPreferencesKey("memos_login_success")
        val MEMOS_SERVER_URL = stringPreferencesKey("memos_server_url")
        val MEMOS_USER_SESSION = stringPreferencesKey("memos_user_session")
        val MEMOS_USER_NAME = stringPreferencesKey("memos_user_name")
        val MEMOS_USERNAME = stringPreferencesKey("memos_username")
        val MEMOS_USER_DISPLAY_NAME = stringPreferencesKey("memos_user_display_name")
        val MEMOS_USER_ROLE = stringPreferencesKey("memos_user_role")
        val MEMOS_AVATAR_URL = stringPreferencesKey("memos_avatar_url")
        val MEMOS_TAG_COUNT = stringPreferencesKey("memos_tag_count")
        val MEMOS_TOTAL_MEMO_COUNT = stringPreferencesKey("memos_total_memo_count")
        val MEMOS_DISPLAY_TIMESTAMPS = stringPreferencesKey("memos_display_timestamps")
    }

    private val sharedPreferences = App.instance.sharedDataStore


    val sortTime: Flow<SortTime> = sharedPreferences.getEnum(PreferencesKeys.SORT_TIME, SortTime.UPDATE_TIME_DESC)
    val useSafe: Flow<Boolean> = sharedPreferences.getBoolean(PreferencesKeys.USE_SAFE, false)


    val localAutoBackup: Flow<Boolean> = sharedPreferences.getBoolean(PreferencesKeys.LOCAL_AUTO_BACKUP, false)

    // content://com.android.externalstorage.documents/tree/primary%3ADocuments
    val localBackupUri: Flow<String?> = sharedPreferences.getString(PreferencesKeys.LOCAL_BACKUP_URI, null)
    val davLoginSuccess: Flow<Boolean>  = sharedPreferences.getBoolean(PreferencesKeys.DAV_LOGIN_SUCCESS, false)
    val davServerUrl: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_SERVER_URL, "https://dav.jianguoyun.com/dav/")
    val davUserName: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_USER_NAME, null)
    val davPassword: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_PASSWORD, null)
    
    val memosLoginSuccess: Flow<Boolean> = sharedPreferences.getBoolean(PreferencesKeys.MEMOS_LOGIN_SUCCESS, false)
    val memosServerUrl: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_SERVER_URL, "")
    val memosUserSession: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_USER_SESSION, null)
    val memosUserName: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_USER_NAME, null)
    val memosUsername: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_USERNAME, null)
    val memosUserDisplayName: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_USER_DISPLAY_NAME, null)
    val memosUserRole: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_USER_ROLE, null)
    val memosAvatarUrl: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_AVATAR_URL, null)
    
    val memosTagCount: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_TAG_COUNT, null)
    val memosTotalMemoCount: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_TOTAL_MEMO_COUNT, null)
    val memosDisplayTimestamps: Flow<String?> = sharedPreferences.getString(PreferencesKeys.MEMOS_DISPLAY_TIMESTAMPS, null)


    suspend fun clearDavConfig() {
        sharedPreferences.edit { preferences ->
            preferences[PreferencesKeys.DAV_LOGIN_SUCCESS] = false
            preferences.remove(PreferencesKeys.DAV_SERVER_URL)
            preferences.remove(PreferencesKeys.DAV_USER_NAME)
            preferences.remove(PreferencesKeys.DAV_PASSWORD)

        }
    }

    suspend fun clearMemosConfig() {
        sharedPreferences.edit { preferences ->
            preferences[PreferencesKeys.MEMOS_LOGIN_SUCCESS] = false
            preferences.remove(PreferencesKeys.MEMOS_SERVER_URL)
            preferences.remove(PreferencesKeys.MEMOS_USER_SESSION)
            preferences.remove(PreferencesKeys.MEMOS_USER_NAME)
            preferences.remove(PreferencesKeys.MEMOS_USERNAME)
            preferences.remove(PreferencesKeys.MEMOS_USER_DISPLAY_NAME)
            preferences.remove(PreferencesKeys.MEMOS_USER_ROLE)
            preferences.remove(PreferencesKeys.MEMOS_AVATAR_URL)
            preferences.remove(PreferencesKeys.MEMOS_TAG_COUNT)
            preferences.remove(PreferencesKeys.MEMOS_TOTAL_MEMO_COUNT)
            preferences.remove(PreferencesKeys.MEMOS_DISPLAY_TIMESTAMPS)
        }
    }

    private suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T?) {
        sharedPreferences.edit { preferences ->
            if(value!=null) {
                preferences[key] = value
            }else{
                preferences.remove(key)
            }
        }
    }

    suspend fun updateLocalBackUri(uri: String?){
        updatePreference(PreferencesKeys.LOCAL_BACKUP_URI,uri)
    }
    suspend fun updateLocalAutoBackup(use: Boolean) {
        updatePreference(PreferencesKeys.LOCAL_AUTO_BACKUP, use)
    }
    suspend fun updateDavLoginSuccess(success: Boolean) {
        updatePreference(PreferencesKeys.DAV_LOGIN_SUCCESS, success)
    }

    suspend fun updateDavServerUrl(uri: String) {
        updatePreference(PreferencesKeys.DAV_SERVER_URL, uri)
    }

    suspend fun updateDavUserName(name:String? ) {
        updatePreference(PreferencesKeys.DAV_USER_NAME, name)
    }
    suspend fun updateDavPassword(password:String? ) {
        updatePreference(PreferencesKeys.DAV_PASSWORD, password)
    }

    suspend fun updateSortTime(sortTime: SortTime) {
        updatePreference(PreferencesKeys.SORT_TIME, sortTime.name)
    }
    suspend fun updateUseSafe(use: Boolean) {
        updatePreference(PreferencesKeys.USE_SAFE, use)
    }

    suspend fun updateMemosLoginSuccess(success: Boolean) {
        updatePreference(PreferencesKeys.MEMOS_LOGIN_SUCCESS, success)
    }

    suspend fun updateMemosServerUrl(url: String?) {
        updatePreference(PreferencesKeys.MEMOS_SERVER_URL, url)
    }

    suspend fun updateMemosUserSession(session: String?) {
        updatePreference(PreferencesKeys.MEMOS_USER_SESSION, session)
    }

    suspend fun updateMemosUserName(userName: String?) {
        updatePreference(PreferencesKeys.MEMOS_USER_NAME, userName)
    }

    suspend fun updateMemosUserDisplayName(displayName: String?) {
        updatePreference(PreferencesKeys.MEMOS_USER_DISPLAY_NAME, displayName)
    }

    suspend fun updateMemosUserRole(role: String?) {
        updatePreference(PreferencesKeys.MEMOS_USER_ROLE, role)
    }

    suspend fun updateMemosUsername(username: String?) {
        updatePreference(PreferencesKeys.MEMOS_USERNAME, username)
    }

    suspend fun updateMemosAvatarUrl(avatarUrl: String?) {
        updatePreference(PreferencesKeys.MEMOS_AVATAR_URL, avatarUrl)
    }
    
    suspend fun updateMemosTagCount(tagCountJson: String?) {
        updatePreference(PreferencesKeys.MEMOS_TAG_COUNT, tagCountJson)
    }
    
    suspend fun updateMemosTotalMemoCount(totalCount: Int) {
        updatePreference(PreferencesKeys.MEMOS_TOTAL_MEMO_COUNT, totalCount.toString())
    }
    
    suspend fun updateMemosDisplayTimestamps(timestampsJson: String?) {
        updatePreference(PreferencesKeys.MEMOS_DISPLAY_TIMESTAMPS, timestampsJson)
    }


}