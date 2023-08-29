package com.hmncube.juiceme

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hmncube.juiceme.history.HistoryScreen
import com.hmncube.juiceme.home.HomeScreen
import com.hmncube.juiceme.theme.JuiceMeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screens = listOf(
            Screen.History,
            Screen.Home,
            Screen.Settings
        )
        setContent {
            JuiceMeTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = "Home", textAlign = TextAlign.Center)
                                },
                            )
                        },
                        bottomBar = {
                            BottomNavigation {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                screens.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.
                                    any { it.route == screen.route } == true
                                    val color = if (selected) {
                                        Color.Yellow
                                    } else {
                                        Color.White
                                    }
                                    BottomNavigationItem(
                                        icon = {
                                            Icon(
                                                painterResource(id = screen.iconId) ,
                                                contentDescription = screen.route,
                                                tint = color
                                            )
                                        },
                                        label = { Text(stringResource(screen.resourceId), color = color) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }) {
                        AppNavigation(navController)
                    }
                }
            }
        }
    }


    fun dialNumber(codePrefix: String, extractedNumber: String) {
        val dialIntent = Intent(Intent.ACTION_CALL)
        val str = Uri.encode("$codePrefix${extractedNumber}${Uri.decode("%23")}")
        dialIntent.data = Uri.parse("tel:$str")
        startActivity(dialIntent)
    }

    @Composable
    private fun AppNavigation(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen(applicationContext)
            }
        }
    }
}
