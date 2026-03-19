package com.foyer.gestion.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.foyer.gestion.data.model.Categories
import com.foyer.gestion.data.model.Transaction
import com.foyer.gestion.data.model.TypeTransaction
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
private val dateFormat = SimpleDateFormat("dd MMM", Locale.FRENCH)

private val MOIS_NOMS = listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val foyerId = authState.foyerId ?: ""

    var showAddDialog by remember { mutableStateOf(false) }
    var showDepensesOnly by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(foyerId) {
        if (foyerId.isNotEmpty()) viewModel.observerTransactions(foyerId)
    }

    val transactionsFiltrees = uiState.transactions.filter { t ->
        val cal = Calendar.getInstance().apply { time = t.date.toDate() }
        val matchMois = cal.get(Calendar.MONTH) + 1 == uiState.moisSelectionne &&
                cal.get(Calendar.YEAR) == uiState.anneeSelectionnee
        val matchType = when (showDepensesOnly) {
            true -> t.type == TypeTransaction.DEPENSE.name
            false -> t.type == TypeTransaction.REVENU.name
            null -> true
        }
        matchMois && matchType
    }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Budget", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
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
                onClick = { showAddDialog = true },
                containerColor = MeliColors.CardPink,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sélecteur de mois
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MeliShapes.card)
                        .background(MeliColors.White.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val m = if (uiState.moisSelectionne == 1) 12 else uiState.moisSelectionne - 1
                            val a = if (uiState.moisSelectionne == 1) uiState.anneeSelectionnee - 1 else uiState.anneeSelectionnee
                            viewModel.changerMois(m, a)
                        }) {
                            Icon(Icons.Default.ChevronLeft, null, tint = MeliColors.White)
                        }
                        Text(
                            "${MOIS_NOMS[uiState.moisSelectionne - 1]} ${uiState.anneeSelectionnee}",
                            color = MeliColors.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = {
                            val m = if (uiState.moisSelectionne == 12) 1 else uiState.moisSelectionne + 1
                            val a = if (uiState.moisSelectionne == 12) uiState.anneeSelectionnee + 1 else uiState.anneeSelectionnee
                            viewModel.changerMois(m, a)
                        }) {
                            Icon(Icons.Default.ChevronRight, null, tint = MeliColors.White)
                        }
                    }
                }
            }

            // Carte résumé
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MeliShapes.card)
                        .background(MeliColors.White)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResumeItem("Revenus", uiState.resume.revenus, MeliColors.GreenOk, "💵")
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(MeliColors.TextMuted.copy(alpha = 0.3f)))
                        ResumeItem("Dépenses", uiState.resume.depenses, MeliColors.RedAlert, "💸")
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(MeliColors.TextMuted.copy(alpha = 0.3f)))
                        ResumeItem(
                            "Solde",
                            uiState.resume.solde,
                            if (uiState.resume.solde >= 0) MeliColors.GreenOk else MeliColors.RedAlert,
                            if (uiState.resume.solde >= 0) "✅" else "⚠️"
                        )
                    }
                }
            }

            // Filtres
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetFilterChip("Tout", showDepensesOnly == null) { showDepensesOnly = null }
                    BudgetFilterChip("Dépenses", showDepensesOnly == true) {
                        showDepensesOnly = if (showDepensesOnly == true) null else true
                    }
                    BudgetFilterChip("Revenus", showDepensesOnly == false) {
                        showDepensesOnly = if (showDepensesOnly == false) null else false
                    }
                }
            }

            if (transactionsFiltrees.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💸", fontSize = 64.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Aucune transaction", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                items(transactionsFiltrees, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onSupprimer = { viewModel.supprimerTransaction(foyerId, transaction.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AjouterTransactionDialog(
            onDismiss = { showAddDialog = false },
            onAjouter = { titre, montant, type, categorie, note ->
                viewModel.ajouterTransaction(foyerId, titre, montant, type, categorie, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ResumeItem(label: String, montant: Double, couleur: Color, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = MeliColors.TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(
            currencyFormat.format(montant),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = couleur
        )
    }
}

@Composable
private fun BudgetFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MeliColors.CardPink,
            selectedLabelColor = MeliColors.TextDark,
            containerColor = MeliColors.White.copy(alpha = 0.12f),
            labelColor = MeliColors.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MeliColors.White.copy(alpha = 0.2f),
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun TransactionCard(transaction: Transaction, onSupprimer: () -> Unit) {
    val isDepense = transaction.type == TypeTransaction.DEPENSE.name
    val couleur = if (isDepense) MeliColors.RedAlert else MeliColors.GreenOk
    val bgColor = if (isDepense) MeliColors.CardPink else MeliColors.CardMint
    val signe = if (isDepense) "-" else "+"
    val emoji = if (isDepense) "💸" else "💵"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.White)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.titre, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MeliColors.TextDark)
                Text(
                    "${transaction.categorie} • ${dateFormat.format(transaction.date.toDate())}",
                    fontSize = 12.sp,
                    color = MeliColors.TextMuted
                )
            }
            Text(
                "$signe${currencyFormat.format(transaction.montant)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = couleur
            )
            IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer",
                    tint = MeliColors.CardPink, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterTransactionDialog(
    onDismiss: () -> Unit,
    onAjouter: (String, Double, TypeTransaction, String, String) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var montantStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TypeTransaction.DEPENSE) }
    var categorie by remember { mutableStateOf("Autre") }
    var note by remember { mutableStateOf("") }
    var expandedCategorie by remember { mutableStateOf(false) }

    val categories = if (type == TypeTransaction.DEPENSE) Categories.depenses else Categories.revenus

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Nouvelle transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TypeTransaction.DEPENSE,
                        onClick = { type = TypeTransaction.DEPENSE; categorie = "Autre" },
                        label = { Text("💸 Dépense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TypeTransaction.REVENU,
                        onClick = { type = TypeTransaction.REVENU; categorie = "Autre" },
                        label = { Text("💵 Revenu") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Description *") },
                    singleLine = true,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = montantStr,
                    onValueChange = { montantStr = it.replace(",", ".") },
                    label = { Text("Montant (€) *") },
                    singleLine = true,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedCategorie, onExpandedChange = { expandedCategorie = it }) {
                    OutlinedTextField(
                        value = categorie,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategorie) },
                        shape = MeliShapes.input,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCategorie, onDismissRequest = { expandedCategorie = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categorie = cat; expandedCategorie = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    maxLines = 2,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val montant = montantStr.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { onAjouter(titre, montant, type, categorie, note) },
                enabled = titre.isNotBlank() && montant > 0,
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
