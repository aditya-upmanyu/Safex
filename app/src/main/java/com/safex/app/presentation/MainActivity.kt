package com.safex.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.presentation.navigation.SAFExNavGraph
import com.safex.app.presentation.navigation.Screen
import com.safex.app.presentation.theme.SAFExTheme
import com.safex.app.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val onboardingDone = settingsDataStore.onboardingDone.first()
            val startDestination = if (onboardingDone) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }

            setContent {
                val themeMode by settingsDataStore.themeMode.collectAsState(ThemeMode.SYSTEM)
                val useDarkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                SAFExTheme(darkTheme = useDarkTheme) {
                    val navController = rememberNavController()
                    SAFExNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
