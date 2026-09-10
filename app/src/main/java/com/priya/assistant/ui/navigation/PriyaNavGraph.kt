package com.priya.assistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.priya.assistant.PriyaApplication
import com.priya.assistant.ui.PriyaViewModel
import com.priya.assistant.ui.SettingsViewModel
import com.priya.assistant.ui.screens.AboutScreen
import com.priya.assistant.ui.screens.AccessibilityScreen
import com.priya.assistant.ui.screens.AISettingsScreen
import com.priya.assistant.ui.screens.ChatScreen
import com.priya.assistant.ui.screens.HomeScreen
import com.priya.assistant.ui.screens.PermissionsScreen
import com.priya.assistant.ui.screens.PrivacyScreen
import com.priya.assistant.ui.screens.SettingsScreen
import com.priya.assistant.ui.screens.VoiceSettingsScreen
import androidx.lifecycle.viewmodel.compose.viewModel

object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val AI_SETTINGS = "settings/ai"
    const val VOICE_SETTINGS = "settings/voice"
    const val ACCESSIBILITY = "settings/accessibility"
    const val PERMISSIONS = "settings/permissions"
    const val PRIVACY = "settings/privacy"
    const val ABOUT = "settings/about"
}

@Composable
fun PriyaNavGraph(app: PriyaApplication) {
    val navController: NavHostController = rememberNavController()
    val priyaViewModel: PriyaViewModel = viewModel(factory = PriyaViewModel.factory(app))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = priyaViewModel,
                onOpenChat = { navController.navigate(Routes.CHAT) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CHAT) {
            ChatScreen(
                viewModel = priyaViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) }
            )
        }
        composable(Routes.AI_SETTINGS) {
            AISettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.VOICE_SETTINGS) {
            VoiceSettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.ACCESSIBILITY) {
            AccessibilityScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
