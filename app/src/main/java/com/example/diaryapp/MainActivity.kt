package com.example.diaryapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diaryapp.ui.theme.AppThemeMode
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.ui.theme.ThemePreferences
import com.example.diaryapp.view.AddScreen
import com.example.diaryapp.view.BackupPreviewScreen
import com.example.diaryapp.view.EditScreen
import com.example.diaryapp.view.HomeScreen
import com.example.diaryapp.view.ProfileScreen
import com.example.diaryapp.view.ViewScreen

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val notificationDestination =
        mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        ThemePreferences.load(this)

        notificationDestination.value =
            intent.getStringExtra("open_screen")

        setContent {

            val themeMode = ThemePreferences.themeMode.value

            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Permission result
                }

            LaunchedEffect(Unit) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    if (
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            }

            DiaryAppTheme(
                themeMode = themeMode
            ) {

                val darkTheme = when (themeMode) {
                    AppThemeMode.LIGHT -> false
                    AppThemeMode.DARK -> true
                    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                val backgroundColor =
                    MaterialTheme.colorScheme.background

                SideEffect {

                    val barColor = backgroundColor.toArgb()

                    window.statusBarColor = barColor
                    window.navigationBarColor = barColor

                    val barController =
                        WindowInsetsControllerCompat(
                            window,
                            window.decorView
                        )

                    barController.isAppearanceLightStatusBars =
                        !darkTheme

                    barController.isAppearanceLightNavigationBars =
                        !darkTheme
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = backgroundColor
                ) { innerPadding ->

                    // NavController must be created before using it.
                    val navController = rememberNavController()

                    // Notification tap -> Profile Screen
                    val destination =
                        notificationDestination.value

                    LaunchedEffect(destination) {

                        if (destination == "profile") {

                            navController.navigate("profile") {
                                launchSingleTop = true
                            }

                            notificationDestination.value = null
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("home") {
                            HomeScreen(navController, viewModel)
                        }

                        composable("add") {
                            AddScreen(navController, viewModel)
                        }

                        composable("profile") {
                            ProfileScreen(navController, viewModel)
                        }

                        composable("edit/{index}") {

                            val index = it.arguments
                                ?.getString("index")
                                ?.toLong() ?: 0L

                            EditScreen(
                                navController,
                                viewModel,
                                index
                            )
                        }

                        composable("view/{index}") {

                            val index = it.arguments
                                ?.getString("index")
                                ?.toLong() ?: 0L

                            ViewScreen(
                                navController,
                                viewModel,
                                index
                            )
                        }

                        composable(
                            route = "backup_preview/{uri}/{fileName}",
                            arguments = listOf(
                                navArgument("uri") {
                                    type = NavType.StringType
                                },
                                navArgument("fileName") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->

                            val uri = backStackEntry.arguments?.getString("uri") ?: ""
                            val fileName =
                                backStackEntry.arguments?.getString("fileName") ?: "Backup"

                            BackupPreviewScreen(
                                navController = navController,
                                viewModel = viewModel,
                                uriString = Uri.decode(uri),
                                fileName = Uri.decode(fileName)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        notificationDestination.value =
            intent.getStringExtra("open_screen")
    }
}