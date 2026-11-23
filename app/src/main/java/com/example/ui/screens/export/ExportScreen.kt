package com.example.ui.screens.export

import android.content.Intent
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import java.io.File
import com.example.core.model.BitratePreset
import com.example.core.model.ExportJobState
import com.example.core.model.FrameRateOption
import com.example.core.model.ResolutionPreset
import com.example.core.model.VideoCodecOption
import com.example.core.util.VyroTimeFormatter
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroPillBadge
import com.example.ui.components.VyroTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroEmerald
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroRose
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    onImportClipsFirst: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val project = uiState.project
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .navigationBarsPadding()
            .testTag("export_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VyroTopBar(
            title = stringResource(R.string.export_title),
            subtitle = stringResource(R.string.export_subtitle),
            onNavigateBack = onNavigateBack
        )

        if (project == null) return@Column

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Project Summary Header
            VyroGlassCard(
                highlighted = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.export_duration_summary,
                                VyroTimeFormatter.formatDurationShort(project.totalDurationMs)
                            ),
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = VyroNeonBlue
                        )
                    }
                    VyroPillBadge(
                        text = VyroTimeFormatter.estimateExportMb(
                            project.totalDurationMs,
                            uiState.bitrate.mbps
                        ).let { stringResource(R.string.export_estimated_size, it) },
                        accentColor = VyroElectricPurple
                    )
                }
            }

            if (project.clips.isEmpty()) {
                VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.export_no_clips_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VyroTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    VyroGradientButton(
                        text = stringResource(R.string.export_import_first),
                        icon = Icons.Outlined.PermMedia,
                        onClick = { onImportClipsFirst(project.id) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "export_import_first_button"
                    )
                }
            }

            // Video Stream Settings
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.export_section_video),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResolutionPreset.entries.forEach { res ->
                        val selected = uiState.resolution == res
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectResolution(res) },
                            label = { Text(res.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag("export_res_${res.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrameRateOption.entries.forEach { option ->
                        val selected = uiState.fps == option.fps
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectFps(option.fps) },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag("export_fps_${option.fps}")
                        )
                    }
                }
            }

            // Codec & Bitrate
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.export_section_codec),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoCodecOption.entries.forEach { codec ->
                        val selected = uiState.codec == codec
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectCodec(codec) },
                            label = { Text(codec.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BitratePreset.entries.forEach { bitrate ->
                        val selected = uiState.bitrate == bitrate
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectBitrate(bitrate) },
                            label = { Text(stringResource(bitrate.labelRes)) },
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

            // Audio Mastering
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.export_section_audio),
                            style = MaterialTheme.typography.titleMedium,
                            color = VyroTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.export_include_audio),
                            style = MaterialTheme.typography.bodySmall,
                            color = VyroTextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.includeAudio,
                        onCheckedChange = viewModel::setIncludeAudio,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VyroNeonBlue,
                            checkedTrackColor = VyroPurpleContainer
                        )
                    )
                }
            }

            // Export Job Status Card
            when (val job = uiState.jobState) {
                is ExportJobState.Preparing, is ExportJobState.Exporting -> {
                    val percent = (job as? ExportJobState.Exporting)?.progressPercent ?: 0
                    VyroGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.export_progress_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = VyroTextPrimary
                            )
                            Text(
                                text = "$percent%",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelLarge,
                                color = VyroNeonBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            color = VyroElectricPurple,
                            trackColor = VyroDeepBlack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }

                is ExportJobState.Completed -> {
                    VyroGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = VyroEmerald
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.export_complete_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VyroEmerald
                                )
                                Text(
                                    text = stringResource(
                                        R.string.export_complete_subtitle,
                                        VyroTimeFormatter.formatMb(job.sizeBytes)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VyroTextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val file = File(job.outputPath)
                                Thread {
                                    val message = try {
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                            "Gallery save requires Android 10 or newer."
                                        } else if (!file.exists()) {
                                            "Exported video file was not found."
                                        } else {
                                            val values = ContentValues().apply {
                                                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                                                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                                                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VYRO Studio")
                                                put(MediaStore.Video.Media.IS_PENDING, 1)
                                            }
                                            val resolver = context.contentResolver
                                            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                                                ?: throw IllegalStateException("Could not create gallery entry")
                                            try {
                                                resolver.openOutputStream(uri)?.use { output ->
                                                    file.inputStream().use { input -> input.copyTo(output) }
                                                } ?: throw IllegalStateException("Could not open gallery output")
                                                values.clear()
                                                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                                                resolver.update(uri, values, null, null)
                                                "Video saved to Movies/VYRO Studio"
                                            } catch (error: Throwable) {
                                                resolver.delete(uri, null, null)
                                                throw error
                                            }
                                        }
                                    } catch (error: Throwable) {
                                        error.localizedMessage ?: "Unable to save video to gallery."
                                    }
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }.start()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save to Gallery")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        VyroGradientButton(
                            text = stringResource(R.string.export_share_button),
                            icon = Icons.Outlined.Share,
                            onClick = {
                                val file = File(job.outputPath)
                                if (file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(intent, project.title).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(chooser)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "share_exported_video_button"
                        )
                    }
                }

                is ExportJobState.Failed -> {
                    VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = VyroRose
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.export_error_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VyroRose
                                )
                                Text(
                                    text = job.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VyroTextSecondary
                                )
                            }
                        }
                    }
                }

                ExportJobState.Idle -> Unit
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val isRunning = uiState.jobState is ExportJobState.Preparing ||
                uiState.jobState is ExportJobState.Exporting
            if (isRunning) {
                OutlinedButton(
                    onClick = viewModel::cancelExport,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VyroRose),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("cancel_export_button")
                ) {
                    Icon(Icons.Outlined.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_cancel_button))
                }
            } else {
                VyroGradientButton(
                    text = stringResource(R.string.export_start_button),
                    icon = Icons.Outlined.FileUpload,
                    enabled = project.clips.isNotEmpty(),
                    onClick = viewModel::startExport,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "start_export_button"
                )
            }
        }
    }
}
