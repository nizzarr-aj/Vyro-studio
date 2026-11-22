package com.example.ui.screens.workspace

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.example.R
import com.example.core.model.TimelineClip
import com.example.core.model.VideoProject
import com.example.core.util.VyroTimeFormatter
import com.example.data.repository.EffectsCatalogRepository
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroPillBadge
import com.example.ui.components.VyroTimecodeChip
import com.example.ui.components.VyroTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroEmerald
import com.example.ui.theme.VyroGraphite
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroObsidian
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroRose
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onNavigateBack: () -> Unit,
    onImportMedia: (Long) -> Unit,
    onOpenEffects: (Long) -> Unit,
    onOpenExport: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val project = uiState.project

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .navigationBarsPadding()
            .testTag("workspace_screen")
    ) {
        VyroTopBar(
            title = project?.title ?: stringResource(R.string.nav_dashboard),
            subtitle = project?.let {
                "${it.aspectRatio.ratioLabel} • ${it.resolution.label} • ${it.fps} fps"
            },
            onNavigateBack = onNavigateBack,
            actions = {
                if (project != null) {
                    VyroGradientButton(
                        text = stringResource(R.string.action_export),
                        icon = Icons.Outlined.FileUpload,
                        onClick = { onOpenExport(project.id) },
                        modifier = Modifier.height(42.dp),
                        testTag = "workspace_export_top_button"
                    )
                }
            }
        )

        if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.splash_status_init),
                    color = VyroTextSecondary
                )
            }
            return@Column
        }

        val isWide = widthSizeClass == WindowWidthSizeClass.Expanded

        if (isWide) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PreviewStage(
                        project = project,
                        viewModel = viewModel,
                        onImportMedia = { onImportMedia(project.id) },
                        modifier = Modifier.weight(1f)
                    )
                    TransportBar(
                        uiState = uiState,
                        project = project,
                        viewModel = viewModel
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimelineAndToolsSection(
                        uiState = uiState,
                        project = project,
                        viewModel = viewModel,
                        onImportMedia = { onImportMedia(project.id) },
                        onOpenEffects = { onOpenEffects(project.id) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PreviewStage(
                    project = project,
                    viewModel = viewModel,
                    onImportMedia = { onImportMedia(project.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                TransportBar(
                    uiState = uiState,
                    project = project,
                    viewModel = viewModel
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimelineAndToolsSection(
                        uiState = uiState,
                        project = project,
                        viewModel = viewModel,
                        onImportMedia = { onImportMedia(project.id) },
                        onOpenEffects = { onOpenEffects(project.id) }
                    )
                }
            }
        }

        // Bottom Studio Tool Dock
        WorkspaceToolDock(
            activePanel = uiState.activePanel,
            isMuted = project.isMuted,
            hasSelection = uiState.selectedClip != null,
            onImport = { onImportMedia(project.id) },
            onToggleTrim = { viewModel.togglePanel(WorkspaceToolPanel.TRIM) },
            onToggleSpeed = { viewModel.togglePanel(WorkspaceToolPanel.SPEED) },
            onOpenEffects = { onOpenEffects(project.id) },
            onToggleAudio = viewModel::toggleMute,
            onExport = { onOpenExport(project.id) }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PreviewStage(
    project: VideoProject,
    viewModel: WorkspaceViewModel,
    onImportMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(VyroObsidian)
            .border(1.dp, VyroBorderSubtle, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (project.clips.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .aspectRatio(project.aspectRatio.floatRatio.coerceIn(0.45f, 2.4f))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                VyroElectricPurple.copy(alpha = 0.14f),
                                VyroDeepBlack,
                                VyroNeonBlue.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, VyroElectricPurple.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.workspace_no_clips_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VyroTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.workspace_no_clips_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = VyroTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    VyroGradientButton(
                        text = stringResource(R.string.action_import_media),
                        icon = Icons.Outlined.PermMedia,
                        onClick = onImportMedia,
                        testTag = "workspace_empty_import_button"
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .aspectRatio(project.aspectRatio.floatRatio.coerceIn(0.45f, 2.4f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            player = viewModel.previewController.player
                        }
                    },
                    update = { playerView ->
                        if (playerView.player !== viewModel.previewController.player) {
                            playerView.player = viewModel.previewController.player
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Aspect & FX telemetry overlay pill
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VyroPillBadge(
                text = project.aspectRatio.ratioLabel,
                accentColor = VyroNeonBlue
            )
            if (project.appliedEffects.isNotEmpty()) {
                VyroPillBadge(
                    text = stringResource(R.string.project_effects_count, project.appliedEffects.size),
                    accentColor = VyroElectricPurple
                )
            }
        }
    }
}

@Composable
private fun TransportBar(
    uiState: WorkspaceUiState,
    project: VideoProject,
    viewModel: WorkspaceViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VyroGraphite)
            .border(1.dp, VyroBorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        VyroTimecodeChip(
            currentMs = uiState.playback.currentPositionMs,
            totalMs = project.totalDurationMs
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { viewModel.stepBy(-1000L) },
                enabled = uiState.playback.hasMedia,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Replay10,
                    contentDescription = stringResource(R.string.action_step_back),
                    tint = VyroTextPrimary
                )
            }

            Surface(
                shape = CircleShape,
                color = VyroElectricPurple,
                modifier = Modifier
                    .size(44.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("workspace_play_pause_button")
            ) {
                IconButton(
                    onClick = viewModel::togglePlayPause,
                    enabled = uiState.playback.hasMedia
                ) {
                    Icon(
                        imageVector = if (uiState.playback.isPlaying) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (uiState.playback.isPlaying) R.string.action_pause else R.string.action_play
                        ),
                        tint = Color.White
                    )
                }
            }

            IconButton(
                onClick = { viewModel.stepBy(1000L) },
                enabled = uiState.playback.hasMedia,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Forward10,
                    contentDescription = stringResource(R.string.action_step_forward),
                    tint = VyroTextPrimary
                )
            }
        }

        IconButton(
            onClick = viewModel::toggleMute,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("workspace_mute_button")
        ) {
            Icon(
                imageVector = if (project.isMuted) {
                    Icons.AutoMirrored.Outlined.VolumeOff
                } else {
                    Icons.AutoMirrored.Outlined.VolumeUp
                },
                contentDescription = stringResource(R.string.tool_audio),
                tint = if (project.isMuted) VyroRose else VyroNeonBlue
            )
        }
    }
}

@Composable
private fun TimelineAndToolsSection(
    uiState: WorkspaceUiState,
    project: VideoProject,
    viewModel: WorkspaceViewModel,
    onImportMedia: () -> Unit,
    onOpenEffects: () -> Unit
) {
    val totalMs = project.totalDurationMs.coerceAtLeast(1L)

    // Contextual Trim / Speed Inspector
    AnimatedVisibility(visible = uiState.activePanel != WorkspaceToolPanel.NONE && uiState.selectedClip != null) {
        val selected = uiState.selectedClip
        if (selected != null) {
            VyroGlassCard(
                highlighted = true,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.workspace_selected_clip, selected.displayName),
                        style = MaterialTheme.typography.titleSmall,
                        color = VyroTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::removeSelectedClip) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = VyroRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_delete_clip), color = VyroRose)
                    }
                }

                if (uiState.activePanel == WorkspaceToolPanel.TRIM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.adjustTrimStart(-500L) },
                            enabled = selected.isVideo,
                            modifier = Modifier.weight(1f)
                        ) { Text("In -0.5s") }
                        OutlinedButton(
                            onClick = { viewModel.adjustTrimStart(500L) },
                            enabled = selected.isVideo,
                            modifier = Modifier.weight(1f)
                        ) { Text("In +0.5s") }
                        OutlinedButton(
                            onClick = { viewModel.adjustTrimEnd(-500L) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Out -0.5s") }
                        OutlinedButton(
                            onClick = { viewModel.adjustTrimEnd(500L) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Out +0.5s") }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${VyroTimeFormatter.formatTimecode(selected.trimStartMs)} → ${VyroTimeFormatter.formatTimecode(selected.trimEndMs)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = VyroNeonBlue
                        )
                        TextButton(onClick = viewModel::resetTrim) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.action_reset_trim))
                        }
                    }
                } else if (uiState.activePanel == WorkspaceToolPanel.SPEED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.workspace_speed_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = VyroTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                            val isSelected = selected.playbackSpeed == speed
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setClipSpeed(speed) },
                                label = { Text("${speed}x") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VyroPurpleContainer,
                                    selectedLabelColor = VyroTextPrimary,
                                    containerColor = VyroGraphiteElevated,
                                    labelColor = VyroTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Playhead Scrubber + Time Ruler
    VyroGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        // Time Ruler Ticks
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val tickCount = 24
            val step = size.width / tickCount
            for (i in 0..tickCount) {
                val x = i * step
                val tall = i % 4 == 0
                drawLine(
                    color = if (tall) VyroNeonBlue.copy(alpha = 0.65f) else VyroBorderSubtle,
                    start = Offset(x, size.height),
                    end = Offset(x, if (tall) 2f else size.height * 0.45f),
                    strokeWidth = if (tall) 2f else 1f,
                    cap = StrokeCap.Round
                )
            }
        }

        Slider(
            value = (uiState.playback.currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f),
            onValueChange = { fraction ->
                viewModel.seekTo((fraction * totalMs).toLong())
            },
            enabled = project.clips.isNotEmpty(),
            colors = SliderDefaults.colors(
                thumbColor = VyroNeonBlue,
                activeTrackColor = VyroElectricPurple,
                inactiveTrackColor = VyroGraphiteElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Track V1: Video Clips
        Text(
            text = stringResource(R.string.workspace_track_video),
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = VyroTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(project.clips, key = { it.id }) { clip ->
                TimelineClipBlock(
                    clip = clip,
                    selected = clip.id == uiState.selectedClip?.id,
                    onClick = { viewModel.selectClip(clip.id) }
                )
            }
            item {
                Surface(
                    onClick = onImportMedia,
                    shape = RoundedCornerShape(12.dp),
                    color = VyroGraphiteElevated,
                    border = BorderStroke(1.dp, VyroElectricPurple.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .size(width = 58.dp, height = 54.dp)
                        .testTag("timeline_add_clip_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.action_import_media),
                            tint = VyroNeonBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track FX: Active Shaders
        Text(
            text = stringResource(R.string.workspace_track_effects),
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = VyroTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = onOpenEffects,
            shape = RoundedCornerShape(10.dp),
            color = VyroPurpleContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, VyroElectricPurple.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("timeline_fx_track")
        ) {
            if (project.appliedEffects.isEmpty()) {
                Text(
                    text = stringResource(R.string.workspace_no_effects_applied),
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(project.appliedEffects, key = { it.presetId }) { fx ->
                        val name = EffectsCatalogRepository.findById(fx.presetId)?.name ?: fx.presetId
                        VyroPillBadge(
                            text = name,
                            accentColor = VyroElectricPurple
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track A1: Master Audio
        Text(
            text = stringResource(R.string.workspace_track_audio),
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = VyroTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = viewModel::toggleMute,
            shape = RoundedCornerShape(10.dp),
            color = VyroDeepBlack,
            border = BorderStroke(
                1.dp,
                if (project.isMuted) VyroRose.copy(alpha = 0.5f) else VyroEmerald.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        if (project.isMuted) R.string.workspace_audio_muted
                        else R.string.workspace_audio_active
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (project.isMuted) VyroRose else VyroEmerald
                )
                Canvas(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                ) {
                    val bars = 18
                    val step = size.width / bars
                    val barColor = if (project.isMuted) VyroTextSecondary.copy(alpha = 0.25f) else VyroNeonBlue
                    for (i in 0 until bars) {
                        val amp = ((i * 7) % 5 + 1) / 5f
                        val h = size.height * amp
                        drawLine(
                            color = barColor,
                            start = Offset(i * step + 2f, (size.height - h) / 2f),
                            end = Offset(i * step + 2f, (size.height + h) / 2f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineClipBlock(
    clip: TimelineClip,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val widthDp = ((clip.effectiveDurationMs / 1000f) * 20f).coerceIn(92f, 200f).dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = VyroGraphiteElevated,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            brush = if (selected) {
                Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue))
            } else {
                Brush.linearGradient(listOf(VyroBorderSubtle, VyroBorderSubtle))
            }
        ),
        modifier = Modifier
            .size(width = widthDp, height = 54.dp)
            .testTag("timeline_clip_${clip.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(clip.uriString))
                    .apply {
                        if (clip.isVideo) {
                            decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                        }
                    }
                    .build(),
                contentDescription = clip.displayName,
                contentScale = ContentScale.Crop,
                alpha = 0.55f,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, VyroDeepBlack.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = clip.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${VyroTimeFormatter.formatDurationShort(clip.effectiveDurationMs)} • ${clip.playbackSpeed}x",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = VyroNeonBlue
                )
            }
        }
    }
}

@Composable
private fun WorkspaceToolDock(
    activePanel: WorkspaceToolPanel,
    isMuted: Boolean,
    hasSelection: Boolean,
    onImport: () -> Unit,
    onToggleTrim: () -> Unit,
    onToggleSpeed: () -> Unit,
    onOpenEffects: () -> Unit,
    onToggleAudio: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        color = VyroGraphite,
        border = BorderStroke(1.dp, VyroBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            item {
                ToolDockButton(
                    icon = Icons.Outlined.PermMedia,
                    label = stringResource(R.string.tool_import),
                    active = false,
                    onClick = onImport,
                    testTag = "dock_import"
                )
            }
            item {
                ToolDockButton(
                    icon = Icons.Outlined.ContentCut,
                    label = stringResource(R.string.tool_trim),
                    active = activePanel == WorkspaceToolPanel.TRIM,
                    enabled = hasSelection,
                    onClick = onToggleTrim,
                    testTag = "dock_trim"
                )
            }
            item {
                ToolDockButton(
                    icon = Icons.Outlined.Speed,
                    label = stringResource(R.string.tool_speed),
                    active = activePanel == WorkspaceToolPanel.SPEED,
                    enabled = hasSelection,
                    onClick = onToggleSpeed,
                    testTag = "dock_speed"
                )
            }
            item {
                ToolDockButton(
                    icon = Icons.Outlined.AutoFixHigh,
                    label = stringResource(R.string.tool_effects),
                    active = false,
                    onClick = onOpenEffects,
                    testTag = "dock_effects"
                )
            }
            item {
                ToolDockButton(
                    icon = if (isMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                    label = stringResource(R.string.tool_audio),
                    active = isMuted,
                    onClick = onToggleAudio,
                    testTag = "dock_audio"
                )
            }
            item {
                ToolDockButton(
                    icon = Icons.Outlined.FileUpload,
                    label = stringResource(R.string.tool_export),
                    active = false,
                    onClick = onExport,
                    testTag = "dock_export"
                )
            }
        }
    }
}

@Composable
private fun ToolDockButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                !enabled -> VyroTextSecondary.copy(alpha = 0.35f)
                active -> VyroNeonBlue
                else -> VyroTextPrimary
            },
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                !enabled -> VyroTextSecondary.copy(alpha = 0.35f)
                active -> VyroNeonBlue
                else -> VyroTextSecondary
            },
            maxLines = 1
        )
    }
}
