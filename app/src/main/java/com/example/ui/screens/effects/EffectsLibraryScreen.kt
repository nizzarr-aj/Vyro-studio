package com.example.ui.screens.effects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.model.EffectCategory
import com.example.core.model.EffectKind
import com.example.core.model.VideoEffectPreset
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroPillBadge
import com.example.ui.components.VyroTopBar
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroEmerald
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun EffectsLibraryScreen(
    viewModel: EffectsLibraryViewModel,
    widthSizeClass: WindowWidthSizeClass,
    showBackButton: Boolean,
    onNavigateBack: () -> Unit,
    onCreateProject: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appliedIds = uiState.activeProject?.appliedEffects?.map { it.presetId }?.toSet().orEmpty()
    val columns = if (widthSizeClass == WindowWidthSizeClass.Compact) 1 else 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .then(if (!showBackButton) Modifier.statusBarsPadding() else Modifier)
            .testTag("effects_library_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showBackButton) {
            VyroTopBar(
                title = stringResource(R.string.effects_title),
                subtitle = stringResource(R.string.effects_subtitle),
                onNavigateBack = onNavigateBack
            )
        } else {
            Column(
                modifier = Modifier
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.effects_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = VyroTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.effects_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 1100.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Project Banner
            item(span = { GridItemSpan(columns) }) {
                VyroGlassCard(
                    highlighted = uiState.activeProject != null,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                tint = VyroNeonBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.activeProject?.let {
                                    stringResource(
                                        R.string.effects_active_banner,
                                        it.title,
                                        it.appliedEffects.size
                                    )
                                } ?: stringResource(R.string.effects_no_project_banner),
                                style = MaterialTheme.typography.titleSmall,
                                color = VyroTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (uiState.activeProject == null) {
                            TextButton(onClick = onCreateProject) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.action_new_project))
                            }
                        } else if (appliedIds.isNotEmpty()) {
                            TextButton(onClick = viewModel::clearAllEffects) {
                                Icon(Icons.Outlined.ClearAll, contentDescription = null, tint = VyroNeonBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.effects_clear_all), color = VyroNeonBlue)
                            }
                        }
                    }
                }
            }

            // Search + Category Filter
            item(span = { GridItemSpan(columns) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text(stringResource(R.string.effects_search_hint)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = VyroNeonBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroElectricPurple,
                            unfocusedBorderColor = VyroBorderSubtle,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf(
                            null to R.string.effects_cat_all,
                            EffectCategory.COLOR to R.string.effects_cat_color,
                            EffectCategory.TONE to R.string.effects_cat_tone,
                            EffectCategory.OPTICS to R.string.effects_cat_optics
                        )
                        items(categories.size) { idx ->
                            val (cat, labelRes) = categories[idx]
                            val selected = uiState.selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setCategory(cat) },
                                label = { Text(stringResource(labelRes)) },
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

            items(uiState.presets, key = { it.id }) { preset ->
                val isApplied = preset.id in appliedIds
                EffectPresetCard(
                    preset = preset,
                    isApplied = isApplied,
                    canApply = uiState.activeProject != null,
                    onToggle = { viewModel.toggleEffect(preset.id) }
                )
            }
        }
    }
}

@Composable
private fun EffectPresetCard(
    preset: VideoEffectPreset,
    isApplied: Boolean,
    canApply: Boolean,
    onToggle: () -> Unit
) {
    val swatchColors = when (preset.kind) {
        EffectKind.CYBER_NEON -> listOf(VyroElectricPurple, VyroNeonBlue)
        EffectKind.WARM_CINEMA -> listOf(Color(0xFFF59E0B), Color(0xFF0F766E))
        EffectKind.VIVID_HUE -> listOf(Color(0xFFEC4899), VyroElectricPurple)
        EffectKind.SATURATION_BOOST -> listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
        EffectKind.HIGH_CONTRAST -> listOf(Color(0xFF0F172A), Color(0xFFE2E8F0))
        EffectKind.NOIR_SHADOW -> listOf(Color(0xFF09090B), Color(0xFF334155))
        EffectKind.GRAYSCALE -> listOf(Color(0xFF1E293B), Color(0xFFCBD5E1))
        EffectKind.INVERT -> listOf(Color(0xFF22D3EE), Color(0xFFF43F5E))
        EffectKind.ROTATE_90 -> listOf(VyroNeonBlue, Color(0xFF1E1B4B))
        EffectKind.MIRROR_FLIP -> listOf(VyroElectricPurple, Color(0xFF0E7490))
    }

    VyroGlassCard(
        highlighted = isApplied,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("effect_card_${preset.id}"),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(swatchColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = VyroTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isApplied) {
                        VyroPillBadge(
                            text = stringResource(R.string.effects_applied),
                            accentColor = VyroEmerald
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onToggle,
                enabled = canApply,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) VyroEmerald.copy(alpha = 0.2f) else VyroElectricPurple,
                    contentColor = if (isApplied) VyroEmerald else Color.White
                ),
                border = if (isApplied) BorderStroke(1.dp, VyroEmerald) else null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(42.dp)
            ) {
                if (isApplied) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                } else {
                    Text(stringResource(R.string.effects_apply), maxLines = 1)
                }
            }
        }
    }
}
