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
| **Administrateur** | Crée les comptes du personnel et génère les rapports d'activité |

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
spring.mail.username=votre.adresse@gmail.com
spring.mail.password=le_mot_de_passe_d_application_gmail
administrateur.email=email_du_premier_administrateur
administrateur.mot-de-passe=son_mot_de_passe
```

| Ligne | À quoi elle sert |
|---|---|
| `spring.datasource.password` | Le mot de passe que l'application utilise pour se connecter à **votre** PostgreSQL |
| `JWT_SECRET` | La clé qui permet au serveur de signer les jetons de connexion |
| `spring.mail.username` et `spring.mail.password` | Le compte Gmail qui envoie les emails d'invitation au personnel |
| `administrateur.email` et `administrateur.mot-de-passe` | Le premier administrateur, créé au démarrage s'il n'en existe aucun |

Pour obtenir une valeur pour `JWT_SECRET`, lancez cette commande et copiez le résultat :

```bash
openssl rand -hex 64
```

Pour `spring.mail.password`, n'utilisez pas votre mot de passe Gmail habituel, mais un **mot de passe d'application** :

1. Sur [myaccount.google.com](https://myaccount.google.com), activez la validation en deux étapes.
2. Dans la barre de recherche du compte, tapez « Mots de passe des applications ».
3. Créez-en un, puis copiez le code de 16 lettres, sans les espaces.

**L'application ne démarre pas si `spring.datasource.password` ou `JWT_SECRET` manque.** Sans les lignes `administrateur`, elle démarre, mais aucun administrateur n'est créé.

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

### Essayer l'API

Avec Postman, par exemple, l'application démarrée.

| Route | Accès | Ce qu'elle fait |
|---|---|---|
| `POST /auth/inscription` | Publique | Crée un compte patient. Répond `201 Created` |
| `POST /auth/connexion` | Publique | Vérifie l'email et le mot de passe, puis renvoie un jeton |
| `GET /utilisateurs/moi` | Avec jeton | Renvoie le profil de la personne connectée, sans mot de passe |
| `POST /personnel` | Administrateur | Crée un médecin, une secrétaire ou un pharmacien, puis lui envoie un lien d'activation. Répond `201 Created` |
| `POST /auth/activation` | Publique | Active un compte avec le jeton du lien et le mot de passe choisi. Répond `204 No Content` |

**1. S'inscrire :** `POST http://localhost:8081/auth/inscription`, avec ce corps en JSON (*Body → raw → JSON*) :

```json
{
  "nom": "Diop",
  "prenom": "Awa",
  "email": "awa@exemple.sn",
  "motDePasse": "secret123",
  "dateNaissance": "1995-04-12",
  "sexe": "FEMININ",
  "telephone": "771234567",
  "adresse": "Dakar"
}
```

Tous les champs sont obligatoires. Le mot de passe doit contenir entre 8 et 72 caractères.

**2. Se connecter :** `POST http://localhost:8081/auth/connexion`, avec l'email et le mot de passe :

```json
{
  "email": "awa@exemple.sn",
  "motDePasse": "secret123"
}
```

La réponse contient le jeton. Copiez-le.

**3. Voir son profil :** `GET http://localhost:8081/utilisateurs/moi`, sans corps. Dans l'onglet *Authorization*, choisissez *Bearer Token* et collez le jeton. Sans jeton, la réponse est `401`.

#### Créer un membre du personnel

Le personnel ne s'inscrit pas lui-même : c'est l'administrateur qui crée son compte. L'administrateur ne choisit pas le mot de passe. La personne reçoit un email et choisit elle-même son mot de passe, que personne d'autre ne connaît.

**1. Se connecter en administrateur :** `POST /auth/connexion` avec l'email et le mot de passe `administrateur` de votre `.env`. Copiez le jeton.

**2. Créer le compte :** `POST http://localhost:8081/personnel`, avec le jeton de l'administrateur (*Bearer Token*) :

```json
{
  "nom": "Fall",
  "prenom": "Moussa",
  "email": "une.adresse@dont-vous-lisez-les-emails.sn",
  "role": "MEDECIN",
  "specialite": "Cardiologie",
  "numeroOrdre": "SN-12345"
}
```

- `role` vaut `MEDECIN`, `SECRETAIRE` ou `PHARMACIEN`.
- `specialite` et `numeroOrdre` ne sont obligatoires que pour un médecin.

Le compte est créé **inactif** : il ne peut pas encore se connecter. Un email est envoyé à l'adresse indiquée. Regardez aussi dans les spams.

**3. Activer le compte :** le lien de l'email mène à une page qui n'existe pas encore. Copiez la fin du lien, tout ce qui suit `jeton=`. Puis envoyez `POST http://localhost:8081/auth/activation`, **sans jeton** (*No Auth*) :

```json
{
  "jeton": "la_fin_du_lien",
  "motDePasse": "secret123"
}
```

La réponse `204` n'a pas de corps : c'est normal. Le lien est valable 48 heures et ne sert qu'une seule fois. La personne peut maintenant se connecter avec son email et ce mot de passe.

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
| Réponse `401` | Il faut être connecté pour accéder à cette route, ou le jeton a expiré | Inscrivez-vous si besoin, connectez-vous, puis envoyez le jeton (voir « Essayer l'API ») |
| Réponse `400` avec le code `DONNEES_INVALIDES` | Un champ manque ou est mal rempli | La liste `erreurs` de la réponse indique quel champ corriger |
| Réponse `409` avec le code `EMAIL_DEJA_UTILISE` | Un compte existe déjà avec cet email | Connectez-vous, ou choisissez un autre email |
| Réponse `403` | Vous êtes connecté, mais votre rôle n'a pas le droit d'utiliser cette route | Connectez-vous avec le bon compte, par exemple l'administrateur pour `POST /personnel` |
| Réponse `400` avec le code `JETON_INVALIDE` | Le lien d'activation est faux, a expiré ou a déjà servi | Vérifiez que vous avez copié toute la fin du lien. Il n'est pas encore possible de renvoyer une invitation (voir section 5) |
| `cannot find symbol` sur `MEDECIN`, ou `incompatible types` avec `Role` | VS Code a importé une autre classe `Role` : `javax.management.relation.Role` | Remplacez l'import par `com.forceN.gestionpatient.utilisateur.enums.Role` |
| `'url' attribute is not specified` après un redémarrage automatique | Un `clean` a vidé le dossier `target` pendant que l'application tournait | Arrêtez l'application, puis relancez-la. Ne lancez pas `clean` pendant qu'elle tourne |

## 5. Où en est le projet

**Ce qui fonctionne :**

- L'organisation du code par domaine
- La gestion commune des erreurs
- La sécurité : connexion, jetons et routes fermées par défaut
- L'inscription d'un patient : données vérifiées, mot de passe chiffré, email unique
- La consultation de son profil
- La création du personnel par l'administrateur, avec une invitation par email
- La création du premier administrateur au démarrage

**Ce qui manque :**

- [ ] **Renvoyer une invitation.** Si le lien d'activation a expiré, il n'existe pas encore de route pour en envoyer un nouveau.
- [ ] **Changer son mot de passe**, une fois connecté.
- [ ] **Corriger le contenu des DTO.** Beaucoup contiennent encore des entités.
- [ ] **Écrire les fonctionnalités :** les contrôleurs, les services et les repositories de chaque domaine.
- [ ] **Décider des droits de chaque rôle :** ce que chacun peut voir et faire.
- [ ] **Choisir les responsables de chaque partie.** Voir [ARCHITECTURE.md](ARCHITECTURE.md), section 4.
