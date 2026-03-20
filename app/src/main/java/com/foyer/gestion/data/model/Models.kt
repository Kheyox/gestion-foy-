package com.foyer.gestion.data.model

import com.google.firebase.Timestamp

// ─── Foyer (Household) ────────────────────────────────────────────────────────

data class Foyer(
    val id: String = "",
    val nom: String = "",
    val membres: List<String> = emptyList(), // UIDs Firebase
    val codeInvitation: String = "",
    val creePar: String = "",
    val creeLe: Timestamp = Timestamp.now()
)

// ─── Utilisateur ──────────────────────────────────────────────────────────────

data class Utilisateur(
    val uid: String = "",
    val prenom: String = "",
    val email: String = "",
    val foyerId: String = "",
    val avatarEmoji: String = "🏠",
    val creeLe: Timestamp = Timestamp.now()
)

// ─── Article de course ────────────────────────────────────────────────────────

data class ArticleCourse(
    val id: String = "",
    val nom: String = "",
    val quantite: String = "1",
    val unite: String = "",
    val categorie: String = "Autre",
    val estCoche: Boolean = false,
    val ajoutePar: String = "",
    val ajouteLe: Timestamp = Timestamp.now(),
    val cochePar: String = "",
    val cocheLe: Timestamp? = null
)

data class ListeCourses(
    val id: String = "",
    val foyerId: String = "",
    val nom: String = "Courses",
    val articles: List<ArticleCourse> = emptyList(),
    val creeLe: Timestamp = Timestamp.now(),
    val modifieLe: Timestamp = Timestamp.now()
)

// ─── Tâche ────────────────────────────────────────────────────────────────────

enum class PrioriteTache { BASSE, NORMALE, HAUTE }
enum class StatutTache { A_FAIRE, EN_COURS, TERMINEE }

data class Tache(
    val id: String = "",
    val foyerId: String = "",
    val titre: String = "",
    val description: String = "",
    val statut: String = StatutTache.A_FAIRE.name,
    val priorite: String = PrioriteTache.NORMALE.name,
    val assigneA: String = "",
    val creePar: String = "",
    val creeLe: Timestamp = Timestamp.now(),
    val echeance: Timestamp? = null,
    val termineLe: Timestamp? = null
)

// ─── Budget ───────────────────────────────────────────────────────────────────

enum class TypeTransaction { DEPENSE, REVENU }

data class Transaction(
    val id: String = "",
    val foyerId: String = "",
    val titre: String = "",
    val montant: Double = 0.0,
    val type: String = TypeTransaction.DEPENSE.name,
    val categorie: String = "Autre",
    val ajoutePar: String = "",
    val date: Timestamp = Timestamp.now(),
    val note: String = ""
)

data class Budget(
    val id: String = "",
    val foyerId: String = "",
    val mois: Int = 0,
    val annee: Int = 0,
    val plafond: Double = 0.0,
    val transactions: List<Transaction> = emptyList()
)

// ─── Événement calendrier ─────────────────────────────────────────────────────

data class Evenement(
    val id: String = "",
    val foyerId: String = "",
    val titre: String = "",
    val description: String = "",
    val dateDebut: Timestamp = Timestamp.now(),
    val dateFin: Timestamp? = null,
    val couleur: String = "BLUE",
    val creePar: String = "",
    val creeLe: Timestamp = Timestamp.now()
)

// ─── Note ─────────────────────────────────────────────────────────────────────

data class Note(
    val id: String = "",
    val foyerId: String = "",
    val titre: String = "",
    val contenu: String = "",
    val creePar: String = "",
    val creeLe: Timestamp = Timestamp.now(),
    val modifieLe: Timestamp = Timestamp.now()
)

// ─── Article frigo ────────────────────────────────────────────────────────────

data class ArticleFrigo(
    val id: String = "",
    val foyerId: String = "",
    val nom: String = "",
    val quantite: String = "",
    val categorie: String = "Réfrigérateur",
    val dateExpiration: Timestamp? = null,
    val ajoutePar: String = "",
    val ajouteLe: Timestamp = Timestamp.now(),
    val imageUrl: String? = null
)

// ─── Recette ──────────────────────────────────────────────────────────────────

data class Recette(
    val id: String = "",
    val foyerId: String = "",
    val titre: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val dureeMinutes: Int = 0,
    val portions: Int = 4,
    val ajoutePar: String = "",
    val ajouteLe: Timestamp = Timestamp.now()
)

// ─── Anniversaire ─────────────────────────────────────────────────────────────

data class Anniversaire(
    val id: String = "",
    val foyerId: String = "",
    val prenom: String = "",
    val nom: String = "",
    val dateNaissance: Timestamp = Timestamp.now(),
    val emoji: String = "🎂",
    val note: String = "",
    val ajoutePar: String = "",
    val ajouteLe: Timestamp = Timestamp.now()
)

// ─── Catégories prédéfinies ───────────────────────────────────────────────────

object Categories {
    val courses = listOf("Fruits & Légumes", "Viandes & Poissons", "Produits laitiers",
        "Boulangerie", "Boissons", "Épicerie", "Hygiène", "Entretien", "Surgelés", "Autre")

    val depenses = listOf("Courses", "Loyer", "Électricité", "Internet", "Transport",
        "Santé", "Restauration", "Loisirs", "Vêtements", "Autre")

    val revenus = listOf("Salaire", "Aide", "Remboursement", "Autre")
}
