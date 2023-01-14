package com.hmncube.juiceme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.hmncube.juiceme.databinding.ActivityMainBinding
import com.hmncube.juiceme.use_cases.PreferencesUseCase
import com.hmncube.juiceme.use_cases.TelephonyUseCase

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController

        viewBinding.bottomNav.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.history, R.id.home, R.id.settings)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        detectNetworkProvider()
    }

    private fun detectNetworkProvider() {
        TelephonyUseCase(baseContext, PreferencesUseCase(baseContext)).getNetworkProvider()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}