package com.foyer.gestion.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.Categories
import com.foyer.gestion.data.model.Transaction
import com.foyer.gestion.data.model.TypeTransaction
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
private val dateFormat = SimpleDateFormat("dd MMM", Locale.FRENCH)

private val MOIS_NOMS = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
    "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")

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

    // Filtrer par mois sélectionné
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
        topBar = {
            TopAppBar(
                title = { Text("💰 Budget") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Sélecteur de mois
            item {
                MoisSelector(
                    mois = uiState.moisSelectionne,
                    annee = uiState.anneeSelectionnee,
                    onPrecedent = {
                        val m = if (uiState.moisSelectionne == 1) 12 else uiState.moisSelectionne - 1
                        val a = if (uiState.moisSelectionne == 1) uiState.anneeSelectionnee - 1 else uiState.anneeSelectionnee
                        viewModel.changerMois(m, a)
                    },
                    onSuivant = {
                        val m = if (uiState.moisSelectionne == 12) 1 else uiState.moisSelectionne + 1
                        val a = if (uiState.moisSelectionne == 12) uiState.anneeSelectionnee + 1 else uiState.anneeSelectionnee
                        viewModel.changerMois(m, a)
                    }
                )
            }

            // Carte résumé
            item {
                ResumeCard(
                    depenses = uiState.resume.depenses,
                    revenus = uiState.resume.revenus,
                    solde = uiState.resume.solde
                )
            }

            // Filtres type
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = showDepensesOnly == null, onClick = { showDepensesOnly = null }, label = { Text("Tout") })
                    FilterChip(selected = showDepensesOnly == true, onClick = { showDepensesOnly = if (showDepensesOnly == true) null else true }, label = { Text("Dépenses") })
                    FilterChip(selected = showDepensesOnly == false, onClick = { showDepensesOnly = if (showDepensesOnly == false) null else false }, label = { Text("Revenus") })
                }
            }

            if (transactionsFiltrees.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💸", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Aucune transaction", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                items(transactionsFiltrees, key = { it.id }) { transaction ->
                    TransactionItem(
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
private fun MoisSelector(
    mois: Int,
    annee: Int,
    onPrecedent: () -> Unit,
    onSuivant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrecedent) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mois précédent")
        }
        Text(
            "${MOIS_NOMS[mois - 1]} $annee",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onSuivant) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mois suivant")
        }
    }
}

@Composable
private fun ResumeCard(depenses: Double, revenus: Double, solde: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (solde >= 0) Color(0xFF4CAF50).copy(alpha = 0.12f)
            else Color(0xFFE53935).copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResumeItem("Revenus", revenus, Color(0xFF4CAF50))
            VerticalDivider(modifier = Modifier.height(60.dp))
            ResumeItem("Dépenses", depenses, Color(0xFFE53935))
            VerticalDivider(modifier = Modifier.height(60.dp))
            ResumeItem("Solde", solde, if (solde >= 0) Color(0xFF4CAF50) else Color(0xFFE53935))
        }
    }
}

@Composable
private fun ResumeItem(label: String, montant: Double, couleur: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(4.dp))
        Text(
            currencyFormat.format(montant),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = couleur
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction, onSupprimer: () -> Unit) {
    val isDepense = transaction.type == TypeTransaction.DEPENSE.name
    val couleur = if (isDepense) Color(0xFFE53935) else Color(0xFF4CAF50)
    val signe = if (isDepense) "-" else "+"

    Surface {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône catégorie
            Card(
                colors = CardDefaults.cardColors(containerColor = couleur.copy(alpha = 0.12f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (isDepense) "💸" else "💵", fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.titre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "${transaction.categorie} • ${dateFormat.format(transaction.date.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                "$signe${currencyFormat.format(transaction.montant)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = couleur
            )
            IconButton(onClick = onSupprimer) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
        title = { Text("Nouvelle transaction") },
        text = {
            Column {
                // Toggle dépense/revenu
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == TypeTransaction.DEPENSE,
                        onClick = { type = TypeTransaction.DEPENSE; categorie = "Autre" },
                        label = { Text("💸 Dépense") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = type == TypeTransaction.REVENU,
                        onClick = { type = TypeTransaction.REVENU; categorie = "Autre" },
                        label = { Text("💵 Revenu") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Description *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = montantStr,
                    onValueChange = { montantStr = it.replace(",", ".") },
                    label = { Text("Montant (€) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCategorie,
                    onExpandedChange = { expandedCategorie = it }
                ) {
                    OutlinedTextField(
                        value = categorie,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategorie) },
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val montant = montantStr.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { onAjouter(titre, montant, type, categorie, note) },
                enabled = titre.isNotBlank() && montant > 0
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
