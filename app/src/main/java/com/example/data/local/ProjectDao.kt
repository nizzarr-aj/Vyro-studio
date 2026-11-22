package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    // --- Project Metadata CRUD ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): Project?

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun observeProjectMetadata(projectId: Long): Flow<Project?>

    @Update
    suspend fun updateProject(project: Project)

    @Query("UPDATE projects SET name = :name, lastModified = :lastModified WHERE id = :projectId")
    suspend fun renameProject(
        projectId: Long,
        name: String,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE projects SET lastModified = :lastModified WHERE id = :projectId")
    suspend fun touchProject(
        projectId: Long,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE projects SET isMuted = :muted, lastModified = :lastModified WHERE id = :projectId")
    suspend fun setProjectMuted(
        projectId: Long,
        muted: Boolean,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE projects SET thumbnailPath = :thumbnailPath, lastModified = :lastModified WHERE id = :projectId")
    suspend fun updateThumbnailPath(
        projectId: Long,
        thumbnailPath: String?,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE projects SET isFavorite = :isFavorite WHERE id = :projectId")
    suspend fun setProjectFavorite(
        projectId: Long,
        isFavorite: Boolean
    )

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("SELECT COUNT(*) FROM projects")
    fun observeProjectCount(): Flow<Int>

    // --- Relational Project + Timeline Clips + Effects ---

    @Transaction
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun observeAllProjects(): Flow<List<ProjectWithRelations>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun observeProject(projectId: Long): Flow<ProjectWithRelations?>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectOnce(projectId: Long): ProjectWithRelations?

    // --- Timeline Clips & Applied Effects ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClips(clips: List<TimelineClipEntity>)

    @Update
    suspend fun updateClip(clip: TimelineClipEntity)

    @Query("DELETE FROM timeline_clips WHERE id = :clipId")
    suspend fun deleteClipById(clipId: Long)

    @Query("UPDATE timeline_clips SET orderIndex = orderIndex + 1 WHERE projectId = :projectId AND orderIndex > :orderIndex")
    suspend fun shiftClipsAfter(projectId: Long, orderIndex: Int)

    @Transaction
    suspend fun replaceClipWithSplit(
        projectId: Long,
        originalClipId: Long,
        originalOrderIndex: Int,
        first: TimelineClipEntity,
        second: TimelineClipEntity
    ) {
        shiftClipsAfter(projectId, originalOrderIndex)
        deleteClipById(originalClipId)
        insertClips(listOf(first, second))
    }

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM timeline_clips WHERE projectId = :projectId")
    suspend fun getMaxClipOrder(projectId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliedEffects(effects: List<AppliedEffectEntity>)

    @Query("DELETE FROM applied_effects WHERE projectId = :projectId AND presetId = :presetId")
    suspend fun deleteAppliedEffect(projectId: Long, presetId: String)

    @Query("DELETE FROM applied_effects WHERE projectId = :projectId")
    suspend fun clearEffectsForProject(projectId: Long)
}
