package com.example.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.ResolutionPreset
import com.example.core.model.UserPreferences
import com.example.core.model.VideoProject
import com.example.data.Project
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProjectSortOption(@StringRes val labelRes: Int) {
    MOST_RECENT(R.string.sort_most_recent),
    ALPHABETICAL_AZ(R.string.sort_alphabetical)
}

data class DashboardUiState(
    val projects: List<VideoProject> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val sortOption: ProjectSortOption = ProjectSortOption.MOST_RECENT,
    val showFavoritesOnly: Boolean = false
)

class ProjectViewModel(
    private val projectRepository: ProjectRepository,
    private val preferencesFlow: Flow<UserPreferences> = flowOf(UserPreferences())
) : ViewModel() {

    private val _sortOption = MutableStateFlow(ProjectSortOption.MOST_RECENT)
    val sortOption: StateFlow<ProjectSortOption> = _sortOption.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val filteredSortedProjectsFlow: Flow<List<VideoProject>> = combine(
        projectRepository.allProjects,
        _sortOption,
        _showFavoritesOnly
    ) { projectList, sort, favoritesOnly ->
        val filtered = if (favoritesOnly) {
            projectList.filter { it.isFavorite }
        } else {
            projectList
        }
        sortProjects(filtered, sort)
    }

    val projects: StateFlow<List<VideoProject>> = filteredSortedProjectsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val projectMetadata: StateFlow<List<Project>> = projectRepository.allProjectMetadata
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<DashboardUiState> = combine(
        filteredSortedProjectsFlow,
        preferencesFlow,
        _sortOption,
        _showFavoritesOnly
    ) { projectList, prefs, sort, favoritesOnly ->
        DashboardUiState(
            projects = projectList,
            preferences = prefs,
            sortOption = sort,
            showFavoritesOnly = favoritesOnly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun setSortOption(option: ProjectSortOption) {
        _sortOption.value = option
    }

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun setFavoritesOnly(enabled: Boolean) {
        _showFavoritesOnly.value = enabled
    }

    private fun sortProjects(
        list: List<VideoProject>,
        sort: ProjectSortOption
    ): List<VideoProject> = when (sort) {
        ProjectSortOption.MOST_RECENT -> list.sortedByDescending { it.updatedAt }
        ProjectSortOption.ALPHABETICAL_AZ -> list.sortedWith(
            compareBy<VideoProject, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
                .thenByDescending { it.updatedAt }
        )
    }

    fun createProject(
        name: String,
        aspectRatio: AspectRatioPreset = AspectRatioPreset.RATIO_16_9,
        fps: Int? = null,
        resolution: ResolutionPreset? = null,
        colorProfile: ColorProfile = ColorProfile.REC_709,
        onCreated: (Long) -> Unit = {}
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val prefs = preferencesFlow.first()
            val id = projectRepository.createProject(
                title = trimmed,
                aspectRatio = aspectRatio,
                fps = fps ?: prefs.defaultFps,
                resolution = resolution ?: prefs.defaultResolution,
                colorProfile = colorProfile
            )
            onCreated(id)
        }
    }

    fun renameProject(projectId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            projectRepository.renameProject(projectId, trimmed)
        }
    }

    fun toggleFavorite(projectId: Long) {
        viewModelScope.launch {
            projectRepository.toggleFavorite(projectId)
        }
    }

    fun duplicateProject(
        projectId: Long,
        onDuplicated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val duplicatedId = projectRepository.duplicateProject(projectId)
            if (duplicatedId != null) {
                onDuplicated(duplicatedId)
            }
        }
    }

    fun duplicateProject(
        project: VideoProject,
        onDuplicated: (Long) -> Unit = {}
    ) {
        duplicateProject(project.id, onDuplicated)
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            projectRepository.deleteProjectMetadata(project)
        }
    }

    class Factory(
        private val projectRepository: ProjectRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectViewModel(projectRepository, preferencesRepository.preferences) as T
        }
    }
}
