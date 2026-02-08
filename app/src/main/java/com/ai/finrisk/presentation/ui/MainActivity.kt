package com.ai.finrisk.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ai.finrisk.ui.theme.FinRiskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinRiskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RiskAssessmentScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
