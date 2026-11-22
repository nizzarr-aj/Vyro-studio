package com.example.ui.screens.newproject

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
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.FrameRateOption
import com.example.core.model.ResolutionPreset
import com.example.ui.components.VyroAspectFramePreview
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroTopBar
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroRose
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewProjectScreen(
    viewModel: NewProjectViewModel,
    onNavigateBack: () -> Unit,
    onCreatedAndImport: (Long) -> Unit,
    onCreatedEmpty: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .navigationBarsPadding()
            .testTag("new_project_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VyroTopBar(
            title = stringResource(R.string.new_project_title),
            subtitle = stringResource(R.string.new_project_subtitle),
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VyroGlassCard(
                highlighted = true,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.title.ifBlank { stringResource(R.string.field_project_name_hint) },
                            style = MaterialTheme.typography.titleLarge,
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${uiState.aspectRatio.ratioLabel} • ${uiState.resolution.label} • ${uiState.fps} fps",
                            style = MaterialTheme.typography.labelSmall,
                            color = VyroNeonBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    VyroAspectFramePreview(
                        ratio = uiState.aspectRatio.floatRatio,
                        label = uiState.aspectRatio.ratioLabel
                    )
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text(stringResource(R.string.field_project_name)) },
                placeholder = { Text(stringResource(R.string.field_project_name_hint)) },
                singleLine = true,
                isError = uiState.showTitleError,
                supportingText = if (uiState.showTitleError) {
                    { Text(stringResource(R.string.error_project_name_required), color = VyroRose) }
                } else null,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroElectricPurple,
                    unfocusedBorderColor = VyroBorderSubtle,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary,
                    focusedLabelColor = VyroNeonBlue,
                    unfocusedLabelColor = VyroTextSecondary,
                    cursorColor = VyroNeonBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_title_input")
            )

            // Aspect Ratio
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.section_aspect_ratio),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioPreset.entries.forEach { preset ->
                        val selected = uiState.aspectRatio == preset
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onAspectRatioSelected(preset) },
                            label = { Text(stringResource(preset.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = VyroElectricPurple,
                                borderColor = VyroBorderSubtle
                            ),
                            modifier = Modifier.testTag("aspect_chip_${preset.id}")
                        )
                    }
                }
            }

            // Frame Rate & Resolution
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.section_frame_rate),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrameRateOption.entries.forEach { option ->
                        val selected = uiState.fps == option.fps
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onFpsSelected(option.fps) },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag("fps_chip_${option.fps}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.section_resolution),
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
                            onClick = { viewModel.onResolutionSelected(res) },
                            label = { Text("${res.label} (${res.detail})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag("resolution_chip_${res.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.section_color_space),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorProfile.entries.forEach { profile ->
                        val selected = uiState.colorProfile == profile
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onColorProfileSelected(profile) },
                            label = { Text(profile.label) },
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

            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VyroGradientButton(
                    text = stringResource(R.string.action_create_and_import),
                    icon = Icons.Outlined.PermMedia,
                    enabled = !uiState.isCreating,
                    onClick = { viewModel.createProject(onCreatedAndImport) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "create_and_import_button"
                )
                OutlinedButton(
                    onClick = { viewModel.createProject(onCreatedEmpty) },
                    enabled = !uiState.isCreating,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VyroBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = VyroGraphiteElevated,
                        contentColor = VyroTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_empty_timeline_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VideoSettings,
                        contentDescription = null,
                        tint = VyroNeonBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_create_empty))
                }
            }
        }
    }
}
