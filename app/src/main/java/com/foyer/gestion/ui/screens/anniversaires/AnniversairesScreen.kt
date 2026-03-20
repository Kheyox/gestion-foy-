package com.foyer.gestion.ui.screens.anniversaires

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.Anniversaire
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AnniversairesViewModel
import com.foyer.gestion.viewmodel.AuthViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private val CardLavender = Color(0xFFCBB8F0)
private val emojiChoix = listOf("🎂", "🎁", "🎉", "🥳", "🌟", "❤️", "🦋", "🌈")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversairesScreen(
    onBack: () -> Unit,
    viewModel: AnniversairesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val foyerId = authViewModel.authState.collectAsState().value.foyerId ?: ""
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(foyerId) { if (foyerId.isNotEmpty()) viewModel.observer(foyerId) }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Anniversaires", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = MeliColors.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeliColors.BgDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = CardLavender,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        if (uiState.anniversaires.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎂", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucun anniversaire", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour en ajouter un", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.anniversaires, key = { it.id }) { anniv ->
                    AnniversaireCard(anniv, onSupprimer = { viewModel.supprimer(foyerId, anniv.id) })
                }
            }
        }
    }

    if (showDialog) {
        AjouterAnniversaireDialog(
            onDismiss = { showDialog = false },
            onAjouter = { prenom, nom, jour, mois, annee, emoji, note ->
                val cal = Calendar.getInstance().apply { set(annee, mois - 1, jour, 0, 0, 0) }
                viewModel.ajouter(foyerId, prenom, nom, Timestamp(cal.time), emoji, note)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AnniversaireCard(anniv: Anniversaire, onSupprimer: () -> Unit) {
    val fmtYear = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(CardLavender)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(anniv.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${anniv.prenom} ${anniv.nom}".trim(),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeliColors.TextDark
                    )
                    Text(
                        fmtYear.format(anniv.dateNaissance.toDate()),
                        fontSize = 13.sp, color = MeliColors.TextMuted
                    )
                    if (anniv.note.isNotEmpty()) {
                        Text(anniv.note, fontSize = 12.sp, color = MeliColors.TextMuted)
                    }
                }
                IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer",
                        tint = MeliColors.BgDark.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterAnniversaireDialog(
    onDismiss: () -> Unit,
    onAjouter: (String, String, Int, Int, Int, String, String) -> Unit
) {
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var jour by remember { mutableStateOf("") }
    var mois by remember { mutableStateOf("") }
    var annee by remember { mutableStateOf("") }
    var emojiChoisi by remember { mutableStateOf("🎂") }
    var note by remember { mutableStateOf("") }

    val joursOk = jour.toIntOrNull()?.let { it in 1..31 } == true
    val moisOk = mois.toIntOrNull()?.let { it in 1..12 } == true
    val anneeOk = annee.toIntOrNull()?.let { it in 1900..2100 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Ajouter un anniversaire", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prenom, onValueChange = { prenom = it },
                        label = { Text("Prénom *") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nom, onValueChange = { nom = it },
                        label = { Text("Nom") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jour, onValueChange = { jour = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Jour") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mois, onValueChange = { mois = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Mois") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annee, onValueChange = { annee = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Année") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1.5f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Emoji", fontSize = 13.sp, color = MeliColors.TextMuted)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojiChoix.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(MeliShapes.pill)
                                .background(if (emojiChoisi == e) MeliColors.BgDark else Color.Transparent)
                                .clickable { emojiChoisi = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e, fontSize = 20.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (optionnel)") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(prenom, nom, jour.toInt(), mois.toInt(), annee.toInt(), emojiChoisi, note) },
                enabled = prenom.isNotBlank() && joursOk && moisOk && anneeOk,
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
