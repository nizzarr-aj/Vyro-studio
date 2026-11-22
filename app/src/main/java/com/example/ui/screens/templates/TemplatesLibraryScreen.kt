package com.example.ui.screens.templates

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.core.model.ProjectTemplate
import com.example.core.model.TemplateCategory
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroPillBadge
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun TemplatesLibraryScreen(
    viewModel: TemplatesLibraryViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onTemplateSelected: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val columns = if (widthSizeClass == WindowWidthSizeClass.Compact) 1 else 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .statusBarsPadding()
            .testTag("templates_library_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.templates_title),
                style = MaterialTheme.typography.headlineMedium,
                color = VyroTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.templates_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = VyroTextSecondary
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 1100.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cats = listOf(
                        null to R.string.templates_cat_all,
                        TemplateCategory.REELS to R.string.templates_cat_reels,
                        TemplateCategory.CINEMA to R.string.templates_cat_cinema,
                        TemplateCategory.EDITORIAL to R.string.templates_cat_editorial
                    )
                    items(cats.size) { idx ->
                        val (cat, labelRes) = cats[idx]
                        val selected = uiState.selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectCategory(cat) },
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

            items(uiState.templates, key = { it.id }) { template ->
                TemplateBlueprintCard(
                    template = template,
                    enabled = !uiState.isCreating,
                    onUse = { viewModel.applyTemplate(template, onTemplateSelected) }
                )
            }
        }
    }
}

@Composable
private fun TemplateBlueprintCard(
    template: ProjectTemplate,
    enabled: Boolean,
    onUse: () -> Unit
) {
    VyroGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("template_card_${template.id}"),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
        ) {
            Image(
                painter = painterResource(template.previewDrawable),
                contentDescription = template.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                VyroDeepBlack.copy(alpha = 0.12f),
                                VyroDeepBlack.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VyroPillBadge(
                    text = template.tag,
                    accentColor = VyroNeonBlue
                )
                VyroPillBadge(
                    text = stringResource(
                        R.string.templates_fx_included,
                        template.presetEffectIds.size
                    ),
                    accentColor = VyroElectricPurple
                )
            }
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleLarge,
                color = VyroTextPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VyroTextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            VyroGradientButton(
                text = stringResource(R.string.templates_use_action),
                icon = Icons.Rounded.AutoAwesome,
                enabled = enabled,
                onClick = onUse,
                modifier = Modifier.fillMaxWidth(),
                testTag = "use_template_${template.id}"
            )
        }
    }
}
