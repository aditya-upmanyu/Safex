package com.safex.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.presentation.navigation.SAFExNavGraph
import com.safex.app.presentation.navigation.Screen
import com.safex.app.presentation.theme.SAFExTheme
import com.safex.app.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val onboardingDone by settingsDataStore.onboardingDone.collectAsState(initial = false)

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SAFExTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val startDestination = if (onboardingDone) {
                    Screen.Home.route
                } else {
                    Screen.Onboarding.route
                }
                SAFExNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
