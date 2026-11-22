package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val creationTimestamp: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val aspectRatioId: String = "16:9",
    val fps: Int = 30,
    val resolutionId: String = "1080p",
    val colorProfileId: String = "rec709",
    val isMuted: Boolean = false,
    val templateId: String? = null,
    val thumbnailPath: String? = null,
    val isFavorite: Boolean = false
) {
    fun createDuplicate(
        duplicateName: String = "$name (Copy)",
        timestamp: Long = System.currentTimeMillis()
    ): Project = copy(
        id = 0L,
        name = duplicateName.trim().ifEmpty { "$name (Copy)" },
        creationTimestamp = timestamp,
        lastModified = timestamp,
        thumbnailPath = null,
        isFavorite = false
    )
}
