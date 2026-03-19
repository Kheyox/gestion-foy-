package com.foyer.gestion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.foyer.gestion.ui.navigation.AppNavigation
import com.foyer.gestion.ui.theme.GestionFoyerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var updateAvailable by mutableStateOf<Pair<String, String>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionFoyerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                    updateAvailable?.let { (version, url) ->
                        AlertDialog(
                            onDismissRequest = { updateAvailable = null },
                            title = { Text("Mise à jour disponible") },
                            text = { Text("La version $version est disponible. Voulez-vous mettre à jour ?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    updateAvailable = null
                                }) { Text("Mettre à jour") }
                            },
                            dismissButton = {
                                TextButton(onClick = { updateAvailable = null }) {
                                    Text("Plus tard")
                                }
                            }
                        )
                    }
                }
            }
        }
        checkForUpdates()
    }

    private fun checkForUpdates() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val conn = URL("https://api.github.com/repos/Kheyox/gestion-foy-/releases/latest")
                    .openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val latestVersion = json.getString("tag_name").removePrefix("v")

                    if (isNewerVersion(latestVersion, BuildConfig.VERSION_NAME)) {
                        val assets = json.getJSONArray("assets")
                        var downloadUrl = ""
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.getString("name").endsWith(".apk")) {
                                downloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                        if (downloadUrl.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                updateAvailable = Pair(latestVersion, downloadUrl)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("UpdateChecker", "Vérification MAJ échouée: ${e.message}")
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
