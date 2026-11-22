package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.core.model.AppliedEffect
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.ResolutionPreset
import com.example.core.model.TimelineClip
import com.example.core.model.VideoProject
import com.example.data.Project

@Entity(
    tableName = "timeline_clips",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class TimelineClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val uriString: String,
    val displayName: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val width: Int,
    val height: Int,
    val orderIndex: Int,
    val playbackSpeed: Float = 1.0f
)

@Entity(
    tableName = "applied_effects",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class AppliedEffectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val presetId: String,
    val intensity: Float,
    val orderIndex: Int
)

data class ProjectWithRelations(
    @Embedded val project: Project,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val clips: List<TimelineClipEntity>,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val effects: List<AppliedEffectEntity>
)

fun ProjectWithRelations.toDomain(): VideoProject = VideoProject(
    id = project.id,
    title = project.name,
    aspectRatio = AspectRatioPreset.fromId(project.aspectRatioId),
    fps = project.fps,
    resolution = ResolutionPreset.fromId(project.resolutionId),
    colorProfile = ColorProfile.fromId(project.colorProfileId),
    isMuted = project.isMuted,
    templateId = project.templateId,
    thumbnailPath = project.thumbnailPath,
    isFavorite = project.isFavorite,
    createdAt = project.creationTimestamp,
    updatedAt = project.lastModified,
    clips = clips.sortedBy { it.orderIndex }.map { it.toDomain() },
    appliedEffects = effects.sortedBy { it.orderIndex }.map { it.toDomain() }
)

fun TimelineClipEntity.toDomain(): TimelineClip = TimelineClip(
    id = id,
    projectId = projectId,
    uriString = uriString,
    displayName = displayName,
    isVideo = isVideo,
    durationMs = durationMs,
    trimStartMs = trimStartMs,
    trimEndMs = trimEndMs,
    width = width,
    height = height,
    orderIndex = orderIndex,
    playbackSpeed = playbackSpeed
)

fun AppliedEffectEntity.toDomain(): AppliedEffect = AppliedEffect(
    id = id,
    projectId = projectId,
    presetId = presetId,
    intensity = intensity,
    orderIndex = orderIndex
)
