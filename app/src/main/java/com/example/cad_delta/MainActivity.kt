package com.example.cad_delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cad_delta.ui.navigation.NavGraph
import com.example.cad_delta.ui.theme.Cad_deltaTheme
import com.example.cad_delta.ui.viewmodel.CustomerFormViewModel
import com.example.cad_delta.ui.viewmodel.DeviceFormViewModel
import com.example.cad_delta.ui.viewmodel.MainViewModel
import com.example.cad_delta.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val factory by lazy {
        val app = application as CadApplication
        ViewModelFactory(app.repository)
    }

    private val mainViewModel: MainViewModel by viewModels { factory }
    private val customerFormViewModel: CustomerFormViewModel by viewModels { factory }
    private val deviceFormViewModel: DeviceFormViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cad_deltaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        mainViewModel = mainViewModel,
                        customerFormViewModel = customerFormViewModel,
                        deviceFormViewModel = deviceFormViewModel
                    )
                }
            }
        }
    }
}
