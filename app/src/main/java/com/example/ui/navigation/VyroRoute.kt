package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface VyroRoute {
    @Serializable
    data object Splash : VyroRoute

    @Serializable
    data object Dashboard : VyroRoute

    @Serializable
    data object NewProject : VyroRoute

    @Serializable
    data class MediaImport(val projectId: Long = -1L) : VyroRoute

    @Serializable
    data class Workspace(val projectId: Long) : VyroRoute

    @Serializable
    data class EffectsLibrary(val projectId: Long = -1L) : VyroRoute

    @Serializable
    data object TemplatesLibrary : VyroRoute

    @Serializable
    data class Export(val projectId: Long) : VyroRoute

    @Serializable
    data object Settings : VyroRoute
}
