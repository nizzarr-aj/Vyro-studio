package com.example

import com.example.core.model.AspectRatioPreset
import com.example.core.model.UserPreferences
import com.example.data.Project
import com.example.data.local.AppliedEffectEntity
import com.example.data.local.ProjectDao
import com.example.data.local.ProjectWithRelations
import com.example.data.local.TimelineClipEntity
import com.example.data.repository.ProjectRepository
import com.example.ui.viewmodel.ProjectSortOption
import com.example.ui.viewmodel.ProjectViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class InMemoryProjectDao : ProjectDao {
        private val projects = MutableStateFlow<List<Project>>(emptyList())
        private var nextId = 1L

        override suspend fun insertProject(project: Project): Long {
            val id = if (project.id == 0L) nextId++ else project.id
            projects.value = projects.value.filterNot { it.id == id } + project.copy(id = id)
            return id
        }

        override fun getAllProjects(): Flow<List<Project>> = projects
        override suspend fun getProjectById(projectId: Long): Project? =
            projects.value.firstOrNull { it.id == projectId }

        override fun observeProjectMetadata(projectId: Long): Flow<Project?> =
            projects.map { list -> list.firstOrNull { it.id == projectId } }

        override suspend fun updateProject(project: Project) {
            projects.value = projects.value.map { if (it.id == project.id) project else it }
        }

        override suspend fun renameProject(projectId: Long, name: String, lastModified: Long) {
            projects.value = projects.value.map {
                if (it.id == projectId) it.copy(name = name, lastModified = lastModified) else it
            }
        }

        override suspend fun touchProject(projectId: Long, lastModified: Long) {}
        override suspend fun setProjectMuted(projectId: Long, muted: Boolean, lastModified: Long) {}
        override suspend fun updateThumbnailPath(
            projectId: Long,
            thumbnailPath: String?,
            lastModified: Long
        ) {
            projects.value = projects.value.map {
                if (it.id == projectId) it.copy(thumbnailPath = thumbnailPath, lastModified = lastModified) else it
            }
        }

        override suspend fun setProjectFavorite(projectId: Long, isFavorite: Boolean) {
            projects.value = projects.value.map {
                if (it.id == projectId) it.copy(isFavorite = isFavorite) else it
            }
        }
        override suspend fun deleteProject(project: Project) = deleteProjectById(project.id)
        override suspend fun deleteProjectById(projectId: Long) {
            projects.value = projects.value.filterNot { it.id == projectId }
        }

        override fun observeProjectCount(): Flow<Int> = projects.map { it.size }
        override fun observeAllProjects(): Flow<List<ProjectWithRelations>> =
            projects.map { list -> list.map { ProjectWithRelations(it, emptyList(), emptyList()) } }

        override fun observeProject(projectId: Long): Flow<ProjectWithRelations?> =
            observeAllProjects().map { list -> list.firstOrNull { it.project.id == projectId } }

        override suspend fun getProjectOnce(projectId: Long): ProjectWithRelations? =
            observeProject(projectId).first()

        override suspend fun insertClips(clips: List<TimelineClipEntity>) {}
        override suspend fun updateClip(clip: TimelineClipEntity) {}
        override suspend fun deleteClipById(clipId: Long) {}
        override suspend fun shiftClipsAfter(projectId: Long, orderIndex: Int) {}
        override suspend fun getClipsForProject(projectId: Long): List<TimelineClipEntity> = emptyList()
        override suspend fun updateClipOrder(clipId: Long, orderIndex: Int) {}
        override suspend fun getMaxClipOrder(projectId: Long): Int = -1
        override suspend fun insertAppliedEffects(effects: List<AppliedEffectEntity>) {}
        override suspend fun deleteAppliedEffect(projectId: Long, presetId: String) {}
        override suspend fun clearEffectsForProject(projectId: Long) {}
    }

    @Test
    fun `create and delete project updates ProjectViewModel StateFlow streams`() = runTest {
        val dao = InMemoryProjectDao()
        val repository = ProjectRepository(dao, testDispatcher)
        val viewModel = ProjectViewModel(repository, flowOf(UserPreferences()))

        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.projects.collect {}
        }
        val uiCollectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        var createdId = -1L
        viewModel.createProject(
            name = "Cyber Horizon",
            aspectRatio = AspectRatioPreset.RATIO_9_16
        ) { id -> createdId = id }

        assertEquals(1, viewModel.projects.value.size)
        assertEquals("Cyber Horizon", viewModel.projects.value.first().title)
        assertEquals(1, viewModel.uiState.value.projects.size)

        viewModel.renameProject(createdId, "  Cyber Horizon Director's Cut  ")
        assertEquals("Cyber Horizon Director's Cut", viewModel.projects.value.first().title)
        assertEquals("Cyber Horizon Director's Cut", viewModel.uiState.value.projects.first().title)

        viewModel.renameProject(createdId, "   ")
        assertEquals("Cyber Horizon Director's Cut", viewModel.projects.value.first().title)

        viewModel.deleteProject(createdId)
        assertEquals(0, viewModel.projects.value.size)
        assertEquals(0, viewModel.uiState.value.projects.size)

        collectJob.cancel()
        uiCollectJob.cancel()
    }

    @Test
    fun `setSortOption sorts projects by Most Recent or A-Z`() = runTest {
        val dao = InMemoryProjectDao()
        dao.insertProject(Project(id = 1, name = "Zebra Cut", lastModified = 1_000L))
        dao.insertProject(Project(id = 2, name = "Alpha Reel", lastModified = 2_000L))
        dao.insertProject(Project(id = 3, name = "midnight Drift", lastModified = 3_000L))

        val repository = ProjectRepository(dao, testDispatcher)
        val viewModel = ProjectViewModel(repository, flowOf(UserPreferences()))

        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.projects.collect {}
        }
        val uiCollectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        // Default: Most Recent (descending lastModified)
        assertEquals(ProjectSortOption.MOST_RECENT, viewModel.sortOption.value)
        assertEquals(
            listOf("midnight Drift", "Alpha Reel", "Zebra Cut"),
            viewModel.uiState.value.projects.map { it.title }
        )

        // Switch to A-Z (case-insensitive)
        viewModel.setSortOption(ProjectSortOption.ALPHABETICAL_AZ)
        assertEquals(ProjectSortOption.ALPHABETICAL_AZ, viewModel.uiState.value.sortOption)
        assertEquals(
            listOf("Alpha Reel", "midnight Drift", "Zebra Cut"),
            viewModel.uiState.value.projects.map { it.title }
        )
        assertEquals(
            listOf("Alpha Reel", "midnight Drift", "Zebra Cut"),
            viewModel.projects.value.map { it.title }
        )

        // Switch back to Most Recent
        viewModel.setSortOption(ProjectSortOption.MOST_RECENT)
        assertEquals(
            listOf("midnight Drift", "Alpha Reel", "Zebra Cut"),
            viewModel.uiState.value.projects.map { it.title }
        )

        collectJob.cancel()
        uiCollectJob.cancel()
    }

    @Test
    fun `toggleFavorite and toggleFavoritesFilter filter projects in ProjectViewModel`() = runTest {
        val dao = InMemoryProjectDao()
        dao.insertProject(Project(id = 1, name = "Alpha", lastModified = 1_000L, isFavorite = false))
        dao.insertProject(Project(id = 2, name = "Beta", lastModified = 2_000L, isFavorite = true))

        val repository = ProjectRepository(dao, testDispatcher)
        val viewModel = ProjectViewModel(repository, flowOf(UserPreferences()))

        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.projects.collect {}
        }
        val uiCollectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        assertEquals(false, viewModel.showFavoritesOnly.value)
        assertEquals(2, viewModel.uiState.value.projects.size)

        // Enable favorites filter -> only Beta
        viewModel.toggleFavoritesFilter()
        assertEquals(true, viewModel.uiState.value.showFavoritesOnly)
        assertEquals(listOf("Beta"), viewModel.uiState.value.projects.map { it.title })

        // Favorite Alpha -> both Alpha and Beta appear
        viewModel.toggleFavorite(1L)
        assertEquals(listOf("Beta", "Alpha"), viewModel.uiState.value.projects.map { it.title })

        // Unfavorite Beta -> only Alpha remains in favorites filter
        viewModel.toggleFavorite(2L)
        assertEquals(listOf("Alpha"), viewModel.uiState.value.projects.map { it.title })

        // Turn off favorites filter -> both appear again
        viewModel.toggleFavoritesFilter()
        assertEquals(false, viewModel.uiState.value.showFavoritesOnly)
        assertEquals(2, viewModel.uiState.value.projects.size)

        collectJob.cancel()
        uiCollectJob.cancel()
    }

    @Test
    fun `duplicateProject by id and by VideoProject emits copied project and invokes callback`() = runTest {
        val dao = InMemoryProjectDao()
        val repository = ProjectRepository(dao, testDispatcher)
        val viewModel = ProjectViewModel(repository, flowOf(UserPreferences()))

        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.projects.collect {}
        }
        val uiCollectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        var originalId = -1L
        viewModel.createProject(
            name = "Midnight Cut",
            aspectRatio = AspectRatioPreset.RATIO_9_16
        ) { id -> originalId = id }
        viewModel.toggleFavorite(originalId)

        var duplicatedIdById = -1L
        viewModel.duplicateProject(originalId) { newId -> duplicatedIdById = newId }

        assertNotEquals(-1L, duplicatedIdById)
        assertNotEquals(originalId, duplicatedIdById)
        assertEquals(2, viewModel.projects.value.size)

        val copiedProject = viewModel.uiState.value.projects.first { it.id == duplicatedIdById }
        assertEquals("Midnight Cut (Copy)", copiedProject.title)
        assertEquals(AspectRatioPreset.RATIO_9_16, copiedProject.aspectRatio)
        assertFalse(copiedProject.isFavorite)

        var duplicatedIdByModel = -1L
        val originalModel = viewModel.projects.value.first { it.id == originalId }
        viewModel.duplicateProject(originalModel) { newId -> duplicatedIdByModel = newId }

        assertNotEquals(-1L, duplicatedIdByModel)
        assertNotEquals(duplicatedIdById, duplicatedIdByModel)
        assertEquals(3, viewModel.uiState.value.projects.size)

        collectJob.cancel()
        uiCollectJob.cancel()
    }
}
