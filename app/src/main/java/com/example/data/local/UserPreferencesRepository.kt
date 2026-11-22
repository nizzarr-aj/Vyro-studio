package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.AppLanguage
import com.example.core.model.ResolutionPreset
import com.example.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vyroPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "vyro_studio_prefs")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val CREATOR_NAME = stringPreferencesKey("creator_name")
        val CREATOR_HANDLE = stringPreferencesKey("creator_handle")
        val LANGUAGE = stringPreferencesKey("language_code")
        val DEFAULT_RESOLUTION = stringPreferencesKey("default_resolution")
        val DEFAULT_FPS = intPreferencesKey("default_fps")
        val PREFER_HEVC = booleanPreferencesKey("prefer_hevc")
    }

    val preferences: Flow<UserPreferences> = context.vyroPrefsDataStore.data.map { prefs ->
        UserPreferences(
            creatorName = prefs[Keys.CREATOR_NAME] ?: "Alex Mercer",
            creatorHandle = prefs[Keys.CREATOR_HANDLE] ?: "@vyro.director",
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "en"),
            defaultResolution = ResolutionPreset.fromId(prefs[Keys.DEFAULT_RESOLUTION] ?: "1080p"),
            defaultFps = prefs[Keys.DEFAULT_FPS] ?: 30,
            preferHevc = prefs[Keys.PREFER_HEVC] ?: false
        )
    }

    suspend fun updateProfile(name: String, handle: String) {
        context.vyroPrefsDataStore.edit { prefs ->
            prefs[Keys.CREATOR_NAME] = name.trim().ifEmpty { "Alex Mercer" }
            prefs[Keys.CREATOR_HANDLE] = handle.trim().ifEmpty { "@vyro.director" }
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.vyroPrefsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setDefaultResolution(resolution: ResolutionPreset) {
        context.vyroPrefsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_RESOLUTION] = resolution.id
        }
    }

    suspend fun setDefaultFps(fps: Int) {
        context.vyroPrefsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_FPS] = fps
        }
    }

    suspend fun setPreferHevc(preferHevc: Boolean) {
        context.vyroPrefsDataStore.edit { prefs ->
            prefs[Keys.PREFER_HEVC] = preferHevc
        }
    }
}
