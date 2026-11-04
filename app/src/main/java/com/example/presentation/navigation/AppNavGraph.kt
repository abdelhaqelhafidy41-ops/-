package com.example.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.core.util.AppContainer
import com.example.presentation.adhkar.AdhkarScreen
import com.example.presentation.adhkar.AdhkarViewModel
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.more.MoreScreen
import com.example.presentation.prayer.PrayerScreen
import com.example.presentation.prayer.PrayerViewModel
import com.example.presentation.quran.QuranScreen
import com.example.presentation.quran.QuranViewModel
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.settings.SettingsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) }
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(appContainer)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToQuran = { navController.navigate(Screen.Quran.route) },
                onNavigateToAdhkar = { navController.navigate(Screen.Adhkar.route) },
                onNavigateToPrayer = { navController.navigate(Screen.Prayer.route) }
            )
        }

        composable(Screen.Quran.route) {
            val quranViewModel: QuranViewModel = viewModel(
                factory = QuranViewModel.Factory(appContainer)
            )
            QuranScreen(
                viewModel = quranViewModel,
                onNavigateToSurah = { surahNumber ->
                    navController.navigate(Screen.QuranReading.createRoute(surahNumber))
                }
            )
        }

        composable(
            route = Screen.QuranReading.route,
            arguments = listOf(
                androidx.navigation.navArgument("surahNumber") {
                    type = androidx.navigation.NavType.IntType
                }
            )
        ) { backStackEntry ->
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
            val context = androidx.compose.ui.platform.LocalContext.current
            val readingViewModel: com.example.presentation.quran.QuranReadingViewModel = viewModel(
                factory = com.example.presentation.quran.QuranReadingViewModel.Factory(
                    surahNumber = surahNumber,
                    appContainer = appContainer,
                    context = context.applicationContext
                )
            )
            com.example.presentation.quran.QuranReadingScreen(
                viewModel = readingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Adhkar.route) {
            val adhkarViewModel: AdhkarViewModel = viewModel(
                factory = AdhkarViewModel.Factory(appContainer)
            )
            AdhkarScreen(
                viewModel = adhkarViewModel,
                onNavigateToLibrary = { navController.navigate(Screen.IslamicLibrary.route) }
            )
        }

        composable(Screen.Prayer.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prayerViewModel: PrayerViewModel = viewModel(
                factory = PrayerViewModel.Factory(context.applicationContext, appContainer)
            )
            PrayerScreen(viewModel = prayerViewModel)
        }

        composable(Screen.More.route) {
            MoreScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToLibrary = { navController.navigate(Screen.IslamicLibrary.route) }
            )
        }

        composable(Screen.IslamicLibrary.route) {
            val libraryViewModel: com.example.presentation.library.IslamicLibraryViewModel = viewModel(
                factory = com.example.presentation.library.IslamicLibraryViewModel.Factory(appContainer)
            )
            com.example.presentation.library.IslamicLibraryScreen(
                viewModel = libraryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(appContainer)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
