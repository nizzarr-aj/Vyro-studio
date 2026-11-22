package com.example

import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.MediaPickCandidate
import com.example.core.model.ResolutionPreset
import com.example.data.Project
import com.example.data.local.AppliedEffectEntity
import com.example.data.local.ProjectDao
import com.example.data.local.ProjectWithRelations
import com.example.data.local.TimelineClipEntity
import com.example.data.repository.ProjectRepository
import com.example.media.engine.ProjectThumbnailGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectRepositoryCrudTest {

    private class FakeProjectDao : ProjectDao {
        private val projects = MutableStateFlow<List<Project>>(emptyList())
        private val clips = MutableStateFlow<List<TimelineClipEntity>>(emptyList())
        private val effects = MutableStateFlow<List<AppliedEffectEntity>>(emptyList())
        private var nextId = 1L

        override suspend fun insertProject(project: Project): Long {
            val id = if (project.id == 0L) nextId++ else project.id
            val stored = project.copy(id = id)
            projects.value = projects.value.filterNot { it.id == id } + stored
            return id
        }

        override fun getAllProjects(): Flow<List<Project>> =
            projects.map { list -> list.sortedByDescending { it.lastModified } }

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

        override suspend fun touchProject(projectId: Long, lastModified: Long) {
            projects.value = projects.value.map {
                if (it.id == projectId) it.copy(lastModified = lastModified) else it
            }
        }

        override suspend fun setProjectMuted(projectId: Long, muted: Boolean, lastModified: Long) {
            projects.value = projects.value.map {
                if (it.id == projectId) it.copy(isMuted = muted, lastModified = lastModified) else it
            }
        }

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

        override suspend fun deleteProject(project: Project) {
            deleteProjectById(project.id)
        }

        override suspend fun deleteProjectById(projectId: Long) {
            projects.value = projects.value.filterNot { it.id == projectId }
            clips.value = clips.value.filterNot { it.projectId == projectId }
            effects.value = effects.value.filterNot { it.projectId == projectId }
        }

        override fun observeProjectCount(): Flow<Int> = projects.map { it.size }

        override fun observeAllProjects(): Flow<List<ProjectWithRelations>> =
            projects.map { list ->
                list.sortedByDescending { it.lastModified }.map { p ->
                    ProjectWithRelations(
                        project = p,
                        clips = clips.value.filter { it.projectId == p.id },
                        effects = effects.value.filter { it.projectId == p.id }
                    )
                }
            }

        override fun observeProject(projectId: Long): Flow<ProjectWithRelations?> =
            observeAllProjects().map { list -> list.firstOrNull { it.project.id == projectId } }

        override suspend fun getProjectOnce(projectId: Long): ProjectWithRelations? =
            observeProject(projectId).first()

        override suspend fun insertClips(clips: List<TimelineClipEntity>) {
            this.clips.value = this.clips.value + clips
        }

        override suspend fun updateClip(clip: TimelineClipEntity) {
            this.clips.value = this.clips.value.map { if (it.id == clip.id) clip else it }
        }

        override suspend fun deleteClipById(clipId: Long) {
            this.clips.value = this.clips.value.filterNot { it.id == clipId }
        }

        override suspend fun getMaxClipOrder(projectId: Long): Int =
            clips.value.filter { it.projectId == projectId }.maxOfOrNull { it.orderIndex } ?: -1

        override suspend fun insertAppliedEffects(effects: List<AppliedEffectEntity>) {
            this.effects.value = this.effects.value + effects
        }

        override suspend fun deleteAppliedEffect(projectId: Long, presetId: String) {
            this.effects.value = this.effects.value.filterNot {
                it.projectId == projectId && it.presetId == presetId
            }
        }

        override suspend fun clearEffectsForProject(projectId: Long) {
            this.effects.value = this.effects.value.filterNot { it.projectId == projectId }
        }
    }

    @Test
    fun `project metadata CRUD operations work across repository and DAO contract`() = runTest {
        val dao = FakeProjectDao()
        val repository = ProjectRepository(dao, kotlinx.coroutines.Dispatchers.Unconfined)

        // Direct Project metadata insert & read
        val metaId = repository.insertProjectMetadata(Project(name = "Studio Intro"))
        val fetchedMeta = repository.getProjectMetadataById(metaId)
        assertNotNull(fetchedMeta)
        assertEquals("Studio Intro", fetchedMeta?.name)
        assertEquals(1, repository.allProjectMetadata.first().size)

        // Domain project create, rename, update, delete
        val id = repository.createProject(
            title = "Neon Skyline",
            aspectRatio = AspectRatioPreset.RATIO_9_16,
            fps = 60,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709
        )

        val created = repository.getProjectMetadataById(id)
        assertNotNull(created)
        assertEquals("Neon Skyline", created?.name)

        repository.updateProjectMetadata(created!!.copy(name = "Neon Skyline v2"))
        assertEquals("Neon Skyline v2", repository.observeProjectMetadata(id).first()?.name)

        repository.renameProject(id, "Tokyo Drift Master")
        val renamed = repository.getProjectOnce(id)
        assertEquals("Tokyo Drift Master", renamed?.title)

        repository.deleteProject(id)
        repository.deleteProjectMetadata(fetchedMeta!!)
        assertNull(repository.getProjectMetadataById(id))
        assertEquals(0, repository.projectCount.first())
    }

    @Test
    fun `project initialization generates thumbnail and media import captures frame thumbnail`() = runTest {
        val dao = FakeProjectDao()
        val deletedPaths = mutableListOf<String>()
        val fakeThumbGen = object : ProjectThumbnailGenerator {
            override suspend fun generateInitialThumbnail(
                projectId: Long,
                title: String,
                aspectRatio: AspectRatioPreset,
                fps: Int,
                templateDrawableRes: Int?
            ): String = "/tmp/slate_$projectId.png"

            override suspend fun captureMediaThumbnail(
                projectId: Long,
                mediaUriString: String,
                isVideo: Boolean
            ): String = "/tmp/media_${projectId}_$isVideo.jpg"

            override suspend fun copyThumbnail(sourcePath: String?, targetProjectId: Long): String =
                "/tmp/copy_$targetProjectId.jpg"

            override suspend fun deleteThumbnail(thumbnailPath: String?) {
                if (thumbnailPath != null) deletedPaths += thumbnailPath
            }
        }

        val repository = ProjectRepository(
            projectDao = dao,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
            thumbnailGenerator = fakeThumbGen
        )

        val id = repository.createProject(
            title = "Blank Slate",
            aspectRatio = AspectRatioPreset.RATIO_16_9,
            fps = 30,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709
        )
        assertEquals("/tmp/slate_$id.png", repository.getProjectOnce(id)?.thumbnailPath)

        repository.appendPickedMedia(
            id,
            listOf(
                MediaPickCandidate(
                    uriString = "content://media/video/42",
                    displayName = "Clip.mp4",
                    mimeType = "video/mp4",
                    isVideo = true,
                    durationMs = 5_000L,
                    width = 1920,
                    height = 1080
                )
            )
        )
        assertEquals("/tmp/media_${id}_true.jpg", repository.getProjectOnce(id)?.thumbnailPath)
        assertEquals(listOf("/tmp/slate_$id.png"), deletedPaths)

        val copyId = repository.duplicateProject(id)!!
        assertEquals("/tmp/copy_$copyId.jpg", repository.getProjectOnce(copyId)?.thumbnailPath)

        repository.deleteProject(id)
        assertEquals(
            listOf("/tmp/slate_$id.png", "/tmp/media_${id}_true.jpg"),
            deletedPaths
        )
    }

    @Test
    fun `project createDuplicate resets id favorite and thumbnail while repository clones clips and effects`() = runTest {
        val original = Project(
            id = 42L,
            name = "Neon Drift",
            creationTimestamp = 1_000L,
            lastModified = 2_000L,
            aspectRatioId = AspectRatioPreset.RATIO_9_16.id,
            fps = 60,
            resolutionId = ResolutionPreset.UHD_4K.id,
            colorProfileId = ColorProfile.HDR10_HLG.id,
            isMuted = true,
            templateId = "cyber_reel",
            thumbnailPath = "/tmp/original.png",
            isFavorite = true
        )

        val entityCopy = original.createDuplicate(timestamp = 9_999L)
        assertEquals(0L, entityCopy.id)
        assertEquals("Neon Drift (Copy)", entityCopy.name)
        assertEquals(9_999L, entityCopy.creationTimestamp)
        assertEquals(9_999L, entityCopy.lastModified)
        assertNull(entityCopy.thumbnailPath)
        assertFalse(entityCopy.isFavorite)
        assertEquals(original.aspectRatioId, entityCopy.aspectRatioId)
        assertEquals(original.fps, entityCopy.fps)
        assertEquals(original.resolutionId, entityCopy.resolutionId)
        assertEquals(original.colorProfileId, entityCopy.colorProfileId)
        assertEquals(original.isMuted, entityCopy.isMuted)
        assertEquals(original.templateId, entityCopy.templateId)

        val dao = FakeProjectDao()
        val repository = ProjectRepository(dao, kotlinx.coroutines.Dispatchers.Unconfined)
        val sourceId = repository.createProject(
            title = "Neon Drift",
            aspectRatio = AspectRatioPreset.RATIO_9_16,
            fps = 60,
            resolution = ResolutionPreset.UHD_4K,
            colorProfile = ColorProfile.HDR10_HLG,
            initialEffectIds = listOf("fx_cyber_neon", "fx_warm_cinema")
        )
        repository.toggleFavorite(sourceId)
        repository.setMuted(sourceId, true)
        repository.appendPickedMedia(
            sourceId,
            listOf(
                MediaPickCandidate(
                    uriString = "content://media/video/101",
                    displayName = "Scene1.mp4",
                    mimeType = "video/mp4",
                    isVideo = true,
                    durationMs = 4_000L,
                    width = 1080,
                    height = 1920
                )
            )
        )

        val duplicatedId = repository.duplicateProject(sourceId)!!
        assertNotEquals(sourceId, duplicatedId)

        val duplicated = repository.getProjectOnce(duplicatedId)
        assertNotNull(duplicated)
        assertEquals("Neon Drift (Copy)", duplicated?.title)
        assertFalse(duplicated!!.isFavorite)
        assertEquals(AspectRatioPreset.RATIO_9_16, duplicated.aspectRatio)
        assertEquals(60, duplicated.fps)
        assertEquals(ResolutionPreset.UHD_4K, duplicated.resolution)
        assertEquals(ColorProfile.HDR10_HLG, duplicated.colorProfile)
        assertEquals(true, duplicated.isMuted)
        assertEquals(1, duplicated.clips.size)
        assertEquals("Scene1.mp4", duplicated.clips.first().displayName)
        assertEquals(listOf("fx_cyber_neon", "fx_warm_cinema"), duplicated.appliedEffects.map { it.presetId })
    }
}
