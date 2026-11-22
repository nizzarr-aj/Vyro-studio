package com.example.ui.screens.dashboard

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.AutoAwesomeMosaic
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File
import com.example.R
import com.example.core.model.VideoProject
import com.example.core.util.VyroTimeFormatter
import com.example.ui.components.VyroGlassCard
import com.example.ui.components.VyroPillBadge
import com.example.ui.components.VyroPrismLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroAmber
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroElectricPurpleBright
import com.example.ui.theme.VyroGraphite
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroNeonCyan
import com.example.ui.theme.VyroObsidian
import com.example.ui.theme.VyroRose
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import com.example.ui.viewmodel.ProjectSortOption
import com.example.ui.viewmodel.ProjectViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun DashboardScreen(
    viewModel: ProjectViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onCreateNewProject: () -> Unit,
    onImportMedia: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onExportProject: (Long) -> Unit,
    onOpenProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projects = uiState.projects
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var projectPendingRename by remember { mutableStateOf<VideoProject?>(null) }
    var projectPendingDelete by remember { mutableStateOf<VideoProject?>(null) }

    val columns = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        else -> 4
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepBlack)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VyroElectricPurple.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.08f),
                        radius = size.width * 0.72f
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.08f),
                    radius = size.width * 0.72f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VyroNeonBlue.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.85f),
                        radius = size.width * 0.65f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.85f),
                    radius = size.width * 0.65f
                )
            }
            .statusBarsPadding()
            .testTag("dashboard_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 96.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(columns) }) {
                val favoritesFilterControl: @Composable () -> Unit = {
                    val active = uiState.showFavoritesOnly
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) VyroAmber.copy(alpha = 0.16f) else VyroGraphiteElevated,
                        border = BorderStroke(
                            1.dp,
                            if (active) VyroAmber else VyroBorderSubtle
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = viewModel::toggleFavoritesFilter)
                            .testTag("dashboard_favorites_filter_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (active) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                contentDescription = stringResource(R.string.cd_filter_favorites),
                                tint = if (active) VyroAmber else VyroTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.filter_favorites),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) VyroAmber else VyroTextPrimary,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                val sortDropdownControl: @Composable () -> Unit = {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VyroGraphiteElevated,
                            border = BorderStroke(
                                1.dp,
                                if (sortMenuExpanded) {
                                    VyroElectricPurple
                                } else {
                                    VyroBorderSubtle
                                }
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { sortMenuExpanded = true }
                                .testTag("dashboard_sort_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                                    contentDescription = stringResource(R.string.cd_sort_projects),
                                    tint = VyroNeonBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(uiState.sortOption.labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VyroTextPrimary,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = VyroTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            containerColor = VyroGraphiteElevated
                        ) {
                            ProjectSortOption.entries.forEach { option ->
                                val isSelected = uiState.sortOption == option
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(option.labelRes),
                                            color = if (isSelected) VyroNeonBlue else VyroTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = VyroNeonBlue
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.testTag(
                                        when (option) {
                                            ProjectSortOption.MOST_RECENT -> "sort_option_most_recent"
                                            ProjectSortOption.ALPHABETICAL_AZ -> "sort_option_az"
                                        }
                                    ),
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            VyroPrismLogo(size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${stringResource(R.string.brand_vyro)} ${stringResource(R.string.brand_studio)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VyroTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(
                                        R.string.dashboard_greeting,
                                        uiState.preferences.creatorName
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VyroTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (widthSizeClass != WindowWidthSizeClass.Compact) {
                                favoritesFilterControl()
                                sortDropdownControl()
                            }

                            Surface(
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.5.dp,
                                    Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue))
                                ),
                                modifier = Modifier
                                    .size(48.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .clickable(onClick = onOpenProfile)
                                    .testTag("dashboard_profile_button")
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.img_creator_avatar),
                                    contentDescription = stringResource(R.string.nav_settings),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    if (widthSizeClass == WindowWidthSizeClass.Compact) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            favoritesFilterControl()
                            sortDropdownControl()
                        }
                    }
                }
            }

            // Cinematic Hero Card
            item(span = { GridItemSpan(columns) }) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                VyroElectricPurple.copy(alpha = 0.65f),
                                VyroNeonBlue.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    color = VyroGraphite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(R.drawable.img_hero_studio),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alpha = 0.40f,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            VyroDeepBlack.copy(alpha = 0.30f),
                                            VyroDeepBlack.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )

                        Column(modifier = Modifier.padding(18.dp)) {
                            VyroPillBadge(
                                text = stringResource(R.string.dashboard_hero_badge),
                                accentColor = VyroNeonBlue
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.dashboard_hero_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = VyroTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.dashboard_hero_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VyroTextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onImportMedia,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, VyroNeonBlue.copy(alpha = 0.45f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = VyroGraphiteElevated.copy(alpha = 0.78f),
                                        contentColor = VyroTextPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("dashboard_import_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PermMedia,
                                        contentDescription = null,
                                        tint = VyroNeonBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.action_import_media),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = onOpenTemplates,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, VyroElectricPurple.copy(alpha = 0.45f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = VyroGraphiteElevated.copy(alpha = 0.78f),
                                        contentColor = VyroTextPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("dashboard_templates_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesomeMosaic,
                                        contentDescription = null,
                                        tint = VyroElectricPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.action_browse_templates),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Header
            item(span = { GridItemSpan(columns) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.section_recent_projects),
                        style = MaterialTheme.typography.titleLarge,
                        color = VyroTextPrimary
                    )
                    VyroPillBadge(
                        text = stringResource(R.string.projects_count_format, projects.size),
                        accentColor = VyroElectricPurple
                    )
                }
            }

            if (projects.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    val favoritesEmpty = uiState.showFavoritesOnly
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
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        (if (favoritesEmpty) VyroAmber else VyroElectricPurple)
                                            .copy(alpha = 0.16f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (favoritesEmpty) Icons.Rounded.Star else Icons.Rounded.MovieCreation,
                                    contentDescription = null,
                                    tint = if (favoritesEmpty) VyroAmber else VyroElectricPurple,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(
                                    if (favoritesEmpty) R.string.empty_favorites_title else R.string.empty_projects_title
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = VyroTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(
                                    if (favoritesEmpty) R.string.empty_favorites_subtitle else R.string.empty_projects_subtitle
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VyroTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectGridCard(
                        project = project,
                        onOpen = { onOpenProject(project.id) },
                        onEditName = { projectPendingRename = project },
                        onToggleFavorite = { viewModel.toggleFavorite(project.id) },
                        onExport = { onExportProject(project.id) },
                        onDuplicate = { viewModel.duplicateProject(project.id) },
                        onDelete = { projectPendingDelete = project }
                    )
                }
            }
        }

        // Add New Project FAB
        ExtendedFloatingActionButton(
            onClick = onCreateNewProject,
            shape = RoundedCornerShape(16.dp),
            containerColor = VyroElectricPurple,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.action_add_new_project),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .then(
                    if (widthSizeClass != WindowWidthSizeClass.Compact) {
                        Modifier.navigationBarsPadding()
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(VyroElectricPurpleBright, VyroNeonBlue)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .minimumInteractiveComponentSize()
                .testTag("add_new_project_fab")
        )
    }

    projectPendingRename?.let { pending ->
        var editedName by remember(pending.id) { mutableStateOf(pending.title) }
        var showNameError by remember(pending.id) { mutableStateOf(false) }

        val submitRename = {
            val trimmed = editedName.trim()
            if (trimmed.isEmpty()) {
                showNameError = true
            } else {
                viewModel.renameProject(pending.id, trimmed)
                projectPendingRename = null
            }
        }

        AlertDialog(
            onDismissRequest = { projectPendingRename = null },
            containerColor = VyroGraphiteElevated,
            title = {
                Text(
                    text = stringResource(R.string.dialog_rename_title),
                    color = VyroTextPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            if (showNameError && it.isNotBlank()) showNameError = false
                        },
                        label = { Text(stringResource(R.string.field_project_name)) },
                        placeholder = { Text(stringResource(R.string.field_project_name_hint)) },
                        singleLine = true,
                        isError = showNameError,
                        supportingText = if (showNameError) {
                            {
                                Text(
                                    text = stringResource(R.string.error_project_name_required),
                                    color = VyroRose
                                )
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitRename() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroElectricPurple,
                            unfocusedBorderColor = VyroBorderSubtle,
                            focusedLabelColor = VyroNeonBlue,
                            unfocusedLabelColor = VyroTextSecondary,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary,
                            cursorColor = VyroNeonBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_project_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = submitRename,
                    modifier = Modifier.testTag("rename_project_confirm_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_save),
                        color = VyroNeonBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { projectPendingRename = null },
                    modifier = Modifier.testTag("rename_project_cancel_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = VyroTextSecondary
                    )
                }
            }
        )
    }

    projectPendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            containerColor = VyroGraphiteElevated,
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    color = VyroTextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_message, pending.title),
                    color = VyroTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(pending.id)
                        projectPendingDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = VyroRose
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = VyroTextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun ProjectGridCard(
    project: VideoProject,
    onOpen: () -> Unit,
    onEditName: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExport: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firstClip = project.clips.firstOrNull()
    val thumbnailFile = remember(project.thumbnailPath, project.updatedAt) {
        project.thumbnailPath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
    }
    val dateText = remember(project.updatedAt) {
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(project.updatedAt))
    }

    VyroGlassCard(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Futuristic 16:10 Media Stage Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VyroObsidian)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                VyroElectricPurple.copy(alpha = 0.40f),
                                VyroNeonBlue.copy(alpha = 0.25f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                if (thumbnailFile != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbnailFile)
                            .memoryCacheKey("${thumbnailFile.absolutePath}_${project.updatedAt}")
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = project.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (firstClip != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(firstClip.uriString))
                            .apply {
                                if (firstClip.isVideo) {
                                    decoderFactory { result, options, _ ->
                                        VideoFrameDecoder(result.source, options)
                                    }
                                }
                            }
                            .crossfade(true)
                            .build(),
                        contentDescription = project.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FuturisticSlatePlaceholder(
                        aspectLabel = project.aspectRatio.ratioLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Scrim for sharp corner badges
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    VyroDeepBlack.copy(alpha = 0.36f),
                                    Color.Transparent,
                                    VyroDeepBlack.copy(alpha = 0.72f)
                                )
                            )
                        )
                )

                // Top-Start Aspect Ratio Pill + Favorite Indicator
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VyroDeepBlack.copy(alpha = 0.78f))
                            .border(1.dp, VyroNeonBlue.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.aspectRatio.ratioLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = VyroNeonBlue
                        )
                    }

                    if (project.isFavorite) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VyroDeepBlack.copy(alpha = 0.80f))
                                .border(1.dp, VyroAmber.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                .testTag("project_favorite_badge_${project.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = stringResource(R.string.filter_favorites),
                                tint = VyroAmber,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Top-End Overflow Menu
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("project_card_menu_${project.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(VyroDeepBlack.copy(alpha = 0.76f))
                                .border(1.dp, VyroBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.cd_project_options),
                                tint = VyroTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = VyroGraphiteElevated
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_open_workspace)) },
                            leadingIcon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = VyroNeonBlue
                                )
                            },
                            modifier = Modifier.testTag("project_menu_edit_name_${project.id}"),
                            onClick = {
                                menuExpanded = false
                                onEditName()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_toggle_favorite)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (project.isFavorite) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (project.isFavorite) VyroAmber else VyroTextSecondary
                                )
                            },
                            modifier = Modifier.testTag("project_menu_toggle_favorite_${project.id}"),
                            onClick = {
                                menuExpanded = false
                                onToggleFavorite()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_export)) },
                            leadingIcon = { Icon(Icons.Outlined.FileUpload, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onExport()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_duplicate)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    tint = VyroElectricPurpleBright
                                )
                            },
                            modifier = Modifier.testTag("project_menu_duplicate_${project.id}"),
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = VyroRose) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = VyroRose
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }

                // Bottom-Start FX Badge (if effects applied)
                if (project.appliedEffects.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VyroDeepBlack.copy(alpha = 0.80f))
                            .border(1.dp, VyroNeonCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.project_effects_count,
                                project.appliedEffects.size
                            ),
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = VyroNeonCyan
                        )
                    }
                }

                // Bottom-End Duration Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VyroDeepBlack.copy(alpha = 0.84f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = VyroTimeFormatter.formatDurationShort(project.totalDurationMs),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = VyroTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Project Metadata
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VyroTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VyroPillBadge(
                        text = stringResource(R.string.project_clips_count, project.clips.size),
                        accentColor = VyroElectricPurple
                    )
                    VyroPillBadge(
                        text = stringResource(R.string.project_fps_format, project.fps),
                        accentColor = VyroNeonBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun FuturisticSlatePlaceholder(
    aspectLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    VyroObsidian,
                    VyroGraphite
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / 6f
            val stepY = size.height / 4f
            for (i in 1..5) {
                drawLine(
                    color = VyroBorderSubtle.copy(alpha = 0.45f),
                    start = Offset(stepX * i, 0f),
                    end = Offset(stepX * i, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (j in 1..3) {
                drawLine(
                    color = VyroBorderSubtle.copy(alpha = 0.45f),
                    start = Offset(0f, stepY * j),
                    end = Offset(size.width, stepY * j),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VyroElectricPurple.copy(alpha = 0.28f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.55f
            )
            val reticleRadius = size.minDimension * 0.26f
            drawCircle(
                brush = Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue)),
                radius = reticleRadius,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Rounded.MovieCreation,
            contentDescription = aspectLabel,
            tint = VyroNeonBlue.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
    }
}
