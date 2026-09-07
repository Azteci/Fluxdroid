package com.azteci.fluxdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.azteci.fluxdroid.ui.AppRoute
import com.azteci.fluxdroid.ui.CanvasScreen
import com.azteci.fluxdroid.ui.FluxDroidTheme
import com.azteci.fluxdroid.ui.FluxViewModel
import com.azteci.fluxdroid.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluxDroidTheme {
                val vm: FluxViewModel = viewModel()
                val route = vm.ui.collectAsState().value.route
                when (route) {
                    AppRoute.Home -> HomeScreen(vm)
                    is AppRoute.Canvas -> CanvasScreen(vm)
                    is AppRoute.NewProject -> HomeScreen(vm)
                }
            }
        }
    }
}
