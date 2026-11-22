package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesomeMosaic
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.core.di.VyroAppContainer
import com.example.ui.components.VyroPrismLogo
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.viewmodel.ProjectViewModel
import com.example.ui.screens.effects.EffectsLibraryScreen
import com.example.ui.screens.effects.EffectsLibraryViewModel
import com.example.ui.screens.export.ExportScreen
import com.example.ui.screens.export.ExportViewModel
import com.example.ui.screens.mediaimport.MediaImportScreen
import com.example.ui.screens.mediaimport.MediaImportViewModel
import com.example.ui.screens.newproject.NewProjectScreen
import com.example.ui.screens.newproject.NewProjectViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.templates.TemplatesLibraryScreen
import com.example.ui.screens.templates.TemplatesLibraryViewModel
import com.example.ui.screens.workspace.WorkspaceScreen
import com.example.ui.screens.workspace.WorkspaceViewModel
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroGraphite
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroPurpleContainer
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

private data class TopLevelNavItem(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: VyroRoute,
    val testTag: String
)

@Composable
fun VyroStudioApp(
    container: VyroAppContainer,
    widthSizeClass: WindowWidthSizeClass
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val appContext = LocalContext.current.applicationContext

    val topLevelItems = listOf(
        TopLevelNavItem(
            labelRes = R.string.nav_dashboard,
            selectedIcon = Icons.Rounded.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            route = VyroRoute.Dashboard,
            testTag = "nav_tab_dashboard"
        ),
        TopLevelNavItem(
            labelRes = R.string.nav_templates,
            selectedIcon = Icons.Rounded.AutoAwesomeMosaic,
            unselectedIcon = Icons.Outlined.AutoAwesomeMosaic,
            route = VyroRoute.TemplatesLibrary,
            testTag = "nav_tab_templates"
        ),
        TopLevelNavItem(
            labelRes = R.string.nav_effects,
            selectedIcon = Icons.Rounded.AutoFixHigh,
            unselectedIcon = Icons.Outlined.AutoFixHigh,
            route = VyroRoute.EffectsLibrary(-1L),
            testTag = "nav_tab_effects"
        ),
        TopLevelNavItem(
            labelRes = R.string.nav_settings,
            selectedIcon = Icons.Rounded.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = VyroRoute.Settings,
            testTag = "nav_tab_settings"
        )
    )

    val showTopLevelChrome = currentDestination != null && (
        currentDestination.hasRoute<VyroRoute.Dashboard>() ||
            currentDestination.hasRoute<VyroRoute.TemplatesLibrary>() ||
            currentDestination.hasRoute<VyroRoute.Settings>() ||
            (currentDestination.hasRoute<VyroRoute.EffectsLibrary>() &&
                (backStackEntry?.toRoute<VyroRoute.EffectsLibrary>()?.projectId ?: -1L) <= 0L)
        )

    val useNavigationRail = showTopLevelChrome && widthSizeClass != WindowWidthSizeClass.Compact

    val navigateTopLevel: (VyroRoute) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = VyroDeepBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showTopLevelChrome && !useNavigationRail) {
                NavigationBar(
                    containerColor = VyroGraphite,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    topLevelItems.forEach { item ->
                        val selected = when (item.route) {
                            VyroRoute.Dashboard -> currentDestination?.hasRoute<VyroRoute.Dashboard>() == true
                            VyroRoute.TemplatesLibrary -> currentDestination?.hasRoute<VyroRoute.TemplatesLibrary>() == true
                            is VyroRoute.EffectsLibrary -> currentDestination?.hasRoute<VyroRoute.EffectsLibrary>() == true
                            VyroRoute.Settings -> currentDestination?.hasRoute<VyroRoute.Settings>() == true
                            else -> false
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTopLevel(item.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes), maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VyroNeonBlue,
                                selectedTextColor = VyroNeonBlue,
                                indicatorColor = VyroPurpleContainer,
                                unselectedIconColor = VyroTextSecondary,
                                unselectedTextColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(VyroDeepBlack)
                .padding(innerPadding)
        ) {
            if (useNavigationRail) {
                NavigationRail(
                    containerColor = VyroGraphite,
                    header = {
                        VyroPrismLogo(
                            size = 38.dp,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(top = 12.dp, bottom = 12.dp)
                        )
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    topLevelItems.forEach { item ->
                        val selected = when (item.route) {
                            VyroRoute.Dashboard -> currentDestination?.hasRoute<VyroRoute.Dashboard>() == true
                            VyroRoute.TemplatesLibrary -> currentDestination?.hasRoute<VyroRoute.TemplatesLibrary>() == true
                            is VyroRoute.EffectsLibrary -> currentDestination?.hasRoute<VyroRoute.EffectsLibrary>() == true
                            VyroRoute.Settings -> currentDestination?.hasRoute<VyroRoute.Settings>() == true
                            else -> false
                        }
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(item.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = VyroNeonBlue,
                                selectedTextColor = VyroNeonBlue,
                                indicatorColor = VyroPurpleContainer,
                                unselectedIconColor = VyroTextSecondary,
                                unselectedTextColor = VyroTextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(1.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = VyroRoute.Splash
                ) {
                    composable<VyroRoute.Splash> {
                        SplashScreen(
                            onFinished = {
                                navController.navigate(VyroRoute.Dashboard) {
                                    popUpTo<VyroRoute.Splash> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<VyroRoute.Dashboard> {
                        val vm: ProjectViewModel = viewModel(
                            factory = ProjectViewModel.Factory(
                                container.projectRepository,
                                container.userPreferencesRepository
                            )
                        )
                        DashboardScreen(
                            viewModel = vm,
                            widthSizeClass = widthSizeClass,
                            onCreateNewProject = { navController.navigate(VyroRoute.NewProject) },
                            onImportMedia = { navController.navigate(VyroRoute.MediaImport(-1L)) },
                            onOpenTemplates = { navigateTopLevel(VyroRoute.TemplatesLibrary) },
                            onOpenProject = { id -> navController.navigate(VyroRoute.Workspace(id)) },
                            onExportProject = { id -> navController.navigate(VyroRoute.Export(id)) },
                            onOpenProfile = { navigateTopLevel(VyroRoute.Settings) }
                        )
                    }

                    composable<VyroRoute.NewProject> {
                        val vm: NewProjectViewModel = viewModel(
                            factory = NewProjectViewModel.Factory(
                                container.projectRepository,
                                container.userPreferencesRepository
                            )
                        )
                        NewProjectScreen(
                            viewModel = vm,
                            onNavigateBack = { navController.popBackStack() },
                            onCreatedAndImport = { projectId ->
                                navController.navigate(VyroRoute.MediaImport(projectId)) {
                                    popUpTo<VyroRoute.NewProject> { inclusive = true }
                                }
                            },
                            onCreatedEmpty = { projectId ->
                                navController.navigate(VyroRoute.Workspace(projectId)) {
                                    popUpTo<VyroRoute.NewProject> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<VyroRoute.MediaImport> { entry ->
                        val args = entry.toRoute<VyroRoute.MediaImport>()
                        val vm: MediaImportViewModel = viewModel(
                            factory = MediaImportViewModel.Factory(
                                projectId = args.projectId,
                                projectRepository = container.projectRepository,
                                preferencesRepository = container.userPreferencesRepository,
                                metadataExtractor = container.mediaMetadataExtractor
                            )
                        )
                        MediaImportScreen(
                            viewModel = vm,
                            onNavigateBack = { navController.popBackStack() },
                            onCommittedToWorkspace = { resolvedProjectId ->
                                navController.navigate(VyroRoute.Workspace(resolvedProjectId)) {
                                    popUpTo<VyroRoute.MediaImport> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<VyroRoute.Workspace> { entry ->
                        val args = entry.toRoute<VyroRoute.Workspace>()
                        val vm: WorkspaceViewModel = viewModel(
                            factory = WorkspaceViewModel.Factory(
                                projectId = args.projectId,
                                projectRepository = container.projectRepository,
                                context = appContext
                            )
                        )
                        WorkspaceScreen(
                            viewModel = vm,
                            widthSizeClass = widthSizeClass,
                            onNavigateBack = { navController.popBackStack() },
                            onImportMedia = { projectId ->
                                navController.navigate(VyroRoute.MediaImport(projectId))
                            },
                            onOpenEffects = { projectId ->
                                navController.navigate(VyroRoute.EffectsLibrary(projectId))
                            },
                            onOpenExport = { projectId ->
                                navController.navigate(VyroRoute.Export(projectId))
                            }
                        )
                    }

                    composable<VyroRoute.EffectsLibrary> { entry ->
                        val args = entry.toRoute<VyroRoute.EffectsLibrary>()
                        val vm: EffectsLibraryViewModel = viewModel(
                            factory = EffectsLibraryViewModel.Factory(
                                projectId = args.projectId,
                                projectRepository = container.projectRepository
                            )
                        )
                        EffectsLibraryScreen(
                            viewModel = vm,
                            widthSizeClass = widthSizeClass,
                            showBackButton = args.projectId > 0L,
                            onNavigateBack = { navController.popBackStack() },
                            onCreateProject = { navController.navigate(VyroRoute.NewProject) }
                        )
                    }

                    composable<VyroRoute.TemplatesLibrary> {
                        val vm: TemplatesLibraryViewModel = viewModel(
                            factory = TemplatesLibraryViewModel.Factory(container.projectRepository)
                        )
                        TemplatesLibraryScreen(
                            viewModel = vm,
                            widthSizeClass = widthSizeClass,
                            onTemplateSelected = { projectId ->
                                navController.navigate(VyroRoute.MediaImport(projectId))
                            }
                        )
                    }

                    composable<VyroRoute.Export> { entry ->
                        val args = entry.toRoute<VyroRoute.Export>()
                        val vm: ExportViewModel = viewModel(
                            factory = ExportViewModel.Factory(
                                projectId = args.projectId,
                                projectRepository = container.projectRepository,
                                preferencesRepository = container.userPreferencesRepository,
                                context = appContext
                            )
                        )
                        ExportScreen(
                            viewModel = vm,
                            onNavigateBack = { navController.popBackStack() },
                            onImportClipsFirst = { projectId ->
                                navController.navigate(VyroRoute.MediaImport(projectId))
                            }
                        )
                    }

                    composable<VyroRoute.Settings> {
                        val vm: SettingsViewModel = viewModel(
                            factory = SettingsViewModel.Factory(
                                preferencesRepository = container.userPreferencesRepository,
                                projectRepository = container.projectRepository,
                                context = appContext
                            )
                        )
                        SettingsScreen(viewModel = vm)
                    }
                }
            }
        }
    }
}
