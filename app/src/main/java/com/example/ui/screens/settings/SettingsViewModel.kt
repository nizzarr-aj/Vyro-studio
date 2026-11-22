package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AppLanguage
import com.example.core.model.ResolutionPreset
import com.example.core.model.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val projectCount: Int = 0,
    val exportsBytes: Long = 0L,
    val profileSavedFeedback: Boolean = false
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    projectRepository: ProjectRepository,
    private val context: Context
) : ViewModel() {

    private val exportsBytesFlow = MutableStateFlow(computeExportsSizeBytes())
    private val savedFeedbackFlow = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferences,
        projectRepository.projectCount,
        exportsBytesFlow,
        savedFeedbackFlow
    ) { prefs, count, bytes, saved ->
        SettingsUiState(
            preferences = prefs,
            projectCount = count,
            exportsBytes = bytes,
            profileSavedFeedback = saved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun saveProfile(name: String, handle: String) {
        viewModelScope.launch {
            preferencesRepository.updateProfile(name, handle)
            savedFeedbackFlow.value = true
        }
    }

    fun dismissSavedFeedback() {
        savedFeedbackFlow.value = false
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }

    fun setDefaultResolution(resolution: ResolutionPreset) {
        viewModelScope.launch {
            preferencesRepository.setDefaultResolution(resolution)
        }
    }

    fun setDefaultFps(fps: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultFps(fps)
        }
    }

    fun setPreferHevc(preferHevc: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPreferHevc(preferHevc)
        }
    }

    fun clearExports() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "exports")
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
            }
            exportsBytesFlow.value = computeExportsSizeBytes()
        }
    }

    private fun computeExportsSizeBytes(): Long {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) return 0L
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    class Factory(
        private val preferencesRepository: UserPreferencesRepository,
        private val projectRepository: ProjectRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository, projectRepository, context) as T
        }
    }
}
