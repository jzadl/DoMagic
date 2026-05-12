package com.domagic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domagic.ui.Screen
import com.domagic.ui.screens.*
import com.domagic.ui.theme.DoMagicTheme
import com.domagic.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoMagicTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val vm: MainViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Intro.route
                    ) {
                        composable(Screen.Intro.route) {
                            IntroScreen(onContinue = {
                                navController.navigate(Screen.Connect.route) {
                                    popUpTo(Screen.Intro.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Connect.route) {
                            ConnectScreen(
                                vm = vm,
                                onConnected = { navController.navigate(Screen.DeviceInfo.route) },
                                onCommunity = { navController.navigate(Screen.Community.route) },
                                onTestMode = { navController.navigate(Screen.TestMode.route) }
                            )
                        }
                        composable(Screen.DeviceInfo.route) {
                            DeviceInfoScreen(
                                vm = vm,
                                onContinue = { navController.navigate(Screen.Patch.route) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Patch.route) {
                            PatchScreen(
                                vm = vm,
                                onPatchDone = { navController.navigate(Screen.Flash.route) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Flash.route) {
                            FlashScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Community.route) {
                            CommunityScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.TestMode.route) {
                            TestModeScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
