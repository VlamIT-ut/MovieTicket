package com.example.movieticket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.movieticket.ui.auth.LoginScreen
import com.example.movieticket.ui.auth.RegisterScreen

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
}

@Composable
fun AuthNavigation(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthScreen.Register.route)
                },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        composable(AuthScreen.Register.route) {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = onAuthSuccess
            )
        }
    }
} 