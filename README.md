# 🏠 Mon Foyer - App de gestion du foyer

Application Android pour gérer votre foyer en famille : courses, tâches, budget — tout synchronisé en temps réel via Firebase.

## Fonctionnalités

- **🛒 Courses** — Listes de courses partagées, articles par catégorie, cochage en temps réel
- **✅ Tâches** — Corvées et tâches, priorités, statuts, assignation par membre
- **💰 Budget** — Suivi des dépenses/revenus mensuel avec catégories
- **👨‍👩‍👧 Multi-membres** — Invitez votre mère avec un code à 6 chiffres
- **⚡ Temps réel** — Modifications visibles instantanément sur tous les appareils

## Stack technique

- **Kotlin + Jetpack Compose** — UI moderne et réactive
- **Firebase Firestore** — Base de données temps réel
- **Firebase Auth** — Authentification email/mot de passe
- **Hilt** — Injection de dépendances
- **Material Design 3** — Design system Google

---

## 🚀 Installation & Setup

### Prérequis

- Android Studio Hedgehog ou plus récent
- JDK 11+
- Compte Firebase (gratuit)

---

### Étape 1 — Configurer Firebase

1. Aller sur [console.firebase.google.com](https://console.firebase.google.com)
2. **Créer un projet** (ex: `mon-foyer`)
3. Ajouter une **application Android** avec le package `com.foyer.gestion`
4. Télécharger `google-services.json` et le placer dans `app/`

#### Activer les services Firebase :

**Authentication :**
- Console Firebase → Authentication → Sign-in method
- Activer **Email/Password**

**Firestore :**
- Console Firebase → Firestore Database → Créer une base de données
- Choisir **mode production**
- Copier les règles de sécurité ci-dessous

#### Règles Firestore (à copier dans la console) :

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Utilisateurs : lecture/écriture sur son propre document
    match /utilisateurs/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }

    // Foyers : accès uniquement aux membres
    match /foyers/{foyerId} {
      allow read: if request.auth != null &&
        request.auth.uid in resource.data.membres;
      allow create: if request.auth != null;
      allow update: if request.auth != null &&
        request.auth.uid in resource.data.membres;

      // Sous-collections (courses, tâches, transactions)
      match /{collection}/{docId} {
        allow read, write: if request.auth != null &&
          request.auth.uid in get(/databases/$(database)/documents/foyers/$(foyerId)).data.membres;
      }
    }
  }
}
```

---

### Étape 2 — Cloner et ouvrir le projet

```bash
git clone https://github.com/Kheyox/gestion-foy-.git
cd gestion-foy-
```

Ouvrir dans **Android Studio** → `Open` → sélectionner le dossier.

---

### Étape 3 — Builder l'APK

Dans Android Studio :
- **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
- L'APK sera dans `app/build/outputs/apk/debug/app-debug.apk`

Ou en ligne de commande :
```bash
./gradlew assembleDebug
```

---

### Étape 4 — Installer sur les téléphones

**Option A — USB :**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Option B — Partage de fichier :**
- Envoyer l'APK par WhatsApp, email, ou Google Drive
- Sur le téléphone : Paramètres → Sécurité → Autoriser les sources inconnues
- Ouvrir le fichier APK reçu et installer

---

## 📱 Premier lancement

1. **Toi** : Créer un compte → Créer un foyer (ex: "Famille") → Note le code d'invitation affiché
2. **Ta mère** : Créer un compte → "Rejoindre" → Entrer le code à 6 caractères

Les deux comptes verront les mêmes données en temps réel !

---

## Structure du projet

```
app/src/main/java/com/foyer/gestion/
├── data/
│   ├── model/          # Modèles de données (Foyer, Tache, Transaction...)
│   └── repository/     # Accès Firebase (Auth, Courses, Taches, Budget)
├── di/                 # Injection de dépendances Hilt
├── ui/
│   ├── navigation/     # Navigation entre écrans
│   ├── screens/        # Écrans (auth, home, courses, taches, budget)
│   └── theme/          # Thème Material Design 3
└── viewmodel/          # ViewModels (état UI + logique)
```

---

## Builds

- **Debug** : `./gradlew assembleDebug` → APK non signé pour tests
- **Release** : `./gradlew assembleRelease` → Nécessite un keystore de signature

Pour générer un APK release signable (pour partager de façon permanente) :
```bash
# Générer un keystore
keytool -genkey -v -keystore monfoyer.jks -keyalg RSA -keysize 2048 -validity 10000 -alias monfoyer
```
Puis configurer dans `app/build.gradle.kts` le bloc `signingConfigs`.
