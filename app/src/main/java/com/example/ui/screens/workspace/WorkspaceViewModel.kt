package com.example.ui.screens.workspace

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.TimelineClip
import com.example.core.model.VideoProject
import com.example.data.repository.ProjectRepository
import com.example.media.engine.Media3PreviewController
import com.example.media.engine.PreviewPlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkspaceToolPanel {
    NONE,
    TRIM,
    SPEED
}

data class WorkspaceUiState(
    val project: VideoProject? = null,
    val selectedClipId: Long? = null,
    val activePanel: WorkspaceToolPanel = WorkspaceToolPanel.NONE,
    val playback: PreviewPlaybackState = PreviewPlaybackState()
) {
    val selectedClip: TimelineClip?
        get() = project?.clips?.firstOrNull { it.id == selectedClipId }
            ?: project?.clips?.firstOrNull()
}

class WorkspaceViewModel(
    private val projectId: Long,
    private val projectRepository: ProjectRepository,
    context: Context
) : ViewModel() {

    val previewController = Media3PreviewController(context, viewModelScope)

    private val selectedClipIdFlow = MutableStateFlow<Long?>(null)
    private val activePanelFlow = MutableStateFlow(WorkspaceToolPanel.NONE)

    val uiState: StateFlow<WorkspaceUiState> = combine(
        projectRepository.observeProject(projectId),
        selectedClipIdFlow,
        activePanelFlow,
        previewController.playbackState
    ) { project, selectedId, panel, playback ->
        if (project != null) {
            previewController.bindProject(project)
        }
        val resolvedSelected = selectedId?.takeIf { id ->
            project?.clips?.any { it.id == id } == true
        } ?: project?.clips?.firstOrNull()?.id

        WorkspaceUiState(
            project = project,
            selectedClipId = resolvedSelected,
            activePanel = panel,
            playback = playback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkspaceUiState()
    )

    fun selectClip(clipId: Long) {
        selectedClipIdFlow.value = clipId
    }

    fun togglePanel(panel: WorkspaceToolPanel) {
        activePanelFlow.value = if (activePanelFlow.value == panel) {
            WorkspaceToolPanel.NONE
        } else {
            panel
        }
    }

    fun togglePlayPause() = previewController.togglePlayPause()

    fun seekTo(ms: Long) = previewController.seekToGlobalPosition(ms)

    fun stepBy(deltaMs: Long) = previewController.stepBy(deltaMs)

    fun toggleMute() {
        val project = uiState.value.project ?: return
        viewModelScope.launch {
            projectRepository.setMuted(project.id, !project.isMuted)
        }
    }

    fun adjustTrimStart(deltaMs: Long) {
        val clip = uiState.value.selectedClip ?: return
        if (!clip.isVideo) return
        val newStart = (clip.trimStartMs + deltaMs)
            .coerceIn(0L, (clip.trimEndMs - 1000L).coerceAtLeast(0L))
        viewModelScope.launch {
            projectRepository.updateClip(clip.copy(trimStartMs = newStart))
        }
    }

    fun adjustTrimEnd(deltaMs: Long) {
        val clip = uiState.value.selectedClip ?: return
        val maxDur = if (clip.isVideo) clip.durationMs else 30_000L
        val newEnd = (clip.trimEndMs + deltaMs)
            .coerceIn(clip.trimStartMs + 1000L, maxDur)
        viewModelScope.launch {
            projectRepository.updateClip(
                clip.copy(
                    trimEndMs = newEnd,
                    durationMs = if (clip.isVideo) clip.durationMs else newEnd
                )
            )
        }
    }

    fun resetTrim() {
        val clip = uiState.value.selectedClip ?: return
        val defaultEnd = if (clip.isVideo) clip.durationMs else 4_000L
        viewModelScope.launch {
            projectRepository.updateClip(
                clip.copy(
                    trimStartMs = 0L,
                    trimEndMs = defaultEnd,
                    durationMs = defaultEnd,
                    playbackSpeed = 1.0f
                )
            )
        }
    }

    fun setClipSpeed(speed: Float) {
        val clip = uiState.value.selectedClip ?: return
        viewModelScope.launch {
            projectRepository.updateClip(clip.copy(playbackSpeed = speed))
        }
    }

    fun removeSelectedClip() {
        val project = uiState.value.project ?: return
        val clip = uiState.value.selectedClip ?: return
        viewModelScope.launch {
            projectRepository.removeClip(project.id, clip.id)
        }
    }

    override fun onCleared() {
        previewController.release()
        super.onCleared()
    }

    class Factory(
        private val projectId: Long,
        private val projectRepository: ProjectRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkspaceViewModel(projectId, projectRepository, context) as T
        }
    }
}
