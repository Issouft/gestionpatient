# gestionpatient

Application de gestion de patients, réalisée en groupe dans le cadre d'un certificat. Elle réunit au même endroit le parcours d'un patient : la prise de rendez-vous, la consultation, l'ordonnance et les médicaments.

> **État actuel :** cette page décrit ce que le projet prévoit, d'après son modèle de données. La plupart de ces fonctionnalités restent à coder. La [section 5](#5-où-en-est-le-projet) indique où on en est.

## À quoi sert le projet

### Qui utilise l'application

| Utilisateur | Ce qu'il fait dans l'application |
|---|---|
| **Patient** | Prend ses rendez-vous, et demande si un médicament est disponible |
| **Secrétaire** | Crée les dossiers des patients et gère les rendez-vous |
| **Médecin** | Reçoit les patients, rédige les consultations et prescrit des médicaments |
| **Pharmacien** | Gère le stock de médicaments et répond aux demandes de disponibilité |
| **Administrateur** | Génère les rapports d'activité |

### Le parcours d'un patient

```text
Rendez-vous → Consultation → Ordonnance → Demande de médicament
```

1. **Le dossier :** une secrétaire crée le dossier du patient. Chaque patient a un seul dossier, qui contient ses antécédents et ses consultations.
2. **Les disponibilités :** chaque médecin a un planning, découpé en créneaux. Un créneau a une date, une heure de début et une heure de fin.
3. **Le rendez-vous :** le patient prend un rendez-vous avec un médecin. Un créneau ne peut être réservé qu'une seule fois. Le rendez-vous peut ensuite être confirmé, reporté, rejeté, annulé ou terminé.
4. **La consultation :** un rendez-vous peut donner lieu à une consultation. Le médecin y note ses observations et son diagnostic, et elle s'ajoute au dossier du patient.
5. **L'ordonnance :** une consultation peut donner lieu à une ordonnance. Pour chaque médicament prescrit, elle indique la quantité, la posologie et la durée du traitement.
6. **La demande de médicament :** le patient demande si un médicament est disponible. Le pharmacien valide ou refuse la demande, et donne sa réponse.

### Les services autour de ce parcours

- **Le stock de médicaments :** chaque médicament a un stock et un seuil d'alerte, qui signale quand il en reste trop peu.
- **Les notifications :** les utilisateurs sont prévenus lors d'un rappel, d'une confirmation ou d'une annulation de rendez-vous, d'une réponse à une demande, ou d'une alerte de stock.
- **Les rapports :** l'administrateur génère des rapports sur une période, à propos de l'activité médicale, des rendez-vous ou du stock de médicaments.

### Comment c'est construit

Ce projet est la partie serveur de l'application. C'est une **API** : un programme qui reçoit des demandes et répond avec des données au format JSON. Il ne contient pas d'écrans pour l'instant.

**Technologies :** Java 21, Spring Boot, PostgreSQL et Maven.

## Sommaire

1. [Installer le projet](#1-installer-le-projet)
2. [Lancer le projet](#2-lancer-le-projet)
3. [Travailler avec Git](#3-travailler-avec-git)
4. [Si quelque chose ne marche pas](#4-si-quelque-chose-ne-marche-pas)
5. [Où en est le projet](#5-où-en-est-le-projet)

Pour comprendre comment le code est organisé, lisez [ARCHITECTURE.md](ARCHITECTURE.md).

## 1. Installer le projet

### Ce qu'il faut avoir

- **Java 21**
- **PostgreSQL**, le logiciel qui stocke les données
- **Git**
- **Un éditeur**, par exemple VS Code

Maven, l'outil qui construit le projet, n'a pas besoin d'être installé : le projet contient déjà le programme `mvnw`, qui s'en occupe.

### Étape 1 : récupérer le projet

```bash
git clone https://github.com/Issouft/gestionpatient.git
cd gestionpatient
```

### Étape 2 : créer la base de données

Créez dans PostgreSQL une base vide nommée `gestionpatient`, avec l'utilisateur `postgres`. Sous Linux, utilisez cette commande :

```bash
sudo -u postgres createdb gestionpatient
```

Vous n'avez pas besoin de créer les tables : l'application s'en charge à son démarrage.

### Étape 3 : créer le fichier `.env`

Un **secret** est une information qui donne un accès, comme un mot de passe. Il ne doit jamais être publié sur GitHub.

On range donc les secrets dans un fichier nommé `.env`, à la racine du projet. Git ignore ce fichier, qui reste sur votre ordinateur. **Chacun a le sien.**

Créez le fichier `.env` avec ce contenu :

```properties
spring.datasource.password=le_mot_de_passe_de_votre_PostgreSQL
JWT_SECRET=une_longue_suite_de_caracteres_aleatoires
```

| Ligne | À quoi elle sert |
|---|---|
| `spring.datasource.password` | Le mot de passe que l'application utilise pour se connecter à **votre** PostgreSQL |
| `JWT_SECRET` | La clé qui permet au serveur de signer les jetons de connexion |

Pour obtenir une valeur pour `JWT_SECRET`, lancez cette commande et copiez le résultat :

```bash
openssl rand -hex 64
```

**L'application ne démarre pas si l'une de ces deux lignes manque.**

## 2. Lancer le projet

Sous Windows, remplacez `./mvnw` par `mvnw.cmd` dans les commandes.

### Démarrer l'application

```bash
./mvnw spring-boot:run
```

L'application répond ensuite à l'adresse `http://localhost:8081`. Appuyez sur `Ctrl+C` pour l'arrêter.

### Lancer les tests

Les tests vérifient automatiquement que le code fonctionne. PostgreSQL doit être démarré, et votre fichier `.env` doit être rempli.

```bash
./mvnw test
```

Si tout va bien, vous verrez `BUILD SUCCESS`. Une longue trace d'erreur peut s'afficher pendant les tests : c'est normal, car l'un d'eux simule volontairement une panne.

## 3. Travailler avec Git

### Récupérer la dernière version du projet

Remplacez `ma-branche` par le nom de votre branche, par exemple `Issouf`.

**1. Regardez si vous avez du travail non enregistré :**

```bash
git status
```

**2. S'il y a des fichiers modifiés, enregistrez-les :**

```bash
git add .
git commit -m "Description de mon travail"
```

`git add .` prépare tous les fichiers listés par `git status`. Vérifiez donc d'abord que la liste ne contient ni `.env` ni aucun mot de passe.

**3. Récupérez la dernière version de `main` :**

```bash
git switch main
git pull origin main
```

**4. Ajoutez-la à votre branche, puis publiez le résultat :**

```bash
git switch ma-branche
git merge main
git push origin ma-branche
```

**Si Git affiche `CONFLICT`, ne forcez rien.** La commande suivante annule la fusion et remet votre branche dans l'état précédent. Demandez ensuite de l'aide au groupe.

```bash
git merge --abort
```

### Revenir en arrière

Git garde l'historique de ce qui a été commité. Pour annuler un commit, utilisez ces commandes :

```bash
git log --oneline              # trouver le code du commit à annuler
git revert <code-du-commit>    # créer un nouveau commit qui l'annule
```

`git revert` ne supprime rien de l'historique et convient donc au travail à plusieurs. Évitez `git reset --hard` et `git push --force` : ces commandes peuvent faire perdre du travail, à vous ou aux autres.

**Git ne protège pas tout :**

- Ce qui n'a jamais été commité ne peut pas être récupéré. Commitez et publiez donc votre travail régulièrement.
- Un mot de passe publié reste lisible dans l'historique, même après sa suppression. Il faut donc le changer.

## 4. Si quelque chose ne marche pas

| Ce que vous voyez | Ce que ça veut dire | Quoi faire |
|---|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | La ligne `JWT_SECRET` manque | Ajoutez-la dans `.env` (étape 3) |
| `no password was provided` | Le mot de passe de la base n'est pas trouvé | Vérifiez `.env`. Sous Linux, lancez aussi `unset SPRING_DATASOURCE_PASSWORD` |
| `password authentication failed` | Le mot de passe de la base est faux | Corrigez `spring.datasource.password` dans `.env` |
| `database "gestionpatient" does not exist` | La base n'a pas été créée | Suivez l'étape 2 |
| `Connection refused` | PostgreSQL n'est pas démarré | Démarrez PostgreSQL |
| Des `import` en rouge dans VS Code | L'éditeur n'a pas rechargé le projet | Lancez `Java: Clean Java Language Server Workspace` |
| Réponse 401 sur toutes les demandes | Il faut être connecté pour accéder à cette route | Connectez-vous d'abord. Voir aussi la section 5 |

## 5. Où en est le projet

**Ce qui fonctionne :**

- L'organisation du code par domaine
- La gestion commune des erreurs
- La sécurité : connexion, jetons et routes fermées par défaut

**Ce qui manque :**

- [ ] **Créer un compte.** Aucun moyen n'existe encore, donc personne ne peut se connecter pour l'instant.
- [ ] **Corriger le contenu des DTO.** Beaucoup contiennent encore des entités.
- [ ] **Écrire les fonctionnalités :** les contrôleurs, les services et les repositories de chaque domaine.
- [ ] **Décider des droits de chaque rôle :** ce que chacun peut voir et faire.
- [ ] **Choisir les responsables de chaque partie.** Voir [ARCHITECTURE.md](ARCHITECTURE.md), section 4.
