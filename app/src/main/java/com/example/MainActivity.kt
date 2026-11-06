package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.data.local.preferences.UserPreferences
import com.example.presentation.navigation.AppBottomNavigationBar
import com.example.presentation.navigation.AppNavGraph
import com.example.ui.theme.NoorMuslimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.notifications.NotificationChannels.createChannels(this)

        val appContainer = (application as NoorMuslimApp).container

        setContent {
            val userPrefs by appContainer.userPreferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            androidx.compose.runtime.LaunchedEffect(userPrefs) {
                if (userPrefs.notificationsEnabled) {
                    com.example.notifications.PrayerAlarmScheduler.scheduleAllPrayerAlarms(
                        this@MainActivity,
                        userPrefs
                    )
                }
                com.example.widgets.WidgetUpdateCoordinator.updateAllWidgets(this@MainActivity)
            }

            // Enforce Arabic RTL support globally
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NoorMuslimTheme(
                    themeMode = userPrefs.themeMode,
                    fontScale = userPrefs.fontSizeScale.scale,
                    fontFamilyType = userPrefs.fontFamilyType
                ) {
                    val navController = rememberNavController()

                    val initialRoute = intent?.getStringExtra("initial_route")
                    androidx.compose.runtime.LaunchedEffect(initialRoute) {
                        if (!initialRoute.isNullOrBlank() && initialRoute != "home") {
                            navController.navigate(initialRoute) {
                                launchSingleTop = true
                            }
                        }
                    }

                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            AppBottomNavigationBar(navController = navController)
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        AppNavGraph(
                            navController = navController,
                            appContainer = appContainer,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
