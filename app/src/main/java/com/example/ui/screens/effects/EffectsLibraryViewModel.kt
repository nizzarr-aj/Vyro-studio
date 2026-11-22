package com.example.ui.screens.effects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.EffectCategory
import com.example.core.model.VideoEffectPreset
import com.example.core.model.VideoProject
import com.example.data.repository.EffectsCatalogRepository
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EffectsLibraryUiState(
    val activeProject: VideoProject? = null,
    val selectedCategory: EffectCategory? = null,
    val searchQuery: String = "",
    val presets: List<VideoEffectPreset> = EffectsCatalogRepository.presets
)

class EffectsLibraryViewModel(
    private val projectId: Long,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val categoryFlow = MutableStateFlow<EffectCategory?>(null)
    private val searchFlow = MutableStateFlow("")

    private val projectFlow = if (projectId > 0L) {
        projectRepository.observeProject(projectId)
    } else {
        projectRepository.allProjects.map { it.firstOrNull() }
    }

    val uiState: StateFlow<EffectsLibraryUiState> = combine(
        projectFlow,
        categoryFlow,
        searchFlow
    ) { project, category, query ->
        val filtered = EffectsCatalogRepository.presets.filter { preset ->
            val matchesCat = category == null || preset.category == category
            val matchesQuery = query.isBlank() ||
                preset.name.contains(query, ignoreCase = true) ||
                preset.description.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
        EffectsLibraryUiState(
            activeProject = project,
            selectedCategory = category,
            searchQuery = query,
            presets = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EffectsLibraryUiState()
    )

    fun setCategory(category: EffectCategory?) {
        categoryFlow.value = category
    }

    fun setSearchQuery(query: String) {
        searchFlow.value = query
    }

    fun toggleEffect(presetId: String) {
        val targetId = uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            projectRepository.toggleEffect(targetId, presetId)
        }
    }

    fun clearAllEffects() {
        val targetId = uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            projectRepository.clearEffects(targetId)
        }
    }

    class Factory(
        private val projectId: Long,
        private val projectRepository: ProjectRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EffectsLibraryViewModel(projectId, projectRepository) as T
        }
    }
}
