package com.hmncube.juiceme

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.Snackbar
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.hmncube.juiceme.components.AppAlertDialog
import com.hmncube.juiceme.components.OpenAlertDialogValues
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.extensions.getValueOrFalse
import com.hmncube.juiceme.history.HistoryScreen
import com.hmncube.juiceme.history.HistoryViewModel
import com.hmncube.juiceme.home.HomeScreen
import com.hmncube.juiceme.settings.SettingScreen
import com.hmncube.juiceme.theme.JuiceMeTheme

class MainActivity : ComponentActivity() {
/*

    lateinit var openAlertDialogValues: MutableState<OpenAlertDialogValues?>
    var openAlertDialog = mutableStateOf(false)
*/

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel by viewModels()
        val historyViewModel: HistoryViewModel = ViewModelFactory(AppDatabase.getDatabase(this))
            .create(HistoryViewModel::class.java)
        val screens = listOf(
            Screen.History,
            Screen.Home,
            Screen.Settings
        )
        setContent {
            JuiceMeTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val openAlertDialog = remember { mutableStateOf(false) }
                val openAlertDialogValues = remember { mutableStateOf<OpenAlertDialogValues?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val screenTitle = viewModel.screenTitle.observeAsState()
                    val showDeleteAll =
                        viewModel.showDeleteAll.observeAsState().getValueOrFalse()
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                when (data.visuals) {
                                    is AppSnackbarVisuals -> {
                                        ShowSnackBar(data)
                                    }

                                    is AppSnackbarVisualsFeedback -> {}
                                }

                            }
                        },
                        topBar = {
                            TopAppBar(
                                title = {
                                    //todo move .value to extenstion
                                    Text(
                                        text = screenTitle.value ?: "Home",
                                        textAlign = TextAlign.Center
                                    )
                                },

                                actions = {
                                    if (showDeleteAll) {
                                        IconButton(onClick = {
                                            openAlertDialogValues.value = prepareToClearAll(historyViewModel = historyViewModel)
                                            openAlertDialog.value = true
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_delete),
                                                contentDescription = stringResource(
                                                    id = R.string.menu_delete
                                                )
                                            )
                                        }
                                    }
                                }

                            )
                        },
                        bottomBar = {
                            BottomNavigation {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                screens.forEach { screen ->
                                    val selected =
                                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    val color = if (selected) {
                                        Color.Yellow
                                    } else {
                                        Color.White
                                    }
                                    BottomNavigationItem(
                                        icon = {
                                            Icon(
                                                painterResource(id = screen.iconId),
                                                contentDescription = screen.route,
                                                tint = color
                                            )
                                        },
                                        label = {
                                            Text(
                                                stringResource(screen.resourceId),
                                                color = color
                                            )
                                        },
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

                        when {
                            openAlertDialog.value -> {
                                val values = openAlertDialogValues.value
                                if (values != null) {
                                    AppAlertDialog(
                                        onDismissRequest = {
                                            openAlertDialog.value = false
                                            openAlertDialogValues.value = null
                                        },
                                        onConfirmation = {
                                            openAlertDialog.value = false
                                            values.onConfirmation()
                                            openAlertDialogValues.value = null
                                        },
                                        dialogTitle = values.dialogTitle,
                                        dialogText = values.dialogText,
                                        icon = values.icon,
                                        isDanger = values.isDanger
                                    )
                                }
                            }
                        }
                        AppNavigation(
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    private fun prepareToClearAll(historyViewModel: HistoryViewModel): OpenAlertDialogValues {
        return OpenAlertDialogValues(
            onConfirmation = { historyViewModel.clearAll() },
            dialogTitle = "Delete",
            dialogText = "Are you sure you want to delete all the numbers?",
            icon = Icons.Default.Delete,
            isDanger = true
        )
    }

    @Composable
    private fun ShowSnackBar(data: SnackbarData) {
        val isError = (data.visuals as AppSnackbarVisuals).isError
        val textColor = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onPrimary
        }
        Snackbar {
            Text(text = data.visuals.message, color = textColor)
        }
    }


    fun dialNumber(codePrefix: String, extractedNumber: String) {
        val dialIntent = Intent(Intent.ACTION_CALL)
        val str = Uri.encode("$codePrefix${extractedNumber}${Uri.decode("%23")}")
        dialIntent.data = Uri.parse("tel:$str")
        startActivity(dialIntent)
    }

    @Composable
    private fun AppNavigation(
        navController: NavHostController,
        snackbarHostState: SnackbarHostState,
        viewModel: MainViewModel
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                viewModel.setTitle("Home")
                HomeScreen()
            }
            composable(Screen.History.route) {
                viewModel.setTitle("History")
                HistoryScreen(applicationContext, snackbarHostState)
            }
            composable(Screen.Settings.route) {
                viewModel.setTitle("Settings")
                SettingScreen(applicationContext, snackbarHostState)
            }
        }
    }
}
