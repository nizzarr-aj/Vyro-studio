package com.example.ui.screens.mediaimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.example.R
import com.example.core.model.MediaPickCandidate
import com.example.core.util.VyroTimeFormatter
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroPillBadge
import com.example.ui.components.VyroTopBar
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroRose
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun MediaImportScreen(
    viewModel: MediaImportViewModel,
    onNavigateBack: () -> Unit,
    onCommittedToWorkspace: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fallbackTitle = stringResource(R.string.default_imported_project_name)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
        onResult = { uris -> viewModel.onMediaUrisPicked(uris) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .navigationBarsPadding()
            .testTag("media_import_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VyroTopBar(
            title = stringResource(R.string.import_title),
            subtitle = uiState.targetProjectTitle?.let {
                stringResource(R.string.import_target_project, it)
            } ?: stringResource(R.string.import_subtitle),
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Photo Picker Filter Selector + Launch CTA
            VyroGlassCard(
                highlighted = true,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        MediaPickerFilter.ALL to R.string.import_filter_all,
                        MediaPickerFilter.VIDEO to R.string.import_filter_video,
                        MediaPickerFilter.IMAGE to R.string.import_filter_image
                    )
                    filters.forEach { (filter, labelRes) ->
                        val selected = uiState.filter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = stringResource(labelRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroPurpleContainer,
                                selectedLabelColor = VyroTextPrimary,
                                containerColor = VyroGraphiteElevated,
                                labelColor = VyroTextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                VyroGradientButton(
                    text = stringResource(R.string.import_pick_button),
                    icon = Icons.Rounded.AddPhotoAlternate,
                    onClick = {
                        val mediaType = when (uiState.filter) {
                            MediaPickerFilter.ALL -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            MediaPickerFilter.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
                            MediaPickerFilter.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
                        }
                        photoPickerLauncher.launch(PickVisualMediaRequest(mediaType))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "launch_photo_picker_button"
                )

                if (uiState.isExtracting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        color = VyroNeonBlue,
                        trackColor = VyroDeepBlack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.import_queue_title, uiState.queuedMedia.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = VyroTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.queuedMedia.isEmpty()) {
                VyroGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(VyroNeonBlue.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PermMedia,
                                contentDescription = null,
                                tint = VyroNeonBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.import_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = VyroTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.import_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VyroTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    itemsIndexed(uiState.queuedMedia) { index, candidate ->
                        QueuedMediaCard(
                            candidate = candidate,
                            canMoveUp = index > 0,
                            canMoveDown = index < uiState.queuedMedia.lastIndex,
                            onMoveUp = { viewModel.moveItem(index, -1) },
                            onMoveDown = { viewModel.moveItem(index, 1) },
                            onRemove = { viewModel.removeItem(index) },
                            context = context
                        )
                    }
                }
            }
        }

        if (uiState.queuedMedia.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                VyroGradientButton(
                    text = stringResource(R.string.import_commit_timeline),
                    icon = Icons.Rounded.Check,
                    enabled = !uiState.isCommitting,
                    onClick = {
                        viewModel.commitToTimeline(fallbackTitle, onCommittedToWorkspace)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "commit_media_to_timeline_button"
                )
            }
        }
    }
}

@Composable
private fun QueuedMediaCard(
    candidate: MediaPickCandidate,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    context: android.content.Context
) {
    VyroGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(candidate.uriString))
                    .apply {
                        if (candidate.isVideo) {
                            decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                        }
                    }
                    .crossfade(true)
                    .build(),
                contentDescription = candidate.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 74.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VyroDeepBlack)
                    .border(1.dp, VyroBorderSubtle, RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = VyroTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VyroPillBadge(
                        text = stringResource(
                            if (candidate.isVideo) R.string.import_clip_video_tag
                            else R.string.import_clip_image_tag
                        ),
                        accentColor = if (candidate.isVideo) VyroElectricPurple else VyroNeonBlue
                    )
                    VyroPillBadge(
                        text = VyroTimeFormatter.formatDurationShort(candidate.durationMs),
                        accentColor = VyroNeonBlue
                    )
                    Text(
                        text = "${candidate.width}×${candidate.height}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = VyroTextSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = stringResource(R.string.import_move_up),
                        tint = if (canMoveUp) VyroTextPrimary else VyroTextSecondary.copy(alpha = 0.35f)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = stringResource(R.string.import_move_down),
                        tint = if (canMoveDown) VyroTextPrimary else VyroTextSecondary.copy(alpha = 0.35f)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.import_remove_clip),
                        tint = VyroRose
                    )
                }
            }
        }
    }
}
