package com.example.ui.screens.mediaimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.MediaPickCandidate
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import com.example.media.engine.MediaMetadataExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class MediaPickerFilter {
    ALL,
    VIDEO,
    IMAGE
}

data class MediaImportUiState(
    val targetProjectId: Long = -1L,
    val targetProjectTitle: String? = null,
    val filter: MediaPickerFilter = MediaPickerFilter.ALL,
    val queuedMedia: List<MediaPickCandidate> = emptyList(),
    val isExtracting: Boolean = false,
    val isCommitting: Boolean = false
)

class MediaImportViewModel(
    private val projectId: Long,
    private val projectRepository: ProjectRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val metadataExtractor: MediaMetadataExtractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaImportUiState(targetProjectId = projectId))
    val uiState: StateFlow<MediaImportUiState> = _uiState.asStateFlow()

    init {
        if (projectId > 0L) {
            viewModelScope.launch {
                val project = projectRepository.getProjectOnce(projectId)
                _uiState.value = _uiState.value.copy(targetProjectTitle = project?.title)
            }
        }
    }

    fun setFilter(filter: MediaPickerFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun onMediaUrisPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtracting = true)
            val extracted = uris.map { uri -> metadataExtractor.extractMetadata(uri) }
            _uiState.value = _uiState.value.copy(
                queuedMedia = _uiState.value.queuedMedia + extracted,
                isExtracting = false
            )
        }
    }

    fun moveItem(index: Int, delta: Int) {
        val list = _uiState.value.queuedMedia.toMutableList()
        val target = index + delta
        if (index !in list.indices || target !in list.indices) return
        val item = list.removeAt(index)
        list.add(target, item)
        _uiState.value = _uiState.value.copy(queuedMedia = list)
    }

    fun removeItem(index: Int) {
        val list = _uiState.value.queuedMedia.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        _uiState.value = _uiState.value.copy(queuedMedia = list)
    }

    fun commitToTimeline(defaultProjectTitle: String, onCommitted: (Long) -> Unit) {
        val state = _uiState.value
        if (state.queuedMedia.isEmpty() || state.isCommitting) return
        _uiState.value = state.copy(isCommitting = true)

        viewModelScope.launch {
            val resolvedProjectId = if (state.targetProjectId > 0L) {
                state.targetProjectId
            } else {
                val prefs = preferencesRepository.preferences.first()
                val first = state.queuedMedia.first()
                val inferredRatio = if (first.height > first.width) {
                    AspectRatioPreset.RATIO_9_16
                } else {
                    AspectRatioPreset.RATIO_16_9
                }
                projectRepository.createProject(
                    title = defaultProjectTitle,
                    aspectRatio = inferredRatio,
                    fps = prefs.defaultFps,
                    resolution = prefs.defaultResolution,
                    colorProfile = ColorProfile.REC_709,
                    initialMediaUri = first.uriString,
                    initialMediaIsVideo = first.isVideo
                )
            }
            projectRepository.appendPickedMedia(resolvedProjectId, state.queuedMedia)
            onCommitted(resolvedProjectId)
        }
    }

    class Factory(
        private val projectId: Long,
        private val projectRepository: ProjectRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val metadataExtractor: MediaMetadataExtractor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaImportViewModel(
                projectId,
                projectRepository,
                preferencesRepository,
                metadataExtractor
            ) as T
        }
    }
}
