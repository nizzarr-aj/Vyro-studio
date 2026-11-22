package com.example.ui.screens.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.BitratePreset
import com.example.core.model.ExportJobState
import com.example.core.model.ResolutionPreset
import com.example.core.model.VideoCodecOption
import com.example.core.model.VideoProject
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import com.example.media.engine.Media3TransformerExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExportUiState(
    val project: VideoProject? = null,
    val resolution: ResolutionPreset = ResolutionPreset.FHD_1080,
    val fps: Int = 30,
    val codec: VideoCodecOption = VideoCodecOption.H264,
    val bitrate: BitratePreset = BitratePreset.HIGH,
    val includeAudio: Boolean = true,
    val jobState: ExportJobState = ExportJobState.Idle
)

class ExportViewModel(
    projectId: Long,
    projectRepository: ProjectRepository,
    preferencesRepository: UserPreferencesRepository,
    context: Context
) : ViewModel() {

    private val exporter = Media3TransformerExporter(context, viewModelScope)

    private val resolutionFlow = MutableStateFlow(ResolutionPreset.FHD_1080)
    private val fpsFlow = MutableStateFlow(30)
    private val codecFlow = MutableStateFlow(VideoCodecOption.H264)
    private val bitrateFlow = MutableStateFlow(BitratePreset.HIGH)
    private val includeAudioFlow = MutableStateFlow(true)
    private var initializedFromProject = false

    val uiState: StateFlow<ExportUiState> = combine(
        projectRepository.observeProject(projectId),
        resolutionFlow,
        fpsFlow,
        codecFlow,
        bitrateFlow,
        includeAudioFlow,
        exporter.exportState
    ) { args ->
        val project = args[0] as VideoProject?
        val res = args[1] as ResolutionPreset
        val fps = args[2] as Int
        val codec = args[3] as VideoCodecOption
        val bitrate = args[4] as BitratePreset
        val audio = args[5] as Boolean
        val job = args[6] as ExportJobState

        if (project != null && !initializedFromProject) {
            initializedFromProject = true
            resolutionFlow.value = project.resolution
            fpsFlow.value = project.fps
            includeAudioFlow.value = !project.isMuted
        }

        ExportUiState(
            project = project,
            resolution = res,
            fps = fps,
            codec = codec,
            bitrate = bitrate,
            includeAudio = audio,
            jobState = job
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExportUiState()
    )

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            if (prefs.preferHevc) {
                codecFlow.value = VideoCodecOption.H265
            }
        }
    }

    fun selectResolution(resolution: ResolutionPreset) {
        resolutionFlow.value = resolution
    }

    fun selectFps(fps: Int) {
        fpsFlow.value = fps
    }

    fun selectCodec(codec: VideoCodecOption) {
        codecFlow.value = codec
    }

    fun selectBitrate(bitrate: BitratePreset) {
        bitrateFlow.value = bitrate
    }

    fun setIncludeAudio(include: Boolean) {
        includeAudioFlow.value = include
    }

    fun startExport() {
        val state = uiState.value
        val project = state.project ?: return
        exporter.startExport(
            project = project,
            resolution = state.resolution,
            fps = state.fps,
            codec = state.codec,
            bitrate = state.bitrate,
            includeAudio = state.includeAudio
        )
    }

    fun cancelExport() {
        exporter.cancelExport()
    }

    fun resetJob() {
        exporter.resetState()
    }

    override fun onCleared() {
        exporter.cancelExport()
        super.onCleared()
    }

    class Factory(
        private val projectId: Long,
        private val projectRepository: ProjectRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExportViewModel(projectId, projectRepository, preferencesRepository, context) as T
        }
    }
}
