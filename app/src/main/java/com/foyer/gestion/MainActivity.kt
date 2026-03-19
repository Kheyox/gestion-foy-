package com.foyer.gestion

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.foyer.gestion.ui.navigation.AppNavigation
import com.foyer.gestion.ui.theme.GestionFoyerTheme
import com.google.firebase.appdistribution.FirebaseAppDistribution
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForUpdates()
        enableEdgeToEdge()
        setContent {
            GestionFoyerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun checkForUpdates() {
        FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()
            .addOnFailureListener { e ->
                Log.d("AppDistribution", "Pas de mise à jour ou erreur : ${e.message}")
            }
    }
}
