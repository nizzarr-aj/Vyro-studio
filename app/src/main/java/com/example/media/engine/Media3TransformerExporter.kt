package com.example.media.engine

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.core.model.BitratePreset
import com.example.core.model.ExportJobState
import com.example.core.model.ResolutionPreset
import com.example.core.model.VideoCodecOption
import com.example.core.model.VideoProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
class Media3TransformerExporter(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _exportState = MutableStateFlow<ExportJobState>(ExportJobState.Idle)
    val exportState: StateFlow<ExportJobState> = _exportState.asStateFlow()

    private var activeTransformer: Transformer? = null
    private var progressJob: Job? = null

    fun startExport(
        project: VideoProject,
        resolution: ResolutionPreset,
        fps: Int,
        codec: VideoCodecOption,
        bitrate: BitratePreset,
        includeAudio: Boolean
    ) {
        if (project.clips.isEmpty()) {
            _exportState.value = ExportJobState.Failed("No clips in timeline to render.")
            return
        }

        cancelExport()
        _exportState.value = ExportJobState.Preparing

        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeTitle = project.title.replace(Regex("[^A-Za-z0-9_-]"), "_").take(24)
            .ifEmpty { "VYRO" }
        val outputFile = File(exportDir, "${safeTitle}_$stamp.mp4")
        if (outputFile.exists()) outputFile.delete()

        val startedAt = System.currentTimeMillis()
        val videoEffects = Media3EffectMapper.toMedia3Effects(project.appliedEffects) +
            listOf(Presentation.createForHeight(resolution.shortEdge))

        val editedItems = project.clips.map { clip ->
            val mediaItemBuilder = MediaItem.Builder()
                .setUri(Uri.parse(clip.uriString))

            if (clip.isVideo) {
                mediaItemBuilder.setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.trimStartMs.coerceAtLeast(0L))
                        .setEndPositionMs(clip.trimEndMs.coerceAtLeast(clip.trimStartMs + 500L))
                        .build()
                )
            } else {
                mediaItemBuilder.setImageDurationMs(clip.effectiveDurationMs.coerceAtLeast(1000L))
            }

            EditedMediaItem.Builder(mediaItemBuilder.build())
                .setRemoveAudio(!includeAudio || project.isMuted || !clip.isVideo)
                .setFrameRate(fps)
                .setEffects(Effects(emptyList(), videoEffects))
                .build()
        }

        val sequence = EditedMediaItemSequence.withAudioAndVideoFrom(editedItems)
        val composition = Composition.Builder(sequence).build()

        val transformer = Transformer.Builder(context.applicationContext)
            .setVideoMimeType(codec.mimeType)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    progressJob?.cancel()
                    val size = if (outputFile.exists()) outputFile.length() else 0L
                    _exportState.value = ExportJobState.Completed(
                        outputPath = outputFile.absolutePath,
                        sizeBytes = size
                    )
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    progressJob?.cancel()
                    _exportState.value = ExportJobState.Failed(
                        exportException.localizedMessage ?: "Media3 Transformer export failed."
                    )
                }
            })
            .build()

        activeTransformer = transformer
        _exportState.value = ExportJobState.Exporting(progressPercent = 0, elapsedMs = 0L)

        scope.launch(Dispatchers.Main) {
            try {
                transformer.start(composition, outputFile.absolutePath)
                startProgressPolling(transformer, startedAt)
            } catch (t: Throwable) {
                _exportState.value = ExportJobState.Failed(
                    t.localizedMessage ?: "Unable to start Media3 Transformer."
                )
            }
        }
    }

    private fun startProgressPolling(transformer: Transformer, startedAt: Long) {
        progressJob?.cancel()
        val holder = ProgressHolder()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                val state = transformer.getProgress(holder)
                val current = _exportState.value
                if (current !is ExportJobState.Exporting) break
                val progress = if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    holder.progress.coerceIn(0, 99)
                } else {
                    current.progressPercent
                }
                _exportState.value = ExportJobState.Exporting(
                    progressPercent = progress,
                    elapsedMs = System.currentTimeMillis() - startedAt
                )
                delay(200L)
            }
        }
    }

    fun cancelExport() {
        progressJob?.cancel()
        progressJob = null
        try {
            activeTransformer?.cancel()
        } catch (_: Exception) {
        }
        activeTransformer = null
        if (_exportState.value is ExportJobState.Exporting || _exportState.value is ExportJobState.Preparing) {
            _exportState.value = ExportJobState.Idle
        }
    }

    fun resetState() {
        _exportState.value = ExportJobState.Idle
    }
}
