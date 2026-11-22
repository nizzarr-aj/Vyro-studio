package com.example.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.ProjectTemplate
import com.example.core.model.TemplateCategory
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TemplateCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplatesUiState(
    val selectedCategory: TemplateCategory? = null,
    val templates: List<ProjectTemplate> = TemplateCatalogRepository.templates,
    val isCreating: Boolean = false
)

class TemplatesLibraryViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    fun selectCategory(category: TemplateCategory?) {
        val filtered = if (category == null) {
            TemplateCatalogRepository.templates
        } else {
            TemplateCatalogRepository.templates.filter { it.category == category }
        }
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            templates = filtered
        )
    }

    fun applyTemplate(template: ProjectTemplate, onCreated: (Long) -> Unit) {
        if (_uiState.value.isCreating) return
        _uiState.value = _uiState.value.copy(isCreating = true)
        viewModelScope.launch {
            val newId = projectRepository.createProjectFromTemplate(template)
            _uiState.value = _uiState.value.copy(isCreating = false)
            onCreated(newId)
        }
    }

    class Factory(
        private val projectRepository: ProjectRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplatesLibraryViewModel(projectRepository) as T
        }
    }
}
