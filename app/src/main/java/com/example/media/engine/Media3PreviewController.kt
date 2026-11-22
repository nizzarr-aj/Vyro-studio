package com.example.media.engine

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.model.TimelineClip
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

data class PreviewPlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val activeClipIndex: Int = 0,
    val hasMedia: Boolean = false
)

@OptIn(UnstableApi::class)
class Media3PreviewController(
    context: Context,
    private val scope: CoroutineScope
) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    private val _playbackState = MutableStateFlow(PreviewPlaybackState())
    val playbackState: StateFlow<PreviewPlaybackState> = _playbackState.asStateFlow()

    private var positionTickerJob: Job? = null
    private var currentClips: List<TimelineClip> = emptyList()
    private var lastSignature: String = ""

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            if (isPlaying) startTicker() else stopTicker()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncPositionNow()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                player.seekTo(0, 0L)
                player.pause()
            }
            syncPositionNow()
        }
    }

    init {
        player.addListener(listener)
    }

    fun bindProject(project: VideoProject) {
        val signature = buildString {
            append(project.id)
            append('|')
            append(project.isMuted)
            append('|')
            project.clips.forEach { c ->
                append("${c.id}:${c.uriString}:${c.trimStartMs}:${c.trimEndMs}:${c.playbackSpeed};")
            }
            append('|')
            project.appliedEffects.forEach { fx ->
                append("${fx.presetId}:${fx.intensity};")
            }
        }

        if (signature == lastSignature) return
        lastSignature = signature

        currentClips = project.clips
        player.volume = if (project.isMuted) 0f else 1f

        val videoEffects = Media3EffectMapper.toMedia3Effects(project.appliedEffects)
        player.setVideoEffects(videoEffects)

        if (project.clips.isEmpty()) {
            player.clearMediaItems()
            _playbackState.value = PreviewPlaybackState(
                isPlaying = false,
                currentPositionMs = 0L,
                totalDurationMs = 0L,
                hasMedia = false
            )
            return
        }

        val mediaItems = project.clips.map { clip ->
            val builder = MediaItem.Builder()
                .setUri(Uri.parse(clip.uriString))
            if (clip.isVideo) {
                builder.setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.trimStartMs.coerceAtLeast(0L))
                        .setEndPositionMs(clip.trimEndMs.coerceAtLeast(clip.trimStartMs + 500L))
                        .build()
                )
            } else {
                builder.setImageDurationMs(clip.effectiveDurationMs.coerceAtLeast(1000L))
            }
            builder.build()
        }

        val firstSpeed = project.clips.firstOrNull()?.playbackSpeed ?: 1.0f
        player.playbackParameters = PlaybackParameters(firstSpeed)
        player.setMediaItems(mediaItems, true)
        player.prepare()

        _playbackState.value = PreviewPlaybackState(
            isPlaying = player.isPlaying,
            currentPositionMs = 0L,
            totalDurationMs = project.totalDurationMs,
            activeClipIndex = 0,
            hasMedia = true
        )
    }

    fun togglePlayPause() {
        if (currentClips.isEmpty()) return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0, 0L)
            }
            player.play()
        }
    }

    fun seekToGlobalPosition(globalMs: Long) {
        if (currentClips.isEmpty()) return
        var remaining = globalMs.coerceIn(0L, currentClips.sumOf { it.effectiveDurationMs })
        for (index in currentClips.indices) {
            val clipDur = currentClips[index].effectiveDurationMs
            if (remaining <= clipDur || index == currentClips.lastIndex) {
                val localOffset = (remaining * currentClips[index].playbackSpeed).toLong()
                    .coerceIn(0L, (currentClips[index].trimEndMs - currentClips[index].trimStartMs).coerceAtLeast(0L))
                player.seekTo(index, localOffset)
                player.playbackParameters = PlaybackParameters(currentClips[index].playbackSpeed)
                syncPositionNow()
                return
            }
            remaining -= clipDur
        }
    }

    fun stepBy(deltaMs: Long) {
        val target = _playbackState.value.currentPositionMs + deltaMs
        seekToGlobalPosition(target)
    }

    private fun startTicker() {
        if (positionTickerJob?.isActive == true) return
        positionTickerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                syncPositionNow()
                delay(50L)
            }
        }
    }

    private fun stopTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun syncPositionNow() {
        if (currentClips.isEmpty()) return
        val idx = player.currentMediaItemIndex.coerceIn(0, currentClips.lastIndex)
        val precedingMs = currentClips.take(idx).sumOf { it.effectiveDurationMs }
        val activeClip = currentClips[idx]
        val localMs = (player.currentPosition.coerceAtLeast(0L) / activeClip.playbackSpeed).toLong()
        val total = currentClips.sumOf { it.effectiveDurationMs }
        _playbackState.value = _playbackState.value.copy(
            currentPositionMs = (precedingMs + localMs).coerceIn(0L, total),
            totalDurationMs = total,
            activeClipIndex = idx,
            hasMedia = true
        )
    }

    fun release() {
        stopTicker()
        player.removeListener(listener)
        player.release()
    }
}
