package com.example.data.repository

import androidx.annotation.DrawableRes
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.MediaPickCandidate
import com.example.core.model.ProjectTemplate
import com.example.core.model.ResolutionPreset
import com.example.core.model.TimelineClip
import com.example.core.model.VideoProject
import com.example.data.Project
import com.example.data.local.AppliedEffectEntity
import com.example.data.local.ProjectDao
import com.example.data.local.TimelineClipEntity
import com.example.data.local.toDomain
import com.example.media.engine.ProjectThumbnailGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Mediates asynchronous access between [ProjectDao] (Room persistence) and UI/ViewModel layers.
 */
class ProjectRepository(
    private val projectDao: ProjectDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val thumbnailGenerator: ProjectThumbnailGenerator? = null
) {

    // --- Reactive Streams (Flow) ---

    val allProjects: Flow<List<VideoProject>> =
        projectDao.observeAllProjects()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    val allProjectMetadata: Flow<List<Project>> =
        projectDao.getAllProjects()
            .flowOn(ioDispatcher)

    val projectCount: Flow<Int> =
        projectDao.observeProjectCount()
            .flowOn(ioDispatcher)

    fun observeProject(projectId: Long): Flow<VideoProject?> =
        projectDao.observeProject(projectId)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    fun observeProjectMetadata(projectId: Long): Flow<Project?> =
        projectDao.observeProjectMetadata(projectId)
            .flowOn(ioDispatcher)

    // --- Project Metadata CRUD ---

    suspend fun insertProjectMetadata(project: Project): Long = withContext(ioDispatcher) {
        projectDao.insertProject(project)
    }

    suspend fun getProjectMetadataById(projectId: Long): Project? = withContext(ioDispatcher) {
        projectDao.getProjectById(projectId)
    }

    suspend fun updateProjectMetadata(project: Project) = withContext(ioDispatcher) {
        projectDao.updateProject(project.copy(lastModified = System.currentTimeMillis()))
    }

    suspend fun renameProject(projectId: Long, newName: String) = withContext(ioDispatcher) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            projectDao.renameProject(projectId, trimmed)
        }
    }

    suspend fun toggleFavorite(projectId: Long) = withContext(ioDispatcher) {
        val current = projectDao.getProjectById(projectId) ?: return@withContext
        projectDao.setProjectFavorite(projectId, !current.isFavorite)
    }

    suspend fun setFavorite(projectId: Long, isFavorite: Boolean) = withContext(ioDispatcher) {
        projectDao.setProjectFavorite(projectId, isFavorite)
    }

    suspend fun updateProjectThumbnail(projectId: Long, thumbnailPath: String?) =
        withContext(ioDispatcher) {
            val previous = projectDao.getProjectById(projectId)?.thumbnailPath
            projectDao.updateThumbnailPath(projectId, thumbnailPath)
            if (previous != null && previous != thumbnailPath) {
                thumbnailGenerator?.deleteThumbnail(previous)
            }
        }

    suspend fun deleteProjectMetadata(project: Project) = withContext(ioDispatcher) {
        projectDao.deleteProject(project)
        thumbnailGenerator?.deleteThumbnail(project.thumbnailPath)
    }

    suspend fun deleteProject(projectId: Long) = withContext(ioDispatcher) {
        val existingPath = projectDao.getProjectById(projectId)?.thumbnailPath
        projectDao.deleteProjectById(projectId)
        thumbnailGenerator?.deleteThumbnail(existingPath)
    }

    // --- Domain Project & Timeline Operations ---

    suspend fun getProjectOnce(projectId: Long): VideoProject? = withContext(ioDispatcher) {
        projectDao.getProjectOnce(projectId)?.toDomain()
    }

    suspend fun createProject(
        title: String,
        aspectRatio: AspectRatioPreset,
        fps: Int,
        resolution: ResolutionPreset,
        colorProfile: ColorProfile,
        templateId: String? = null,
        initialEffectIds: List<String> = emptyList(),
        @DrawableRes templateDrawableRes: Int? = null,
        initialMediaUri: String? = null,
        initialMediaIsVideo: Boolean = false
    ): Long = withContext(ioDispatcher) {
        val cleanTitle = title.trim()
        val now = System.currentTimeMillis()
        val projectId = projectDao.insertProject(
            Project(
                name = cleanTitle,
                creationTimestamp = now,
                lastModified = now,
                aspectRatioId = aspectRatio.id,
                fps = fps,
                resolutionId = resolution.id,
                colorProfileId = colorProfile.id,
                templateId = templateId
            )
        )

        if (initialEffectIds.isNotEmpty()) {
            val entities = initialEffectIds.mapIndexedNotNull { idx, fxId ->
                val preset = EffectsCatalogRepository.findById(fxId) ?: return@mapIndexedNotNull null
                AppliedEffectEntity(
                    projectId = projectId,
                    presetId = preset.id,
                    intensity = preset.defaultIntensity,
                    orderIndex = idx
                )
            }
            if (entities.isNotEmpty()) {
                projectDao.insertAppliedEffects(entities)
            }
        }

        val capturedOrGeneratedPath = if (!initialMediaUri.isNullOrBlank()) {
            thumbnailGenerator?.captureMediaThumbnail(
                projectId = projectId,
                mediaUriString = initialMediaUri,
                isVideo = initialMediaIsVideo
            )
        } else {
            null
        } ?: thumbnailGenerator?.generateInitialThumbnail(
            projectId = projectId,
            title = cleanTitle,
            aspectRatio = aspectRatio,
            fps = fps,
            templateDrawableRes = templateDrawableRes
        )

        if (!capturedOrGeneratedPath.isNullOrBlank()) {
            projectDao.updateThumbnailPath(projectId, capturedOrGeneratedPath, now)
        }

        projectId
    }

    suspend fun createProjectFromTemplate(template: ProjectTemplate): Long = withContext(ioDispatcher) {
        createProject(
            title = template.title,
            aspectRatio = template.aspectRatio,
            fps = template.fps,
            resolution = template.resolution,
            colorProfile = template.colorProfile,
            templateId = template.id,
            initialEffectIds = template.presetEffectIds,
            templateDrawableRes = template.previewDrawable
        )
    }

    suspend fun duplicateProject(projectId: Long): Long? = withContext(ioDispatcher) {
        val source = projectDao.getProjectOnce(projectId) ?: return@withContext null
        val now = System.currentTimeMillis()
        val duplicatedEntity = source.project.createDuplicate(timestamp = now)
        val newId = projectDao.insertProject(duplicatedEntity)
        if (source.clips.isNotEmpty()) {
            projectDao.insertClips(
                source.clips.map { it.copy(id = 0, projectId = newId) }
            )
        }
        if (source.effects.isNotEmpty()) {
            projectDao.insertAppliedEffects(
                source.effects.map { it.copy(id = 0, projectId = newId) }
            )
        }

        val copiedThumb = thumbnailGenerator?.copyThumbnail(source.project.thumbnailPath, newId)
            ?: thumbnailGenerator?.generateInitialThumbnail(
                projectId = newId,
                title = "${source.project.name} (Copy)",
                aspectRatio = AspectRatioPreset.fromId(source.project.aspectRatioId),
                fps = source.project.fps
            )
        if (!copiedThumb.isNullOrBlank()) {
            projectDao.updateThumbnailPath(newId, copiedThumb, now)
        }
        newId
    }

    suspend fun setMuted(projectId: Long, muted: Boolean) = withContext(ioDispatcher) {
        projectDao.setProjectMuted(projectId, muted)
    }

    suspend fun appendPickedMedia(projectId: Long, candidates: List<MediaPickCandidate>) =
        withContext(ioDispatcher) {
            if (candidates.isEmpty()) return@withContext
            val existingCount = projectDao.getMaxClipOrder(projectId) + 1
            val entities = candidates.mapIndexed { idx, candidate ->
                TimelineClipEntity(
                    projectId = projectId,
                    uriString = candidate.uriString,
                    displayName = candidate.displayName,
                    isVideo = candidate.isVideo,
                    durationMs = candidate.durationMs,
                    trimStartMs = 0L,
                    trimEndMs = candidate.durationMs,
                    width = candidate.width,
                    height = candidate.height,
                    orderIndex = existingCount + idx,
                    playbackSpeed = 1.0f
                )
            }
            projectDao.insertClips(entities)

            val project = projectDao.getProjectById(projectId)
            val currentThumb = project?.thumbnailPath
            val needsMediaCapture = currentThumb.isNullOrBlank() ||
                currentThumb.endsWith(".png") ||
                (existingCount == 0 && !currentThumb.endsWith(".jpg"))

            if (needsMediaCapture) {
                val first = candidates.first()
                val newThumb = thumbnailGenerator?.captureMediaThumbnail(
                    projectId = projectId,
                    mediaUriString = first.uriString,
                    isVideo = first.isVideo
                )
                if (!newThumb.isNullOrBlank()) {
                    projectDao.updateThumbnailPath(projectId, newThumb)
                    if (currentThumb != null && currentThumb != newThumb) {
                        thumbnailGenerator?.deleteThumbnail(currentThumb)
                    }
                    return@withContext
                }
            }
            projectDao.touchProject(projectId)
        }

    suspend fun updateClip(clip: TimelineClip) = withContext(ioDispatcher) {
        projectDao.updateClip(
            TimelineClipEntity(
                id = clip.id,
                projectId = clip.projectId,
                uriString = clip.uriString,
                displayName = clip.displayName,
                isVideo = clip.isVideo,
                durationMs = clip.durationMs,
                trimStartMs = clip.trimStartMs,
                trimEndMs = clip.trimEndMs,
                width = clip.width,
                height = clip.height,
                orderIndex = clip.orderIndex,
                playbackSpeed = clip.playbackSpeed
            )
        )
        projectDao.touchProject(clip.projectId)
    }

    suspend fun splitClip(projectId: Long, clipId: Long): Boolean = withContext(ioDispatcher) {
        val project = projectDao.getProjectOnce(projectId)?.toDomain() ?: return@withContext false
        val clip = project.clips.firstOrNull { it.id == clipId } ?: return@withContext false
        val start = clip.trimStartMs.coerceAtLeast(0L)
        val end = clip.trimEndMs.coerceAtMost(clip.durationMs)
        val midpoint = start + (end - start) / 2L
        if (midpoint - start < 500L || end - midpoint < 500L) return@withContext false

        fun entity(id: Long, from: Long, to: Long, order: Int, suffix: String) =
            TimelineClipEntity(
                id = id,
                projectId = clip.projectId,
                uriString = clip.uriString,
                displayName = clip.displayName + suffix,
                isVideo = clip.isVideo,
                durationMs = clip.durationMs,
                trimStartMs = from,
                trimEndMs = to,
                width = clip.width,
                height = clip.height,
                orderIndex = order,
                playbackSpeed = clip.playbackSpeed
            )

        projectDao.replaceClipWithSplit(
            projectId = projectId,
            originalClipId = clip.id,
            originalOrderIndex = clip.orderIndex,
            first = entity(0L, start, midpoint, clip.orderIndex, " (1)"),
            second = entity(0L, midpoint, end, clip.orderIndex + 1, " (2)")
        )
        projectDao.touchProject(projectId)
        true
    }

    suspend fun removeClip(projectId: Long, clipId: Long) = withContext(ioDispatcher) {
        val before = projectDao.getProjectOnce(projectId)
        val firstClipBefore = before?.clips?.minByOrNull { it.orderIndex }
        projectDao.deleteClipById(clipId)

        val after = projectDao.getProjectOnce(projectId)
        if (after != null) {
            val newFirst = after.clips.minByOrNull { it.orderIndex }
            val previousThumb = after.project.thumbnailPath
            if (newFirst == null && firstClipBefore != null) {
                val templateDrawable = after.project.templateId?.let {
                    TemplateCatalogRepository.findById(it)?.previewDrawable
                }
                val slateThumb = thumbnailGenerator?.generateInitialThumbnail(
                    projectId = projectId,
                    title = after.project.name,
                    aspectRatio = AspectRatioPreset.fromId(after.project.aspectRatioId),
                    fps = after.project.fps,
                    templateDrawableRes = templateDrawable
                )
                if (!slateThumb.isNullOrBlank()) {
                    projectDao.updateThumbnailPath(projectId, slateThumb)
                    if (previousThumb != null && previousThumb != slateThumb) {
                        thumbnailGenerator?.deleteThumbnail(previousThumb)
                    }
                    return@withContext
                }
            } else if (newFirst != null && firstClipBefore?.id == clipId) {
                val capturedThumb = thumbnailGenerator?.captureMediaThumbnail(
                    projectId = projectId,
                    mediaUriString = newFirst.uriString,
                    isVideo = newFirst.isVideo
                )
                if (!capturedThumb.isNullOrBlank()) {
                    projectDao.updateThumbnailPath(projectId, capturedThumb)
                    if (previousThumb != null && previousThumb != capturedThumb) {
                        thumbnailGenerator?.deleteThumbnail(previousThumb)
                    }
                    return@withContext
                }
            }
        }
        projectDao.touchProject(projectId)
    }

    suspend fun toggleEffect(projectId: Long, presetId: String) = withContext(ioDispatcher) {
        val current = projectDao.getProjectOnce(projectId) ?: return@withContext
        val existing = current.effects.firstOrNull { it.presetId == presetId }
        if (existing != null) {
            projectDao.deleteAppliedEffect(projectId, presetId)
        } else {
            val preset = EffectsCatalogRepository.findById(presetId) ?: return@withContext
            val nextOrder = (current.effects.maxOfOrNull { it.orderIndex } ?: -1) + 1
            projectDao.insertAppliedEffects(
                listOf(
                    AppliedEffectEntity(
                        projectId = projectId,
                        presetId = preset.id,
                        intensity = preset.defaultIntensity,
                        orderIndex = nextOrder
                    )
                )
            )
        }
        projectDao.touchProject(projectId)
    }

    suspend fun clearEffects(projectId: Long) = withContext(ioDispatcher) {
        projectDao.clearEffectsForProject(projectId)
        projectDao.touchProject(projectId)
    }
}
