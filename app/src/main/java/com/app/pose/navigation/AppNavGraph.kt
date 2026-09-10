package com.app.pose.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.pose.ui.components.AppBottomNav
import com.app.pose.ui.feature.camera.CameraWorkoutScreen
import com.app.pose.ui.feature.detail.ExerciseDetailScreen
import com.app.pose.ui.feature.home.HomeScreen
import com.app.pose.ui.feature.library.LibraryScreen
import com.app.pose.ui.feature.onboarding.OnboardingScreen
import com.app.pose.ui.feature.profile.ProfileScreen
import com.app.pose.ui.feature.progress.ProgressScreen
import com.app.pose.ui.feature.summary.WorkoutSummaryScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Home.route,
        Screen.Library.route,
        Screen.Progress.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                AppBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onStartWorkout = {
                        navController.navigate(Screen.Camera.createRoute("squat"))
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Onboarding.route
            ) {
                // Onboarding
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onDone = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Home
                composable(Screen.Home.route) {
                    HomeScreen(
                        onOpenExercise = { exerciseId ->
                            navController.navigate(Screen.Detail.createRoute(exerciseId))
                        },
                        onStartExercise = { exerciseId ->
                            navController.navigate(Screen.Camera.createRoute(exerciseId))
                        },
                        onNavigateToLibrary = {
                            navController.navigate(Screen.Library.route)
                        },
                        onNavigateToProgress = {
                            navController.navigate(Screen.Progress.route)
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                // Exercise Library
                composable(Screen.Library.route) {
                    LibraryScreen(
                        onOpenExercise = { exerciseId ->
                            navController.navigate(Screen.Detail.createRoute(exerciseId))
                        },
                        onStartExercise = { exerciseId ->
                            navController.navigate(Screen.Camera.createRoute(exerciseId))
                        }
                    )
                }

                // Exercise Detail
                composable(
                    route = Screen.Detail.route,
                    arguments = listOf(
                        navArgument("exerciseId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "squat"
                    ExerciseDetailScreen(
                        exerciseId = exerciseId,
                        onBack = { navController.popBackStack() },
                        onStart = { id ->
                            navController.navigate(Screen.Camera.createRoute(id))
                        }
                    )
                }

                // Camera Workout
                composable(
                    route = Screen.Camera.route,
                    arguments = listOf(
                        navArgument("exerciseId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "squat"
                    CameraWorkoutScreen(
                        exerciseId = exerciseId,
                        onExit = { navController.popBackStack() },
                        onFinish = { result ->
                            navController.navigate(
                                Screen.Summary.createRoute(
                                    exerciseId = result.exerciseId,
                                    reps = result.reps,
                                    correctReps = result.correctReps,
                                    formScore = result.formScore,
                                    durationSec = result.durationSec
                                )
                            ) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }

                // Workout Summary
                composable(
                    route = Screen.Summary.route,
                    arguments = listOf(
                        navArgument("exerciseId") { type = NavType.StringType },
                        navArgument("reps") { type = NavType.IntType },
                        navArgument("correctReps") { type = NavType.IntType },
                        navArgument("formScore") { type = NavType.IntType },
                        navArgument("durationSec") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "squat"
                    val reps = backStackEntry.arguments?.getInt("reps") ?: 12
                    val correctReps = backStackEntry.arguments?.getInt("correctReps") ?: 10
                    val formScore = backStackEntry.arguments?.getInt("formScore") ?: 83
                    val durationSec = backStackEntry.arguments?.getInt("durationSec") ?: 180

                    WorkoutSummaryScreen(
                        exerciseId = exerciseId,
                        reps = reps,
                        correctReps = correctReps,
                        formScore = formScore,
                        durationSec = durationSec,
                        onContinue = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onViewProgress = {
                            navController.navigate(Screen.Progress.route) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }

                // Progress
                composable(Screen.Progress.route) {
                    ProgressScreen()
                }

                // Profile
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}
