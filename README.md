```markdown
<div align="center">

<br>

```
██████╗ ██╗      █████╗ ███╗   ██╗███████╗██╗  ██╗██╗ █████╗ 
██╔══██╗██║     ██╔══██╗████╗  ██║██╔════╝╚██╗██╔╝██║██╔══██╗
██████╔╝██║     ███████║██╔██╗ ██║█████╗   ╚███╔╝ ██║███████║
██╔═══╝ ██║     ██╔══██║██║╚██╗██║██╔══╝   ██╔██╗ ██║██╔══██║
██║     ███████╗██║  ██║██║ ╚████║███████╗██╔╝ ██╗██║██║  ██║
╚═╝     ╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝╚═╝╚═╝  ╚═╝
```

### 📚 Organise. Révise. Réussis.

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)]()
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)]()
[![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)]()
[![Material Design](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=material-design&logoColor=white)]()
[![Min SDK](https://img.shields.io/badge/Min%20SDK%2024-informational?style=for-the-badge)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK%2036-success?style=for-the-badge)]()

<br>

> **Planexia** est une application Android native qui aide les étudiants  
> à structurer leurs révisions en modules, objectifs et tâches,  
> avec un timer intégré, des notifications intelligentes et un suivi de progression.

<br>

</div>

---

## 📋 Sommaire

```
📌 Aperçu          🚀 Fonctionnalités     🏗️ Architecture
🛠️ Technologies    ⚙️ Installation        📁 Structure
🗄️ Base de données 👥 Équipe
```

---

## 📌 Aperçu

Planexia repose sur une hiérarchie simple et efficace :

```
┌─────────────────────────────────────────┐
│                PLANEXIA                 │
│                                         │
│   📦 MODULE  (ex: Mathématiques)        │
│      └── 🎯 OBJECTIF  (ex: Intégrales) │
│              └── ✅ TÂCHE  (ex: Exos)  │
│                                         │
│   100% hors ligne · SQLite local        │
└─────────────────────────────────────────┘
```

---

## 🚀 Fonctionnalités

### 📚 Gestion des études

| | Fonctionnalité | Description |
|---|---|---|
| 📦 | **Modules** | Matières avec coefficient et code couleur |
| 🎯 | **Objectifs** | Objectifs par module avec date limite |
| ✅ | **Tâches** | Tâches avec ressources, notes et suivi |
| 📊 | **Progression** | Avancement par module en temps réel |

### ⏱️ Planification & Focus

| | Fonctionnalité | Description |
|---|---|---|
| 📅 | **Planning** | Vue calendrier jour / semaine |
| ⏱️ | **Chrono** | Timer avec objectif de durée et historique |
| 📄 | **Export PDF** | Résumé du planning en PDF |

### 🔔 Notifications automatiques

| Heure | Notification |
|---|---|
| 🌅 **8h00** | Rappel des tâches du jour |
| ⚠️ **9h00** | Alerte deadlines proches |
| 📈 **Lundi 10h00** | Bilan de progression hebdo |

### 👤 Compte utilisateur

- 🔐 Inscription & connexion avec mot de passe hashé
- 🎨 Profil personnalisable — pseudo, filière, année
- 🌙 Dark Mode natif
- ⭐ Fonctionnalités Premium débloquables

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────┐
│         COUCHE PRÉSENTATION                  │
│   Activities · Adapters · RecyclerViews      │
└────────────────────┬─────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────┐
│           REPOSITORY PATTERN                 │
│         PlanexiaRepository.java              │
│         (642 lignes · CRUD complet)          │
└────────────────────┬─────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────┐
│         SQLiteOpenHelper                     │
│       PlanexiaDatabaseHelper.java            │
└────────────────────┬─────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────┐
│            SQLite  (local)                   │
└──────────────────────────────────────────────┘
```

**Composants clés :**

| Composant | Rôle |
|---|---|
| `PlanexiaRepository` | Point d'entrée unique pour les données |
| `SessionManager` | Session via SharedPreferences |
| `ChronoService` | ForegroundService — timer arrière-plan |
| `TaskReminderReceiver` | BroadcastReceiver + AlarmManager |
| `PdfExporter` | Génération de documents PDF |

---

## 🛠️ Technologies utilisées

| Technologie | Version | Usage |
|---|---|---|
| **Java** | 11+ | Langage principal |
| **Android SDK** | 36 (min 24) | Plateforme |
| **Gradle Kotlin DSL** | latest | Build system |
| **SQLite** | — | Base de données locale |
| **Material Design** | 3 | Composants UI |
| **AndroidX / AppCompat** | — | Compatibilité |
| **RecyclerView + CardView** | — | Listes & grilles |
| **AlarmManager** | — | Notifications programmées |
| **ForegroundService** | — | Timer arrière-plan |
| **ExecutorService** | — | Async DB operations |

---

## ⚙️ Installation

**Prérequis :**
- Android Studio Hedgehog ou plus récent
- JDK 11+
- Appareil ou émulateur Android API 24+

```bash
# Cloner le dépôt
git clone https://github.com/rihabemounaim/Planexia.git

# Ouvrir dans Android Studio
# File > Open > sélectionner le dossier

# Builder l'APK
./gradlew assembleDebug
```

> APK généré dans `app/build/outputs/apk/debug/`

**Commandes utiles :**

```bash
./gradlew build           # Compilation complète
./gradlew assembleDebug   # APK debug
./gradlew test            # Tests unitaires
./gradlew clean           # Nettoyage
```

**Permissions requises :**

| Permission | Raison |
|---|---|
| `POST_NOTIFICATIONS` | Rappels et alertes |
| `SCHEDULE_EXACT_ALARM` | Précision des alarmes |
| `FOREGROUND_SERVICE` | Timer arrière-plan |
| `VIBRATE` | Retour haptique |

---

## 📁 Structure du projet

```
app/src/main/java/com/example/planexia/
│
├── 🚪  OnboardingActivity.java       ← Point d'entrée
├── 🏠  MainActivity.java             ← Dashboard
├── 📅  PlanningActivity.java         ← Calendrier
├── ⏱️  ChronoActivity.java           ← Timer
├── ⚙️  ChronoService.java            ← Service fond
├── 👤  ProfileActivity.java
├── 🔧  ParametresActivity.java
│
├── 📂  data/
│   ├──  PlanexiaDatabaseHelper.java  ← SQLite setup
│   ├──  PlanexiaRepository.java      ← CRUD
│   └──  SessionManager.java          ← Session
│
├── 📂  model/
│   ├──  Module.java
│   ├──  Objective.java
│   └──  Task.java
│
├── 📂  ui/
│   ├── modules/
│   ├── objectives/
│   ├── tasks/
│   ├── progression/
│   ├── premium/
│   └── adapters/
│
├── 📂  notifications/
│   ├──  NotificationHelper.java
│   ├──  NotificationsActivity.java
│   └──  TaskReminderReceiver.java    ← Alarmes
│
└── 📂  util/
    ├──  PdfExporter.java
    └──  PasswordUtils.java
```

---

## 🗄️ Base de données

```
┌──────────────────────────────────────────────────────────┐
│  users                                                   │
│  id · email · password_hash · pseudo · filiere · annee  │
│  is_premium                                              │
└───────────────────────┬──────────────────────────────────┘
                        │ 1
                        │ ∞
┌───────────────────────▼──────────────────────────────────┐
│  modules                                                 │
│  id · user_id · name · coefficient · color               │
└───────────────────────┬──────────────────────────────────┘
                        │ 1
                        │ ∞
┌───────────────────────▼──────────────────────────────────┐
│  objectives                                              │
│  id · module_id · title · due_date                       │
└───────────────────────┬──────────────────────────────────┘
                        │ 1
                        │ ∞
┌───────────────────────▼──────────────────────────────────┐
│  tasks                                                   │
│  id · objective_id · title · is_done · due_date          │
│  resource_text                                           │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│  chrono_sessions                                         │
│  id · user_id · task_label · duration_ms · goal_min      │
│  created_at                                              │
└──────────────────────────────────────────────────────────┘
```

---

##  Équipe

> Projet tutoré — Équipe N°5

<div align="center">

| Nom | Rôle |
|---|---|
| **Rihabe MOUNAIM** | Développeur |
| **Mohamed Yanis FOURAR** | Développeur |
| **Luc LEVEQUE** | Développeur |
| **Sarah ABDELLI** | Développeur |
| **Fatma KADRI** | Développeur |

<br>



</div>
```