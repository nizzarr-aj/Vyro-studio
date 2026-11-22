package com.example.core.di

import android.app.Application
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ProjectRepository
import com.example.media.engine.AndroidProjectThumbnailGenerator
import com.example.media.engine.MediaMetadataExtractor
import com.example.media.engine.ProjectThumbnailGenerator

class VyroAppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val projectThumbnailGenerator: ProjectThumbnailGenerator by lazy {
        AndroidProjectThumbnailGenerator(appContext)
    }

    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(
            projectDao = database.projectDao(),
            thumbnailGenerator = projectThumbnailGenerator
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val mediaMetadataExtractor: MediaMetadataExtractor by lazy {
        MediaMetadataExtractor(appContext)
    }
}

class VyroApplication : Application() {
    lateinit var container: VyroAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = VyroAppContainer(this)
    }
}
