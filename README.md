<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Database-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white"/>
<img src="https://img.shields.io/badge/IDE-Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white"/>
<img src="https://img.shields.io/badge/Architecture-MVC-1F3A5F?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Commits-92-2E75B6?style=for-the-badge&logo=git&logoColor=white"/>

<br/><br/>

# 🌱 PLANNEXIA

### *L'application Android qui redonne le contrôle aux étudiants*

**Organise tes révisions. Réduis ton stress. Réussis tes examens.**

<br/>

> Projet Tutoré — L3 MIAGE | Université Paris-Saclay | 2024–2025  
> **Équipe N°5** — Rihabe MOUNAIM · Fatma KADRI · Luc LEVEQUE · Mohamed Yanis FOURAR · Sarah ABDELLI

<br/>

</div>

---

## 📋 Table des matières

- [À propos](#-à-propos)
- [Problématique](#-problématique)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture technique](#️-architecture-technique)
- [Modèle de données](#-modèle-de-données)
- [Installation](#-installation)
- [Structure du projet](#-structure-du-projet)
- [Organisation de l'équipe](#-organisation-de-léquipe)
- [Développement durable](#-développement-durable)
- [Roadmap](#-roadmap)
- [Équipe](#-équipe)

---

## 🎯 À propos

**Plannexia** est une application Android native développée en Java, conçue pour centraliser et simplifier la gestion des révisions étudiantes.

Contrairement aux outils généralistes comme Notion ou Todoist, Plannexia est **100% orientée étudiant** : elle structure le travail académique autour d'une hiérarchie claire — **Modules → Objectifs → Tâches** — avec suivi de progression intégré et planning hebdomadaire visuel.

```
✅ Fonctionne entièrement hors-ligne (SQLite local, zéro serveur)
✅ Interface épurée, pensée pour une prise en main immédiate
✅ Suivi de progression en temps réel
✅ Planning jour / semaine dynamique
```

---

## ❗ Problématique

Les étudiants font face à un paradoxe : **ils veulent s'organiser, mais les outils existants sont soit trop complexes, soit pas adaptés à leurs besoins académiques.**

| Problème identifié | Impact |
|---|---|
| 📄 Gaspillage de papier (agendas, fiches non structurées) | Inefficacité + impact environnemental |
| 🗂️ Manque de vision globale sur les révisions | Mauvaise priorisation, oublis |
| 😰 Stress lié à une mauvaise planification | Dégradation des performances |
| ⚖️ Inégalités entre étudiants organisés et désorganisés | Écarts de résultats injustes |
| 📱 Dispersion sur plusieurs outils sans cohérence | Perte de temps et d'énergie cognitive |

**Plannexia répond à ces 5 problèmes en une seule application.**

---

## ✨ Fonctionnalités

### 🔐 Authentification
- Création de compte et connexion sécurisée
- Hachage des mots de passe
- Sessions persistantes

### 📊 Dashboard
- Vue synthétique de la progression globale
- Récapitulatif des modules, objectifs et tâches en cours
- Indicateurs visuels d'avancement

### 📚 Gestion des Modules
- Création, modification, suppression de modules de révision
- Organisation par matière avec couleur personnalisable
- Vue d'ensemble par module

### 🎯 Gestion des Objectifs
- Définition d'objectifs mesurables par module
- Date limite et suivi d'avancement automatique
- Calcul dynamique de la progression

### ✅ Gestion des Tâches
- Décomposition des objectifs en tâches actionnables
- Statut : à faire / en cours / terminé
- Dates d'échéance individuelles

### 📅 Planning
- Vue calendaire jour et semaine
- Affichage dynamique construit depuis les tâches
- Navigation intuitive

### 👤 Profil Utilisateur
- Personnalisation des paramètres
- Gestion des données personnelles

### 🔔 Notifications *(optionnel)*
- Rappels d'échéances proches

### ⭐ Fonctionnalités Premium *(roadmap)*
- Planning automatique via IA
- Export PDF des révisions
- Statistiques avancées et analytics

---

## 🏗️ Architecture technique

### Stack technologique

| Technologie | Outil / Version | Justification |
|---|---|---|
| **Langage** | Java | Natif Android, robuste, bien documenté |
| **IDE** | Android Studio | Environnement officiel Google, émulateur intégré |
| **Base de données** | SQLite | Embarquée, offline-first, légère, zéro dépendance réseau |
| **Build system** | Gradle (Kotlin DSL) | Standard Android, gestion des dépendances |
| **Versionning** | Git / GitHub | Travail collaboratif, 92 commits |
| **Architecture** | MVC | Séparation claire Modèle / Vue / Contrôleur |

### Pattern MVC

```
app/
├── model/          # Entités : User, Module, Objective, Task
│   └── db/         # SQLiteOpenHelper, DAOs
├── view/           # Layouts XML, Activities, Fragments
│   ├── auth/       # Login, Register
│   ├── dashboard/  # Dashboard principal
│   ├── modules/    # Gestion des modules
│   ├── objectives/ # Gestion des objectifs
│   ├── tasks/      # Gestion des tâches
│   ├── planning/   # Vue calendaire
│   └── profile/    # Profil utilisateur
└── controller/     # Logique métier, liaison Modèle ↔ Vue
```

> **Avantage clé** : la séparation MVC a permis à chaque membre de l'équipe de travailler sur un module indépendamment, réduisant les conflits Git et accélérant l'intégration.

---

## 🗄️ Modèle de données

La base SQLite locale structure les données autour de **4 entités** liées en hiérarchie :

```
User
 └── Module (userId)
      └── Objective (moduleId)
           └── Task (objectiveId)
```

### Schéma détaillé

```sql
-- Utilisateur
CREATE TABLE User (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nom         TEXT NOT NULL,
    email       TEXT UNIQUE NOT NULL,
    password    TEXT NOT NULL  -- hashé
);

-- Module de révision
CREATE TABLE Module (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    userId      INTEGER NOT NULL,
    nom         TEXT NOT NULL,
    description TEXT,
    couleur     TEXT,
    FOREIGN KEY (userId) REFERENCES User(id)
);

-- Objectif
CREATE TABLE Objective (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    moduleId    INTEGER NOT NULL,
    titre       TEXT NOT NULL,
    description TEXT,
    dateLimit   TEXT,
    progression INTEGER DEFAULT 0,
    FOREIGN KEY (moduleId) REFERENCES Module(id)
);

-- Tâche
CREATE TABLE Task (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    objectiveId  INTEGER NOT NULL,
    titre        TEXT NOT NULL,
    statut       TEXT DEFAULT 'todo',  -- todo / done
    dateEcheance TEXT,
    FOREIGN KEY (objectiveId) REFERENCES Objective(id)
);
```

---

## 🚀 Installation

### Prérequis

- Android Studio **Hedgehog** ou version supérieure
- SDK Android **API 24** (Android 7.0) minimum
- JDK 11+

### Cloner le projet

```bash
git clone https://github.com/Yanis-Fourar/Projet_tutore.git
cd Projet_tutore
```

### Lancer l'application

1. Ouvrir le projet dans **Android Studio**
2. Attendre la synchronisation Gradle
3. Connecter un appareil Android ou lancer l'émulateur
4. Cliquer sur ▶️ **Run**

> ⚠️ Aucune configuration de serveur requise — l'application fonctionne entièrement en local grâce à SQLite.

---

## 📁 Structure du projet

```
Projet_tutore/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/           # Code source Java (MVC)
│   │       ├── res/
│   │       │   ├── layout/     # Fichiers XML des interfaces
│   │       │   ├── drawable/   # Ressources graphiques
│   │       │   └── values/     # Couleurs, strings, styles
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── README.md
```

---

## 👥 Organisation de l'équipe

Le projet a été développé selon une **méthodologie agile inspirée de Scrum**, avec des sprints bi-hebdomadaires et des revues de code systématiques.

### Déroulement des sprints

| Sprint | Phase | Contenu |
|---|---|---|
| **Sprint 1** | Cadrage | Cahier des charges, maquettes UI, modèle de données, mise en place GitHub |
| **Sprint 2** | Fondations | Authentification, base SQLite, structure des packages |
| **Sprint 3** | Fonctionnalités core | Modules, objectifs, tâches, dashboard, suivi de progression |
| **Sprint 4** | Finalisation | Planning, profil, tests, bugs, documentation |

### Workflow Git

```
main
 ├── feature/auth
 ├── feature/dashboard
 ├── feature/modules
 ├── feature/objectives
 ├── feature/tasks
 ├── feature/planning
 └── feature/profile
```

- ✅ **92 commits** au total
- ✅ Chaque Pull Request validée par un pair avant merge
- ✅ Tests manuels sur émulateur et appareils physiques
- ✅ Points de synchronisation hebdomadaires

### Défis techniques résolus

| Défi | Solution apportée |
|---|---|
| Cohérence des clés étrangères SQLite | `SQLiteOpenHelper` avec versioning de schéma |
| Synchronisation temps réel du Dashboard | Requêtes asynchrones via `AsyncTask / Executors` |
| Calcul dynamique de la progression | Requêtes SQL de comptage avec jointures |
| Conflits Git sur les layouts XML | Conventions de nommage strictes + branches dédiées |

---

## 🌱 Développement durable

Plannexia s'inscrit dans une vision du **développement durable social** — le pilier souvent oublié mais essentiel.

> *"Une application qui aide les étudiants à mieux s'organiser contribue à réduire les inégalités académiques, améliorer la santé mentale et former des individus plus capables de prendre de bonnes décisions pour la société et pour la planète."*

| Pilier | Contribution |
|---|---|
| 🤝 **Social** | Réduction du stress académique, équité organisationnelle entre étudiants |
| 💰 **Économique** | Modèle freemium inclusif, rentable dès l'an 1 |
| 🌍 **Environnemental** | Zéro papier, SQLite local (pas de serveur cloud énergivore), app légère |

**Alignement ODD 4** — Éducation de qualité (Objectifs de Développement Durable, ONU)

---

## 🗺️ Roadmap

### ✅ Version actuelle (MVP)
- [x] Authentification complète
- [x] CRUD Modules / Objectifs / Tâches
- [x] Dashboard avec suivi de progression
- [x] Planning hebdomadaire
- [x] Profil utilisateur

### 🔜 Version 2.0
- [ ] Planning automatique via IA (OpenAI API)
- [ ] Export PDF des révisions
- [ ] Statistiques avancées et analytics
- [ ] Notifications intelligentes
- [ ] Mode sombre

### 🔮 Version 3.0
- [ ] Synchronisation cloud (multi-appareils)
- [ ] Collaboration entre étudiants
- [ ] Version iOS
- [ ] Internationalisation (EN, AR, ES)

---

## 👨‍💻 Équipe

| Membre | Rôle |
|---|---|
| **Mohamed Yanis FOURAR** |<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Database-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white"/>
<img src="https://img.shields.io/badge/IDE-Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white"/>
<img src="https://img.shields.io/badge/Architecture-MVC-1F3A5F?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Commits-92-2E75B6?style=for-the-badge&logo=git&logoColor=white"/>

<br/><br/>

# 🌱 PLANNEXIA

### *L'application Android qui redonne le contrôle aux étudiants*

**Organise tes révisions. Réduis ton stress. Réussis tes examens.**

<br/>

> Projet Tutoré — L3 MIAGE | Université Paris-Saclay | 2024–2025  
> **Équipe N°5** — Rihabe MOUNAIM · Fatma KADRI · Luc LEVEQUE · Mohamed Yanis FOURAR · Sarah ABDELLI

<br/>

</div>

---

## 📋 Table des matières

- [À propos](#-à-propos)
- [Problématique](#-problématique)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture technique](#️-architecture-technique)
- [Modèle de données](#-modèle-de-données)
- [Installation](#-installation)
- [Structure du projet](#-structure-du-projet)
- [Organisation de l'équipe](#-organisation-de-léquipe)
- [Développement durable](#-développement-durable)
- [Roadmap](#-roadmap)
- [Équipe](#-équipe)

---

## 🎯 À propos

**Plannexia** est une application Android native développée en Java, conçue pour centraliser et simplifier la gestion des révisions étudiantes.

Contrairement aux outils généralistes comme Notion ou Todoist, Plannexia est **100% orientée étudiant** : elle structure le travail académique autour d'une hiérarchie claire — **Modules → Objectifs → Tâches** — avec suivi de progression intégré et planning hebdomadaire visuel.

```
✅ Fonctionne entièrement hors-ligne (SQLite local, zéro serveur)
✅ Interface épurée, pensée pour une prise en main immédiate
✅ Suivi de progression en temps réel
✅ Planning jour / semaine dynamique
```

---

## ❗ Problématique

Les étudiants font face à un paradoxe : **ils veulent s'organiser, mais les outils existants sont soit trop complexes, soit pas adaptés à leurs besoins académiques.**

| Problème identifié | Impact |
|---|---|
| 📄 Gaspillage de papier (agendas, fiches non structurées) | Inefficacité + impact environnemental |
| 🗂️ Manque de vision globale sur les révisions | Mauvaise priorisation, oublis |
| 😰 Stress lié à une mauvaise planification | Dégradation des performances |
| ⚖️ Inégalités entre étudiants organisés et désorganisés | Écarts de résultats injustes |
| 📱 Dispersion sur plusieurs outils sans cohérence | Perte de temps et d'énergie cognitive |

**Plannexia répond à ces 5 problèmes en une seule application.**

---

## ✨ Fonctionnalités

### 🔐 Authentification
- Création de compte et connexion sécurisée
- Hachage des mots de passe
- Sessions persistantes

### 📊 Dashboard
- Vue synthétique de la progression globale
- Récapitulatif des modules, objectifs et tâches en cours
- Indicateurs visuels d'avancement

### 📚 Gestion des Modules
- Création, modification, suppression de modules de révision
- Organisation par matière avec couleur personnalisable
- Vue d'ensemble par module

### 🎯 Gestion des Objectifs
- Définition d'objectifs mesurables par module
- Date limite et suivi d'avancement automatique
- Calcul dynamique de la progression

### ✅ Gestion des Tâches
- Décomposition des objectifs en tâches actionnables
- Statut : à faire / en cours / terminé
- Dates d'échéance individuelles

### 📅 Planning
- Vue calendaire jour et semaine
- Affichage dynamique construit depuis les tâches
- Navigation intuitive

### 👤 Profil Utilisateur
- Personnalisation des paramètres
- Gestion des données personnelles

### 🔔 Notifications *(optionnel)*
- Rappels d'échéances proches

### ⭐ Fonctionnalités Premium *(roadmap)*
- Planning automatique via IA
- Export PDF des révisions
- Statistiques avancées et analytics

---

## 🏗️ Architecture technique

### Stack technologique

| Technologie | Outil / Version | Justification |
|---|---|---|
| **Langage** | Java | Natif Android, robuste, bien documenté |
| **IDE** | Android Studio | Environnement officiel Google, émulateur intégré |
| **Base de données** | SQLite | Embarquée, offline-first, légère, zéro dépendance réseau |
| **Build system** | Gradle (Kotlin DSL) | Standard Android, gestion des dépendances |
| **Versionning** | Git / GitHub | Travail collaboratif, 92 commits |
| **Architecture** | MVC | Séparation claire Modèle / Vue / Contrôleur |

### Pattern MVC

```
app/
├── model/          # Entités : User, Module, Objective, Task
│   └── db/         # SQLiteOpenHelper, DAOs
├── view/           # Layouts XML, Activities, Fragments
│   ├── auth/       # Login, Register
│   ├── dashboard/  # Dashboard principal
│   ├── modules/    # Gestion des modules
│   ├── objectives/ # Gestion des objectifs
│   ├── tasks/      # Gestion des tâches
│   ├── planning/   # Vue calendaire
│   └── profile/    # Profil utilisateur
└── controller/     # Logique métier, liaison Modèle ↔ Vue
```

> **Avantage clé** : la séparation MVC a permis à chaque membre de l'équipe de travailler sur un module indépendamment, réduisant les conflits Git et accélérant l'intégration.

---

## 🗄️ Modèle de données

La base SQLite locale structure les données autour de **4 entités** liées en hiérarchie :

```
User
 └── Module (userId)
      └── Objective (moduleId)
           └── Task (objectiveId)
```

### Schéma détaillé

```sql
-- Utilisateur
CREATE TABLE User (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nom         TEXT NOT NULL,
    email       TEXT UNIQUE NOT NULL,
    password    TEXT NOT NULL  -- hashé
);

-- Module de révision
CREATE TABLE Module (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    userId      INTEGER NOT NULL,
    nom         TEXT NOT NULL,
    description TEXT,
    couleur     TEXT,
    FOREIGN KEY (userId) REFERENCES User(id)
);

-- Objectif
CREATE TABLE Objective (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    moduleId    INTEGER NOT NULL,
    titre       TEXT NOT NULL,
    description TEXT,
    dateLimit   TEXT,
    progression INTEGER DEFAULT 0,
    FOREIGN KEY (moduleId) REFERENCES Module(id)
);

-- Tâche
CREATE TABLE Task (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    objectiveId  INTEGER NOT NULL,
    titre        TEXT NOT NULL,
    statut       TEXT DEFAULT 'todo',  -- todo / done
    dateEcheance TEXT,
    FOREIGN KEY (objectiveId) REFERENCES Objective(id)
);
```

---

## 🚀 Installation

### Prérequis

- Android Studio **Hedgehog** ou version supérieure
- SDK Android **API 24** (Android 7.0) minimum
- JDK 11+

### Cloner le projet

```bash
git clone https://github.com/Yanis-Fourar/Projet_tutore.git
cd Projet_tutore
```

### Lancer l'application

1. Ouvrir le projet dans **Android Studio**
2. Attendre la synchronisation Gradle
3. Connecter un appareil Android ou lancer l'émulateur
4. Cliquer sur ▶️ **Run**

> ⚠️ Aucune configuration de serveur requise — l'application fonctionne entièrement en local grâce à SQLite.

---

## 📁 Structure du projet

```
Projet_tutore/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/           # Code source Java (MVC)
│   │       ├── res/
│   │       │   ├── layout/     # Fichiers XML des interfaces
│   │       │   ├── drawable/   # Ressources graphiques
│   │       │   └── values/     # Couleurs, strings, styles
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── README.md
```

---

## 👥 Organisation de l'équipe

Le projet a été développé selon une **méthodologie agile inspirée de Scrum**, avec des sprints bi-hebdomadaires et des revues de code systématiques.

### Déroulement des sprints

| Sprint | Phase | Contenu |
|---|---|---|
| **Sprint 1** | Cadrage | Cahier des charges, maquettes UI, modèle de données, mise en place GitHub |
| **Sprint 2** | Fondations | Authentification, base SQLite, structure des packages |
| **Sprint 3** | Fonctionnalités core | Modules, objectifs, tâches, dashboard, suivi de progression |
| **Sprint 4** | Finalisation | Planning, profil, tests, bugs, documentation |

### Workflow Git

```
main
 ├── feature/auth
 ├── feature/dashboard
 ├── feature/modules
 ├── feature/objectives
 ├── feature/tasks
 ├── feature/planning
 └── feature/profile
```

- ✅ **92 commits** au total
- ✅ Chaque Pull Request validée par un pair avant merge
- ✅ Tests manuels sur émulateur et appareils physiques
- ✅ Points de synchronisation hebdomadaires

### Défis techniques résolus

| Défi | Solution apportée |
|---|---|
| Cohérence des clés étrangères SQLite | `SQLiteOpenHelper` avec versioning de schéma |
| Synchronisation temps réel du Dashboard | Requêtes asynchrones via `AsyncTask / Executors` |
| Calcul dynamique de la progression | Requêtes SQL de comptage avec jointures |
| Conflits Git sur les layouts XML | Conventions de nommage strictes + branches dédiées |

---

## 🌱 Développement durable

Plannexia s'inscrit dans une vision du **développement durable social** — le pilier souvent oublié mais essentiel.

> *"Une application qui aide les étudiants à mieux s'organiser contribue à réduire les inégalités académiques, améliorer la santé mentale et former des individus plus capables de prendre de bonnes décisions pour la société et pour la planète."*

| Pilier | Contribution |
|---|---|
| 🤝 **Social** | Réduction du stress académique, équité organisationnelle entre étudiants |
| 💰 **Économique** | Modèle freemium inclusif, rentable dès l'an 1 |
| 🌍 **Environnemental** | Zéro papier, SQLite local (pas de serveur cloud énergivore), app légère |

**Alignement ODD 4** — Éducation de qualité (Objectifs de Développement Durable, ONU)

---

## 🗺️ Roadmap

### ✅ Version actuelle (MVP)
- [x] Authentification complète
- [x] CRUD Modules / Objectifs / Tâches
- [x] Dashboard avec suivi de progression
- [x] Planning hebdomadaire
- [x] Profil utilisateur

### 🔜 Version 2.0
- [ ] Planning automatique via IA (OpenAI API)
- [ ] Export PDF des révisions
- [ ] Statistiques avancées et analytics
- [ ] Notifications intelligentes
- [ ] Mode sombre

### 🔮 Version 3.0
- [ ] Synchronisation cloud (multi-appareils)
- [ ] Collaboration entre étudiants
- [ ] Version iOS
- [ ] Internationalisation (EN, AR, ES)

---

## 👨‍💻 Équipe

| Membre | Rôle |
|---|---|
| **Rihabe MOUNAIM** | Développeur|
| **Mohamed Yanis FOURAR** | Développeur |
| **Luc LEVEQUE** | Développeur |
| **Fatma KADRI** | Développeur |
| **Sarah ABDELLI** | Développeur |

---

## 📄 Licence

Projet académique réalisé dans le cadre du projet tutoré de L3 MIAGE — Université Paris-Saclay.  
© 2025 Équipe Plannexia — Tous droits réservés.

---

<div align="center">

**🌱 Plannexia — Plante ta réussite, récolte tes résultats.**

*Projet Tutoré L3 MIAGE | Université Paris-Saclay | 2024–2025*

</div>|
| **Luc LEVEQUE** | Développeur |
| **Rihabe MOUNAIM** | Développeur front-end / UX |
| **Fatma KADRI** | Développeur full-stack |
| **Sarah ABDELLI** | Développeur / Documentation |

---

## 📄 Licence

Projet académique réalisé dans le cadre du projet tutoré de L3 MIAGE — Université Paris-Saclay.  
© 2025 Équipe Plannexia — Tous droits réservés.

---

<div align="center">

**🌱 Plannexia — Plante ta réussite, récolte tes résultats.**

*Projet Tutoré L3 MIAGE | Université Paris-Saclay | 2024–2025*

</div>
