package com.foyer.gestion.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foyer.gestion.ui.screens.anniversaires.AnniversairesScreen
import com.foyer.gestion.ui.screens.auth.LoginScreen
import com.foyer.gestion.ui.screens.auth.RegisterScreen
import com.foyer.gestion.ui.screens.budget.BudgetScreen
import com.foyer.gestion.ui.screens.calendrier.CalendrierScreen
import com.foyer.gestion.ui.screens.courses.CoursesScreen
import com.foyer.gestion.ui.screens.courses.ListeCoursesScreen
import com.foyer.gestion.ui.screens.foyer.FoyerSetupScreen
import com.foyer.gestion.ui.screens.frigo.FrigoScreen
import com.foyer.gestion.ui.screens.home.HomeScreen
import com.foyer.gestion.ui.screens.notes.NotesScreen
import com.foyer.gestion.ui.screens.recettes.RecettesScreen
import com.foyer.gestion.ui.screens.taches.TachesScreen
import com.foyer.gestion.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object FoyerSetup : Screen("foyer_setup")
    object Home : Screen("home")
    object Courses : Screen("courses")
    object ListeCourses : Screen("liste_courses/{listeId}/{listeNom}") {
        fun createRoute(listeId: String, listeNom: String) = "liste_courses/$listeId/$listeNom"
    }
    object Taches : Screen("taches")
    object Budget : Screen("budget")
    object Calendrier : Screen("calendrier")
    object Notes : Screen("notes")
    object Frigo : Screen("frigo")
    object Recettes : Screen("recettes")
    object Anniversaires : Screen("anniversaires")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when {
            authState.user == null -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            authState.foyerId.isNullOrEmpty() -> {
                navController.navigate(Screen.FoyerSetup.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
                if (navController.currentDestination?.route == Screen.Login.route ||
                    navController.currentDestination?.route == Screen.FoyerSetup.route
                ) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {}
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {}
            )
        }
        composable(Screen.FoyerSetup.route) {
            FoyerSetupScreen(onSetupComplete = {})
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCourses = { navController.navigate(Screen.Courses.route) },
                onNavigateToTaches = { navController.navigate(Screen.Taches.route) },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                onNavigateToCalendrier = { navController.navigate(Screen.Calendrier.route) },
                onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                onNavigateToFrigo = { navController.navigate(Screen.Frigo.route) },
                onNavigateToRecettes = { navController.navigate(Screen.Recettes.route) },
                onNavigateToAnniversaires = { navController.navigate(Screen.Anniversaires.route) }
            )
        }
        composable(Screen.Courses.route) {
            CoursesScreen(
                onNavigateToListe = { id, nom ->
                    navController.navigate(Screen.ListeCourses.createRoute(id, nom))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ListeCourses.route,
            arguments = listOf(
                navArgument("listeId") { type = NavType.StringType },
                navArgument("listeNom") { type = NavType.StringType }
            )
        ) { backStack ->
            val listeId = backStack.arguments?.getString("listeId") ?: ""
            val listeNom = backStack.arguments?.getString("listeNom") ?: ""
            ListeCoursesScreen(
                listeId = listeId,
                listeNom = listeNom,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Taches.route) {
            TachesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Budget.route) {
            BudgetScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Calendrier.route) {
            CalendrierScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Notes.route) {
            NotesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Frigo.route) {
            FrigoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Recettes.route) {
            RecettesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Anniversaires.route) {
            AnniversairesScreen(onBack = { navController.popBackStack() })
        }
    }
}
