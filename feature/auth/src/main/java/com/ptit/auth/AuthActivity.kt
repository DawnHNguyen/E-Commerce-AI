package com.ptit.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ptit.auth.login.LoginScreen
import com.ptit.auth.register.RegisterScreen
import com.ptit.navigation.Navigator
import com.ptit.navigation.destination.LoginScreen
import com.ptit.navigation.destination.RegisterScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val layoutDirection = LocalLayoutDirection.current

            // A surface container using the 'background' color from the theme
            Scaffold(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = LoginScreen,
                    modifier = Modifier
                        .padding(
                            start = it.calculateStartPadding(layoutDirection),
                            end = it.calculateEndPadding(layoutDirection),
                        ),
                    enterTransition = {
                        fadeIn(animationSpec = tween(0))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(0))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(0))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(0))
                    }
                ) {
                    composable<LoginScreen> {
                        LoginScreen(
                            onNavigateToSignUp = {
                                navController.navigate(RegisterScreen)
                            },
                            onLoginSuccess = {
                                Navigator.navigateToMainActivity(this@AuthActivity)
                                finish()
                            }
                        )
                    }

                    composable<RegisterScreen> {
                        RegisterScreen(
                            onNavigateToSignIn = navController::navigateUp,
                            onRegisterSuccess = {
                                Navigator.navigateToMainActivity(this@AuthActivity)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}