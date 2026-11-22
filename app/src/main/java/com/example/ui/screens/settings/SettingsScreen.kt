package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.model.AppLanguage
import com.example.core.model.FrameRateOption
import com.example.core.model.ResolutionPreset
import com.example.core.util.VyroTimeFormatter
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroPillBadge
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroEmerald
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nameInput by remember(uiState.preferences.creatorName) {
        mutableStateOf(uiState.preferences.creatorName)
    }
    var handleInput by remember(uiState.preferences.creatorHandle) {
        mutableStateOf(uiState.preferences.creatorHandle)
    }

    LaunchedEffect(uiState.profileSavedFeedback) {
        if (uiState.profileSavedFeedback) {
            kotlinx.coroutines.delay(2000L)
            viewModel.dismissSavedFeedback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .statusBarsPadding()
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 820.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = VyroTextPrimary
            )

            // Creator Profile Card
            VyroGlassCard(
                highlighted = true,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.img_creator_avatar),
                        contentDescription = uiState.preferences.creatorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue)),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        VyroPillBadge(
                            text = stringResource(R.string.settings_studio_badge),
                            accentColor = VyroNeonBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.preferences.creatorName,
                            style = MaterialTheme.typography.titleLarge,
                            color = VyroTextPrimary
                        )
                        Text(
                            text = uiState.preferences.creatorHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = VyroTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.settings_creator_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroElectricPurple,
                        unfocusedBorderColor = VyroBorderSubtle,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = handleInput,
                    onValueChange = { handleInput = it },
                    label = { Text(stringResource(R.string.settings_creator_handle)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroElectricPurple,
                        unfocusedBorderColor = VyroBorderSubtle,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_handle_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VyroGradientButton(
                        text = stringResource(R.string.settings_save_profile),
                        icon = Icons.Outlined.Save,
                        onClick = { viewModel.saveProfile(nameInput, handleInput) },
                        testTag = "settings_save_profile_button"
                    )
                    if (uiState.profileSavedFeedback) {
                        VyroPillBadge(
                            text = stringResource(R.string.settings_saved_feedback),
                            accentColor = VyroEmerald
                        )
                    }
                }
            }

            // Interface Language (EN / FR / AR RTL)
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Language, contentDescription = null, tint = VyroNeonBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_language_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VyroTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_language_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = uiState.preferences.language == lang
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { Text(lang.nativeName) },
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
                            modifier = Modifier.testTag("lang_chip_${lang.code}")
                        )
                    }
                }
            }

            // Default Mastering Specs
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_defaults_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResolutionPreset.entries.forEach { res ->
                        val selected = uiState.preferences.defaultResolution == res
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setDefaultResolution(res) },
                            label = { Text(res.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrameRateOption.entries.forEach { opt ->
                        val selected = uiState.preferences.defaultFps == opt.fps
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setDefaultFps(opt.fps) },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_hevc_label),
                            style = MaterialTheme.typography.titleSmall,
                            color = VyroTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.settings_hevc_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = VyroTextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.preferences.preferHevc,
                        onCheckedChange = viewModel::setPreferHevc,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VyroNeonBlue,
                            checkedTrackColor = VyroPurpleContainer
                        )
                    )
                }
            }

            // Studio Storage
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Storage, contentDescription = null, tint = VyroElectricPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_storage_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VyroTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.settings_storage_desc,
                        uiState.projectCount,
                        VyroTimeFormatter.formatMb(uiState.exportsBytes)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = viewModel::clearExports,
                    enabled = uiState.exportsBytes > 0L,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VyroBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroNeonBlue),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_clear_exports))
                }
            }

            // Architecture Info
            VyroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Memory, contentDescription = null, tint = VyroNeonBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_engine_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VyroTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_engine_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
