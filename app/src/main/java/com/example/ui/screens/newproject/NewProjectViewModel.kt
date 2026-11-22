package com.example.ui.screens.newproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.ResolutionPreset
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class NewProjectUiState(
    val title: String = "",
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.RATIO_9_16,
    val fps: Int = 30,
    val resolution: ResolutionPreset = ResolutionPreset.FHD_1080,
    val colorProfile: ColorProfile = ColorProfile.REC_709,
    val showTitleError: Boolean = false,
    val isCreating: Boolean = false
)

class NewProjectViewModel(
    private val projectRepository: ProjectRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewProjectUiState())
    val uiState: StateFlow<NewProjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            _uiState.value = _uiState.value.copy(
                fps = prefs.defaultFps,
                resolution = prefs.defaultResolution
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title, showTitleError = false)
    }

    fun onAspectRatioSelected(aspectRatio: AspectRatioPreset) {
        _uiState.value = _uiState.value.copy(aspectRatio = aspectRatio)
    }

    fun onFpsSelected(fps: Int) {
        _uiState.value = _uiState.value.copy(fps = fps)
    }

    fun onResolutionSelected(resolution: ResolutionPreset) {
        _uiState.value = _uiState.value.copy(resolution = resolution)
    }

    fun onColorProfileSelected(colorProfile: ColorProfile) {
        _uiState.value = _uiState.value.copy(colorProfile = colorProfile)
    }

    fun createProject(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(showTitleError = true)
            return
        }
        if (state.isCreating) return
        _uiState.value = state.copy(isCreating = true)

        viewModelScope.launch {
            val projectId = projectRepository.createProject(
                title = state.title,
                aspectRatio = state.aspectRatio,
                fps = state.fps,
                resolution = state.resolution,
                colorProfile = state.colorProfile
            )
            onCreated(projectId)
        }
    }

    class Factory(
        private val projectRepository: ProjectRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewProjectViewModel(projectRepository, preferencesRepository) as T
        }
    }
}
