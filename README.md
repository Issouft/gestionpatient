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

Le projet a deux parties, dans le même dépôt :

- **le backend** (dossier racine) : une **API**, un programme qui reçoit des demandes et répond avec des données au format JSON ;
- **le frontend** (dossier `frontend/`) : les écrans, dans le navigateur. Ils appellent l'API. Voir [frontend/README.md](frontend/README.md).

**Technologies :** Java 21, Spring Boot, PostgreSQL et Maven pour le backend ; Angular 21 et Angular Material pour le frontend.

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
- **Node.js 20 ou plus**, pour le frontend (`node -v` affiche la version)
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

Vous n'avez pas besoin de créer les tables : l'application s'en charge à son démarrage, avec Flyway (voir « Modifier la base de données »).

### Étape 3 : créer le fichier `.env`

Un **secret** est une information qui donne un accès, comme un mot de passe. Il ne doit jamais être publié sur GitHub.

On range donc les secrets dans un fichier nommé `.env`, à la racine du projet. Git ignore ce fichier, qui reste sur votre ordinateur. **Chacun a le sien.**

Créez le fichier `.env` avec ce contenu :

```properties
spring.datasource.password=le_mot_de_passe_de_votre_PostgreSQL
JWT_SECRET=une_longue_suite_de_caracteres_aleatoires
spring.mail.username=votre.adresse@gmail.com
spring.mail.password=le_mot_de_passe_d_application_gmail
super-administrateur.email=email_du_super_administrateur
```

| Ligne | À quoi elle sert |
|---|---|
| `spring.datasource.password` | Le mot de passe que l'application utilise pour se connecter à **votre** PostgreSQL |
| `JWT_SECRET` | La clé qui permet au serveur de signer les jetons de connexion |
| `spring.mail.username` et `spring.mail.password` | Le compte Gmail qui envoie les emails : invitation du personnel et mot de passe oublié |
| `super-administrateur.email` | L'email du super-administrateur, qui gère la plateforme. Il n'y a pas de mot de passe ici : voir « Le super-administrateur » ci-dessous |

Pour obtenir une valeur pour `JWT_SECRET`, lancez cette commande et copiez le résultat :

```bash
openssl rand -hex 64
```

Pour `spring.mail.password`, n'utilisez pas votre mot de passe Gmail habituel, mais un **mot de passe d'application** :

1. Sur [myaccount.google.com](https://myaccount.google.com), activez la validation en deux étapes.
2. Dans la barre de recherche du compte, tapez « Mots de passe des applications ».
3. Créez-en un, puis copiez le code de 16 lettres, sans les espaces.

**L'application ne démarre pas si `spring.datasource.password` ou `JWT_SECRET` manque.** Sans la ligne `super-administrateur.email`, elle démarre, mais aucun super-administrateur n'est créé.

#### Le super-administrateur

Le **super-administrateur** gère la plateforme : il crée les établissements et invite leurs administrateurs. Il n'appartient à aucun établissement, et **il n'a accès à aucune donnée des cliniques** (personnel, dossiers, consultations, rapports…) : c'est une décision du groupe, pour la confidentialité des données de santé.

Au premier démarrage, s'il n'existe aucun super-administrateur, l'application en crée un avec l'email de `.env`. Son compte est **inactif**, et il reçoit un email avec un lien pour choisir **lui-même** son mot de passe. Ainsi, aucun mot de passe n'est écrit dans un fichier, et les développeurs ne le connaissent pas.

- Le lien se termine par `?jeton=...`. Comme pour le personnel, envoyez ce jeton et le mot de passe choisi à `POST /auth/activation` (voir « Créer un membre du personnel », étape 3).
- Lien perdu ou expiré ? Redémarrez l'application : tant que le super-administrateur n'a pas activé son compte, un nouveau lien part à chaque démarrage. L'ancien ne marche plus.
- Si l'email ne part pas, l'application démarre quand même, avec un avertissement dans la console.
- **L'adresse doit être nouvelle.** Si elle appartient déjà à un compte (patient, administrateur…), le super-administrateur n'est pas créé, et la console affiche une erreur qui le dit. L'application démarre quand même.

**Si votre `.env` contient encore `administrateur.email` :** remplacez-la par `super-administrateur.email`, avec une **autre** adresse. L'ancienne ligne ne sert plus. L'administrateur qui existe déjà dans votre base reste l'administrateur de la « Clinique principale », avec le même mot de passe.

## 2. Lancer le projet

Sous Windows, remplacez `./mvnw` par `mvnw.cmd` dans les commandes.

### Démarrer l'application

```bash
./mvnw spring-boot:run
```

L'application répond ensuite à l'adresse `http://localhost:8081`. Appuyez sur `Ctrl+C` pour l'arrêter.

### Démarrer le frontend

Dans un **second terminal**, l'API étant démarrée :

```bash
cd frontend
npm ci      # la première fois, puis après chaque changement de package-lock.json
npm start
```

Ouvrez **http://localhost:4200**. Les écrans appellent l'API sur le port 8081. Détails, tests et organisation du code : [frontend/README.md](frontend/README.md).

- L'API n'accepte les appels du navigateur que depuis l'adresse du frontend (CORS). En production, mettez la vraie adresse dans la variable `FRONTEND_URL`.
- Les liens des emails (activation, mot de passe oublié) mènent aussi à cette adresse.

### Lancer les tests

Les tests vérifient automatiquement que le code fonctionne. PostgreSQL doit être démarré, et votre fichier `.env` doit être rempli.

```bash
./mvnw test
```

Si tout va bien, vous verrez `BUILD SUCCESS`. Une longue trace d'erreur peut s'afficher pendant les tests : c'est normal, car l'un d'eux simule volontairement une panne.

Les tests ne modifient pas votre base : chacun est annulé à la fin. Ils n'envoient pas non plus d'emails : l'envoi est remplacé par un faux pendant les tests.

### Modifier la base de données

Les tables sont créées et modifiées par **Flyway**, à partir des fichiers SQL du dossier `src/main/resources/db/migration`. Chaque fichier est une **migration** : une étape numérotée de l'évolution de la base.

Au démarrage, Flyway regarde dans la table `flyway_schema_history` les migrations déjà appliquées à votre base, puis applique les nouvelles, dans l'ordre. Tout le monde a donc la même base, et le serveur aussi le jour de la mise en ligne. Hibernate, lui, ne crée plus rien : il vérifie seulement que les entités correspondent aux tables (`ddl-auto=validate`).

**Ajouter une table ou une colonne :**

1. Modifiez l'entité Java, comme avant.
2. Créez dans `src/main/resources/db/migration` un fichier qui porte le numéro suivant. Par exemple, si le dernier est `V3__...sql`, créez `V4__ajouter_telephone_medecin.sql`, avec **deux** `_` après le numéro.
3. Écrivez-y le SQL de la modification :

   ```sql
   alter table medecin add column telephone varchar(255);
   ```

4. Redémarrez l'application. La console affiche `Successfully applied 1 migration`.

**Les règles :**

- **Ne modifiez jamais une migration déjà fusionnée dans `main`.** Elle a déjà été appliquée chez les autres, et Flyway refuse de démarrer si son contenu change. Pour corriger, ajoutez une nouvelle migration.
- **Deux personnes ne prennent pas le même numéro.** Avant d'ouvrir votre PR, faites `git pull` et vérifiez que votre numéro est toujours libre. S'il est pris, renommez votre fichier.
- **Une migration par changement**, avec un nom qui dit ce qu'elle fait.

`V1__schema_initial.sql` décrit la base telle qu'elle était avant Flyway. Si votre base existait déjà, Flyway ne le rejoue pas : il la marque simplement « version 1 », sans toucher à vos données.

### Tester avec Swagger

Le plus simple pour essayer l'API : une page dans le navigateur, sans rien installer. Elle liste toutes les routes, regroupées par partie, et se met à jour toute seule quand une route change.

1. Démarrez l'application, puis ouvrez **http://localhost:8081/swagger-ui.html**.
2. Ouvrez *Authentification* → `POST /auth/connexion` → *Try it out*. Écrivez l'email et le mot de passe, puis cliquez sur *Execute*.
3. Dans la réponse, copiez la valeur du champ `jeton`, sans les guillemets.
4. Cliquez sur **Authorize** en haut de la page, collez le jeton (sans écrire `Bearer`), puis *Authorize* et *Close*.
5. Toutes les routes envoient maintenant ce jeton. Ouvrez-en une, puis *Try it out* → *Execute*.

- Le jeton est gardé si vous rechargez la page. Pour changer de compte, cliquez sur **Authorize** → *Logout*, puis collez le nouveau jeton.
- Le personnel qui travaille dans plusieurs établissements utilise ensuite `POST /auth/etablissement`, puis colle **le nouveau jeton** dans *Authorize*.
- Une route protégée répond toujours `401` sans jeton : Swagger n'ouvre aucun accès.
- **En production**, mettez `SWAGGER_ACTIF=false` dans les variables d'environnement : la page et la liste des routes n'existent plus (`404`).

### Essayer l'API

Les routes sont décrites ici. On peut les essayer avec Swagger (voir plus haut) ou avec Postman, l'application démarrée.

| Route | Accès | Ce qu'elle fait |
|---|---|---|
| `POST /auth/inscription` | Publique | Crée un compte patient, et lui fait rejoindre la clinique choisie si `etablissementId` est donné. Répond `201 Created` |
| `POST /auth/connexion` | Publique | Vérifie l'email et le mot de passe, puis renvoie un jeton et, pour le personnel, ses établissements |
| `POST /auth/etablissement` | Avec jeton | Le personnel choisit (ou change) l'établissement où il travaille. Renvoie un nouveau jeton |
| `GET /utilisateurs/moi` | Avec jeton | Renvoie le profil de la personne connectée, sans mot de passe. Un patient y voit aussi sa date de naissance, son âge, son téléphone et son adresse ; un médecin, sa spécialité et son numéro d'ordre |
| `PUT /utilisateurs/moi` | Avec jeton | Modifie le nom et le prénom ; pour un patient, aussi le téléphone et l'adresse. Renvoie le profil à jour |
| `PUT /utilisateurs/moi/mot-de-passe` | Avec jeton | Change le mot de passe de la personne connectée, après vérification de l'ancien. Répond `204 No Content` |
| `POST /auth/mot-de-passe-oublie` | Publique | Envoie par email un lien valable 1 h pour choisir un nouveau mot de passe. Répond toujours `204 No Content`, même si l'email n'a pas de compte |
| `POST /auth/reinitialisation` | Publique | Change le mot de passe avec le jeton du lien. Répond `204 No Content` |
| `POST /personnel` | Administrateur | Crée un médecin, une secrétaire ou un pharmacien dans l'établissement de l'administrateur, puis lui envoie un lien d'activation. Répond `201 Created` |
| `POST /auth/activation` | Publique | Active un compte avec le jeton du lien et le mot de passe choisi. Répond `204 No Content` |
| `POST /personnel/{id}/invitation` | Administrateur | Envoie un nouveau lien d'activation à une personne de son établissement qui n'a pas encore activé son compte. L'ancien lien ne marche plus. Répond `204 No Content` |
| `GET /personnel` | Administrateur | Liste les médecins, secrétaires et pharmaciens de son établissement, actifs ou non, triés par nom |
| `POST /personnel/{id}/desactivation` | Administrateur | Suspend un membre du personnel dans son établissement : son jeton pour cet établissement est refusé tout de suite. Ses autres établissements ne changent pas. Répond `204 No Content` |
| `POST /personnel/{id}/reactivation` | Administrateur | Rend l'accès à l'établissement à un membre suspendu, avec le même mot de passe. Répond `204 No Content` |
| `POST /etablissements` | Super-administrateur | Crée un établissement. Répond `201 Created` |
| `GET /etablissements` | Super-administrateur | Liste tous les établissements, actifs ou non, triés par nom |
| `POST /etablissements/{id}/desactivation` | Super-administrateur | Désactive un établissement : tout son personnel perd l'accès tout de suite. Répond `204 No Content` |
| `POST /etablissements/{id}/reactivation` | Super-administrateur | Réactive un établissement. Répond `204 No Content` |
| `PUT /etablissements/{id}` | Super-administrateur | Modifie la fiche d'un établissement (mêmes champs et mêmes règles qu'à la création). Ne change pas `actif` |
| `POST /etablissements/{id}/administrateurs` | Super-administrateur | Invite l'administrateur d'un établissement : compte inactif rattaché à l'établissement, lien d'activation par email. Répond `201 Created` |
| `GET /annuaire/etablissements` | Publique | Liste les établissements actifs, triés par nom. Filtres facultatifs : `?type=CLINIQUE&ville=Thiès` |
| `GET /annuaire/etablissements/{id}` | Publique | La fiche publique d'un établissement actif. `404` s'il n'existe pas ou est désactivé |
| `GET /annuaire/etablissements/{id}/medecins` | Publique | Liste les médecins en activité de cet établissement : nom, prénom et spécialité. Filtre facultatif : `?specialite=Cardiologie` |
| `POST /patients/moi/etablissements/{id}` | Patient | Le patient rejoint cette clinique : elle pourra voir ses rendez-vous et son dossier. Répond `204 No Content`, même s'il l'avait déjà rejointe |
| `GET /patients/moi/etablissements` | Patient | Liste les cliniques actives que le patient a rejointes, triées par nom |

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
  "adresse": "Dakar",
  "etablissementId": 7
}
```

Tous les champs sont obligatoires, sauf `etablissementId`. Le mot de passe doit contenir entre 8 et 72 caractères.

`etablissementId` est la clinique choisie dans l'annuaire (voir « Consulter l'annuaire (public) ») : le patient la rejoint dès l'inscription. Sans ce champ, il pourra en rejoindre une plus tard. Une clinique inconnue ou désactivée donne `404` avec le code `ETABLISSEMENT_INTROUVABLE`, et le compte n'est pas créé.

**2. Se connecter :** `POST http://localhost:8081/auth/connexion`, avec l'email et le mot de passe :

```json
{
  "email": "awa@exemple.sn",
  "motDePasse": "secret123"
}
```

La réponse contient le jeton (`jeton`). Copiez-le. Pour un patient, `etablissementId` vaut `null` et la liste `etablissements` est vide : c'est normal, un patient n'a pas d'établissement à choisir.

**Pour le personnel**, la réponse liste aussi les établissements où il travaille, avec ses rôles :

```json
{
  "jeton": "eyJhbGciOi...",
  "etablissementId": null,
  "etablissements": [
    { "etablissementId": 1, "etablissementNom": "Clinique A", "roles": ["MEDECIN"] },
    { "etablissementId": 4, "etablissementNom": "Clinique B", "roles": ["MEDECIN"] }
  ]
}
```

- **Un seul établissement :** `etablissementId` est déjà rempli, et le jeton permet de travailler tout de suite.
- **Plusieurs établissements :** `etablissementId` vaut `null`. Choisissez-en un avec `POST http://localhost:8081/auth/etablissement`, avec ce jeton (*Bearer Token*) et ce corps : `{"etablissementId": 4}`. La réponse contient un **nouveau jeton** : utilisez-le pour la suite. La même route sert à changer d'établissement.

Les comptes qui existaient avant les établissements sont rattachés à la « Clinique principale » : ils n'ont rien à choisir.

**3. Voir son profil :** `GET http://localhost:8081/utilisateurs/moi`, sans corps. Dans l'onglet *Authorization*, choisissez *Bearer Token* et collez le jeton. Sans jeton, la réponse est `401`.

**Modifier son profil :** `PUT http://localhost:8081/utilisateurs/moi`, avec le jeton (*Bearer Token*) et ce corps en JSON :

```json
{
  "nom": "Diop",
  "prenom": "Awa",
  "telephone": "781234567",
  "adresse": "Thiès"
}
```

Le nom et le prénom sont obligatoires. Le téléphone et l'adresse ne concernent que le patient : s'ils sont absents, ils ne changent pas. L'email, le rôle et le mot de passe ne se changent pas ici, même s'ils sont envoyés.

**4. Changer son mot de passe :** `PUT http://localhost:8081/utilisateurs/moi/mot-de-passe`, avec le jeton (*Bearer Token*) et ce corps en JSON :

```json
{
  "ancienMotDePasse": "secret123",
  "nouveauMotDePasse": "nouveau456"
}
```

La réponse est `204`, sans contenu. Le nouveau mot de passe suit les mêmes règles qu'à l'inscription. **Toutes les sessions sont fermées**, y compris celle en cours : les jetons créés avant le changement sont refusés (`401`). Reconnectez-vous avec le nouveau mot de passe. Ainsi, quelqu'un qui aurait volé une session la perd aussitôt.

**5. Mot de passe oublié :** `POST http://localhost:8081/auth/mot-de-passe-oublie`, sans jeton, avec ce corps :

```json
{
  "email": "awa@exemple.sn"
}
```

La réponse est toujours `204`, que l'email ait un compte ou non : on ne révèle pas quels comptes existent. Si le compte existe et qu'il est actif, un email part avec un lien valable **1 heure**. Le lien se termine par `?jeton=...` : copiez ce jeton, avec un clic droit.

Puis `POST http://localhost:8081/auth/reinitialisation`, sans jeton de connexion, avec ce corps :

```json
{
  "jeton": "le-jeton-copié-depuis-le-lien",
  "motDePasse": "nouveau789"
}
```

La réponse est `204`. Le lien ne marche qu'une fois, et seul le dernier lien demandé est valable. Comme pour un changement de mot de passe, les sessions ouvertes avant sont fermées. Un compte inactif ne reçoit pas de lien : un membre du personnel qui n'a pas encore activé son compte doit utiliser son invitation.

#### Consulter l'annuaire (public)

Avant même de créer son compte, un patient peut chercher une clinique et voir ses médecins. Ces routes marchent **sans jeton** (*No Auth* dans Postman).

**1. Chercher un établissement :** `GET http://localhost:8081/annuaire/etablissements?ville=Thiès&type=CLINIQUE`

- Les deux filtres sont facultatifs. La ville ne tient pas compte des majuscules.
- `type` vaut `CLINIQUE`, `CABINET`, `PHARMACIE` ou `LABORATOIRE`. Une autre valeur donne `400` avec le code `PARAMETRE_INVALIDE`.
- Seuls les établissements **actifs** apparaissent, avec leur fiche publique : nom, type, adresse, ville, téléphone et email de contact.

**2. Voir ses médecins :** `GET http://localhost:8081/annuaire/etablissements/7/medecins?specialite=Cardiologie`

```json
[
  { "id": 12, "nom": "Ndiaye", "prenom": "Fatou", "specialite": "Cardiologie" }
]
```

- Le filtre `specialite` est facultatif, et ne tient pas compte des majuscules.
- N'apparaissent que les médecins **en activité** : compte activé, et pas suspendus dans cet établissement.
- **Données protégées :** ni email, ni numéro d'ordre, ni téléphone. Les secrétaires, pharmaciens et administrateurs n'apparaissent pas.
- `404` avec le code `ETABLISSEMENT_INTROUVABLE` : l'établissement n'existe pas **ou** il est désactivé. La réponse est la même dans les deux cas.

#### Rejoindre une clinique (patient)

Une clinique ne voit un patient (rendez-vous, dossier...) **que s'il l'a rejointe lui-même**. Il peut le faire à l'inscription (`etablissementId`), ou plus tard avec son jeton :

**1. Rejoindre :** `POST http://localhost:8081/patients/moi/etablissements/7`, **sans corps**, avec le jeton du patient. Réponse `204`.

- Il peut rejoindre plusieurs cliniques. Rejoindre une clinique déjà rejointe ne change rien (`204`).
- `404` avec le code `ETABLISSEMENT_INTROUVABLE` : la clinique n'existe pas ou est désactivée.
- `403` avec le code `ADHESION_SUSPENDUE` : la clinique a suspendu son adhésion. Il ne peut pas l'annuler seul.
- `403` avec le code `ACCES_REFUSE` : seul un patient peut rejoindre une clinique.

**2. Voir ses cliniques :** `GET http://localhost:8081/patients/moi/etablissements`. Une clinique désactivée disparaît de la liste.

#### Gérer les établissements (super-administrateur)

Le super-administrateur crée les établissements de la plateforme. Il voit leur fiche (nom, type, adresse, contact), mais **jamais leurs données** : ni personnel, ni patients, ni dossiers.

**1. Se connecter en super-administrateur :** `POST /auth/connexion` avec son email et le mot de passe qu'il a choisi (voir « Le super-administrateur »). Copiez le jeton.

**2. Créer un établissement :** `POST http://localhost:8081/etablissements`, avec le jeton du super-administrateur (*Bearer Token*) :

```json
{
  "nom": "Clinique du Plateau",
  "type": "CLINIQUE",
  "adresse": "12 rue Carnot",
  "ville": "Dakar",
  "telephone": "338210000",
  "email": "contact@clinique-plateau.sn"
}
```

- `nom` et `type` sont obligatoires. `type` vaut `CLINIQUE`, `CABINET`, `PHARMACIE` ou `LABORATOIRE`.
- `email` est l'adresse de contact de l'établissement : ce n'est pas un compte de connexion.
- Réponse `201`, avec `"actif": true`. Notez l'`id` : il sert à inviter l'administrateur de l'établissement.

**3. Lister les établissements :** `GET http://localhost:8081/etablissements`. Le champ `actif` indique si chacun est actif.

**Modifier une fiche :** `PUT http://localhost:8081/etablissements/7`, avec le même corps qu'à la création. Le champ `actif` est ignoré : l'activation a ses propres routes.

**4. Désactiver un établissement :** `POST http://localhost:8081/etablissements/7/desactivation`, **sans corps**. Réponse `204`.

- Tout son personnel perd l'accès à sa requête suivante (`401` avec le code `ACCES_ETABLISSEMENT_RETIRE`), et l'établissement n'est plus proposé à la connexion.
- Ses données restent en base. `POST /etablissements/7/reactivation` le réactive, et son personnel retrouve l'accès.
- `404` avec le code `ETABLISSEMENT_INTROUVABLE` : aucun établissement ne porte cet `id`.

#### Créer l'administrateur d'un établissement

C'est le super-administrateur qui invite l'administrateur d'un établissement. Comme pour le personnel, il ne choisit pas le mot de passe : la personne reçoit un email et le choisit elle-même.

**1. Inviter l'administrateur :** `POST http://localhost:8081/etablissements/7/administrateurs` (7 est l'`id` de l'établissement), avec le jeton du super-administrateur (*Bearer Token*) :

```json
{
  "nom": "Diop",
  "prenom": "Awa",
  "email": "une.adresse@dont-vous-lisez-les-emails.sn"
}
```

- Réponse `201`, avec `"role": "ADMINISTRATEUR"` et `"actif": false` : le compte existe, mais il ne peut pas encore se connecter.
- Le compte est rattaché à cet établissement. Un établissement peut avoir plusieurs administrateurs.
- `409` avec le code `EMAIL_DEJA_UTILISE` : cet email a déjà un compte. Un compte n'a qu'un rôle : choisissez une autre adresse.
- `404` avec le code `ETABLISSEMENT_INTROUVABLE` : aucun établissement ne porte cet `id`.

**2. Activer le compte :** la personne ouvre le lien reçu par email et choisit son mot de passe. Pour tester sans navigateur : `POST /auth/activation` avec le jeton du lien et le mot de passe, comme pour le personnel (voir « Créer un membre du personnel »).

**3. Se connecter :** `POST /auth/connexion`. S'il n'administre qu'un établissement, le jeton le désigne directement. Il peut alors gérer le personnel de cet établissement.

#### Créer un membre du personnel

Le personnel ne s'inscrit pas lui-même : c'est l'administrateur qui crée son compte. L'administrateur ne choisit pas le mot de passe. La personne reçoit un email et choisit elle-même son mot de passe, que personne d'autre ne connaît.

**Tout se passe dans l'établissement de l'administrateur**, celui de son jeton. La personne créée y est rattachée automatiquement. L'administrateur ne voit ni ne touche le personnel des autres établissements : pour lui, il n'existe pas (`404`).

**1. Se connecter en administrateur :** `POST /auth/connexion` avec l'email de l'administrateur et le mot de passe qu'il a choisi (voir « Créer l'administrateur d'un établissement »). Copiez le jeton.

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

Le compte est créé **inactif** : il ne peut pas encore se connecter. Un email est envoyé à l'adresse indiquée. Regardez aussi dans les spams. Notez l'`id` de la réponse : il sert à renvoyer l'invitation (étape 4).

**3. Activer le compte :** le plus simple est d'ouvrir le lien de l'email, le frontend étant démarré (voir « Démarrer le frontend ») : la page d'activation demande le mot de passe et sa confirmation. Pour tester sans le frontend, faites un clic droit sur le lien, puis *Copier l'adresse du lien*, et gardez tout ce qui suit `jeton=`. Ne recopiez pas le jeton à la main : des caractères comme `I`, `l` et `1` se confondent facilement. Puis envoyez `POST http://localhost:8081/auth/activation`, **sans jeton** (*No Auth*) :

```json
{
  "jeton": "la_fin_du_lien",
  "motDePasse": "secret123"
}
```

La réponse `204` n'a pas de corps : c'est normal. Le lien est valable 48 heures et ne sert qu'une seule fois. La personne peut maintenant se connecter avec son email et ce mot de passe.

**4. Renvoyer l'invitation (si besoin) :** si le lien a expiré ou si l'email a été perdu, l'administrateur envoie `POST http://localhost:8081/personnel/51/invitation`, avec son jeton (*Bearer Token*) et **sans corps** (*Body → none*). Remplacez `51` par l'`id` de la personne.

- `204` : un nouvel email est parti. **Seul le dernier lien marche** : les anciens sont désactivés, pour qu'il n'y ait jamais deux liens valides en même temps.
- `409` avec le code `COMPTE_DEJA_ACTIF` : la personne a déjà activé son compte, aucun email n'est envoyé.
- `404` avec le code `UTILISATEUR_INTROUVABLE` : aucun membre du personnel de votre établissement ne porte cet `id`.

**5. Lister le personnel :** `GET http://localhost:8081/personnel`, avec le jeton de l'administrateur. Seul le personnel de son établissement apparaît. Pour chaque personne :

- `statutDansEtablissement` : `ACTIVE`, ou `SUSPENDUE` si l'administrateur l'a suspendue (étape 6) ;
- `actif` : `false` tant que la personne n'a pas activé son compte avec le lien de l'email.

**6. Suspendre un membre du personnel :** `POST http://localhost:8081/personnel/51/desactivation`, avec le jeton de l'administrateur et **sans corps**. Réponse `204`, même si la personne était déjà suspendue.

- La suspension ne vaut que **dans l'établissement de l'administrateur**. Le jeton de la personne pour cet établissement est refusé dès sa requête suivante (`401` avec le code `ACCES_ETABLISSEMENT_RETIRE`).
- Si elle travaille aussi dans un autre établissement, elle continue d'y travailler normalement : un administrateur ne peut pas couper son accès ailleurs. Son compte et son mot de passe ne changent pas.
- `404` si l'`id` n'est pas celui d'un médecin, d'une secrétaire ou d'un pharmacien de votre établissement : un patient, un administrateur ou le personnel d'un autre établissement ne se suspend pas ici.

**7. Réactiver un membre du personnel :** `POST http://localhost:8081/personnel/51/reactivation`, avec le jeton de l'administrateur et **sans corps**. Réponse `204`, même si la personne n'était pas suspendue. Elle retrouve son accès à l'établissement **avec le même mot de passe** : aucun email n'est envoyé. Mêmes cas de `404` que pour la suspension.

#### Le parcours médical (Partie 3)

Cette partie suit le patient après son rendez-vous : le médecin le consulte, rédige une ordonnance, puis le patient demande ses médicaments au pharmacien. L'administrateur peut ensuite générer des rapports.

Toutes ces routes demandent un jeton (*Bearer Token*). **Chaque étape indique avec quel compte se connecter.** Avec le jeton d'un autre rôle, la réponse est `403`.

**Avant de commencer, il vous faut quatre comptes actifs :**

| Compte | Comment l'obtenir | Où trouver son `id` |
|---|---|---|
| Un patient | `POST /auth/inscription` (voir « Essayer l'API », étape 1) | Le champ `id` de la réponse, ou `GET /utilisateurs/moi` avec son jeton |
| Un médecin | `POST /personnel` avec `"role": "MEDECIN"`, puis activation (voir « Créer un membre du personnel ») | Le champ `id` de la réponse, ou `GET /personnel` avec le jeton de l'administrateur |
| Une secrétaire | Pareil, avec `"role": "SECRETAIRE"` | Pas besoin : elle est reconnue grâce à son jeton |
| Un pharmacien | Pareil, avec `"role": "PHARMACIEN"` | Pas besoin : il est reconnu grâce à son jeton |

Connectez chaque compte avec `POST /auth/connexion` et gardez les quatre jetons sous la main. Dans les exemples ci-dessous, le patient a l'`id` `12` et le médecin l'`id` `5` : **remplacez-les par vos propres `id`.**

**1. Ajouter un médicament** (jeton du **pharmacien**) : `POST http://localhost:8081/medicaments`

```json
{
  "nom": "Paracétamol",
  "dosage": "500 mg",
  "stock": 100,
  "seuilAlerte": 10
}
```

- Tous les champs sont obligatoires. `stock` et `seuilAlerte` ne peuvent pas être négatifs.
- Le stock n'est donné **qu'à la création**. Il n'y a pas de route pour le modifier : il baisse tout seul à chaque retrait confirmé (étape 6).
- Réponse `201`. Notez l'`id` du médicament : il sert aux étapes 5 et 6.
- `409 MEDICAMENT_DEJA_EXISTANT` : un médicament avec le même nom et le même dosage existe déjà (majuscules et minuscules ne comptent pas).

Pour revoir les médicaments : `GET http://localhost:8081/medicaments`, avec **n'importe quel jeton**. Chaque médicament indique son stock, `disponible` (stock supérieur à 0) et `sousSeuilAlerte` (stock inférieur ou égal au seuil).

**2. Créer le dossier du patient** (jeton de la **secrétaire**) : `POST http://localhost:8081/dossiers`

```json
{
  "patientId": 12,
  "antecedents": "Asthme"
}
```

- `antecedents` est facultatif.
- Réponse `201`. Le dossier contient le nom du patient et la liste de ses consultations, vide pour l'instant.
- `404 PATIENT_INTROUVABLE` : aucun patient ne porte cet `id`. L'`id` d'un médecin ou d'une secrétaire ne marche pas.
- `409 DOSSIER_DEJA_EXISTANT` : un patient n'a qu'un seul dossier. Relisez-le avec `GET /dossiers/patient/12`.

**3. Créer un rendez-vous confirmé** (dans la base, pas dans Postman)

La Partie 2 n'a pas encore de route pour créer un rendez-vous. En attendant, ajoutez-le directement dans PostgreSQL. Ouvrez la base :

```bash
sudo -u postgres psql gestionpatient
```

Puis collez cette commande, avec **vos** `id` de patient et de médecin :

```sql
INSERT INTO rendez_vous (creation_date, last_modified_date, date_heure, motif, statut_rendez_vous, patient_id, medecin_id)
VALUES (now(), now(), now(), 'Fièvre', 'CONFIRME', 12, 5)
RETURNING id;
```

PostgreSQL affiche l'`id` du rendez-vous : notez-le. Tapez `\q` pour quitter.

- `ERROR: ... violates foreign key constraint` : l'`id` du patient ou du médecin n'existe pas, ou n'a pas le bon rôle.

**4. Consulter le patient** (jeton du **médecin** du rendez-vous) : `POST http://localhost:8081/consultations`

```json
{
  "rendezVousId": 7,
  "observations": "Fièvre depuis 2 jours",
  "diagnostic": "Grippe"
}
```

- `observations` et `diagnostic` sont obligatoires.
- Réponse `201`. La consultation s'ajoute au dossier du patient, et le rendez-vous passe à `TERMINE`. Notez l'`id` de la consultation : il sert à l'étape 5.
- Les vérifications se font dans cet ordre :
  - `404 RENDEZ_VOUS_INTROUVABLE` : aucun rendez-vous ne porte cet `id` ;
  - `403 AUTRE_MEDECIN` : ce rendez-vous est celui d'un autre médecin ;
  - `409 CONSULTATION_DEJA_EXISTANTE` : ce rendez-vous a déjà été consulté ;
  - `409 RENDEZ_VOUS_NON_CONFIRME` : le rendez-vous n'est pas `CONFIRME` ;
  - `404 DOSSIER_INTROUVABLE` : le patient n'a pas de dossier. Faites d'abord l'étape 2.

Pour relire : `GET /consultations/{id}` (médecin), ou le dossier complet avec `GET /dossiers/patient/12` (secrétaire ou médecin). Les consultations du dossier sont classées de la plus récente à la plus ancienne.

**5. Rédiger l'ordonnance** (jeton du **médecin** de la consultation) : `POST http://localhost:8081/consultations/{id}/ordonnance`, où `{id}` est l'`id` de la consultation.

```json
{
  "lignes": [
    { "medicamentId": 3, "quantite": 2, "posologie": "1 comprimé matin et soir", "dureeJours": 5 }
  ]
}
```

- Au moins une ligne. Dans chaque ligne, tous les champs sont obligatoires, et `quantite` et `dureeJours` doivent être supérieurs à 0.
- Réponse `201`. Chaque ligne affiche aussi le nom et le dosage du médicament.
- **Prescrire ne change pas le stock.** L'ordonnance n'envoie pas non plus de demande au pharmacien : c'est le patient qui la fait (étape 6).
- `404 CONSULTATION_INTROUVABLE` : aucune consultation ne porte cet `id`.
- `403 AUTRE_MEDECIN` : seul le médecin qui a fait la consultation peut rédiger son ordonnance.
- `409 ORDONNANCE_DEJA_EXISTANTE` : une consultation n'a qu'une seule ordonnance.
- `404 MEDICAMENT_INTROUVABLE` : un `medicamentId` n'existe pas. Vérifiez avec `GET /medicaments`.
- `400 DONNEES_INVALIDES` : la liste `lignes` est vide, ou une ligne est mal remplie.

Pour relire : `GET /consultations/{id}/ordonnance` ou `GET /ordonnances/{id}`, avec le jeton du médecin ou du pharmacien.

**6. Demander un médicament et le retirer**

Le patient demande, le pharmacien répond, puis le patient vient chercher le produit :

```text
EN_ATTENTE ──validation──▶ VALIDEE ──retrait──▶ VALIDEE avec dateRetrait (le stock baisse)
     └────────refus──────▶ REFUSEE
```

**a. Le patient demande** (jeton du **patient**) : `POST http://localhost:8081/demandes`

```json
{
  "medicamentId": 3,
  "quantite": 2
}
```

Réponse `201`, avec le statut `EN_ATTENTE`. Notez l'`id` de la demande. Le stock n'est pas vérifié à ce moment-là : c'est le pharmacien qui répond. `404 MEDICAMENT_INTROUVABLE` si le médicament n'existe pas.

**b. Le pharmacien voit les demandes à traiter** (jeton du **pharmacien**) : `GET http://localhost:8081/demandes?statut=EN_ATTENTE`. Les plus anciennes viennent en premier. Sans `?statut=...`, toutes les demandes s'affichent, des plus récentes aux plus anciennes.

**c. Le pharmacien répond** (jeton du **pharmacien**) :

- Le produit est disponible : `POST http://localhost:8081/demandes/{id}/validation`. Le statut devient `VALIDEE`.
- Le produit n'est pas disponible : `POST http://localhost:8081/demandes/{id}/refus`. Le statut devient `REFUSEE`.

Le corps est **facultatif**. Pour laisser un message au patient, envoyez `{"reponse": "Passez demain matin"}` (255 caractères au plus). Sans corps (*Body → none*), un message par défaut est enregistré.

- `409 STOCK_INSUFFISANT` (validation seulement) : le stock est plus petit que la quantité demandée.
- `409 DEMANDE_DEJA_TRAITEE` : la demande a déjà été validée ou refusée.
- `404 DEMANDE_INTROUVABLE` : aucune demande ne porte cet `id`.
- `400 REPONSE_TROP_LONGUE` : le message dépasse 255 caractères.

**d. Le patient lit la réponse** (jeton du **patient**) : `GET http://localhost:8081/demandes/moi`. Il ne voit que ses propres demandes, des plus récentes aux plus anciennes, avec le statut et le message du pharmacien.

**e. Le pharmacien confirme le retrait** (jeton du **pharmacien**), quand le patient vient chercher le produit : `POST http://localhost:8081/demandes/{id}/retrait`, **sans corps**.

- **Le stock baisse** de la quantité demandée, et `dateRetrait` est rempli. Vérifiez avec `GET /medicaments`.
- `409 DEMANDE_NON_VALIDEE` : la demande est encore `EN_ATTENTE`, ou a été `REFUSEE`.
- `409 DEMANDE_DEJA_RETIREE` : le retrait a déjà été confirmé. Il ne se confirme qu'une fois.
- `409 STOCK_INSUFFISANT` : d'autres retraits ont vidé le stock depuis la validation.

**7. Générer un rapport** (jeton de l'**administrateur**) : `POST http://localhost:8081/rapports`

```json
{
  "type": "ACTIVITE_MEDICALE",
  "periodeDebut": "2026-11-01",
  "periodeFin": "2026-11-30"
}
```

Les dates s'écrivent `année-mois-jour`. Le premier et le dernier jour sont **inclus**. L'application calcule les chiffres et les écrit dans le champ `resume` de la réponse (`201`). Le rapport est enregistré : `GET /rapports` les liste, des plus récents aux plus anciens.

| `type` | Ce que contient `resume` |
|---|---|
| `ACTIVITE_MEDICALE` | Le nombre de consultations, de patients différents consultés, d'ordonnances et de dossiers créés pendant la période |
| `RENDEZ_VOUS` | Le nombre de rendez-vous de la période (selon leur date), au total et pour chaque statut |
| `STOCK_MEDICAMENTS` | Le nombre de médicaments, ceux sous le seuil d'alerte et ceux en rupture **au jour du rapport** ; puis les demandes reçues pendant la période, pour chaque statut, et les retraits confirmés avec le nombre d'unités sorties |

Exemple de `resume` : `Du 01/11/2026 au 30/11/2026 : 3 consultation(s) pour 2 patient(s), 2 ordonnance(s), 4 dossier(s) créé(s).`

- `400 PERIODE_INVALIDE` : la date de fin est avant la date de début.
- `400 DONNEES_INVALIDES` : `type`, `periodeDebut` ou `periodeFin` manque.

**Toutes les routes de la Partie 3, en résumé :**

| Route | Accès | Ce qu'elle fait |
|---|---|---|
| `POST /medicaments` | Pharmacien | Ajoute un médicament avec son stock de départ. Répond `201 Created` |
| `GET /medicaments` | Avec jeton | Liste les médicaments, triés par nom, avec leur stock et s'ils sont sous le seuil d'alerte |
| `POST /dossiers` | Secrétaire | Crée le dossier d'un patient. Un seul dossier par patient. Répond `201 Created` |
| `GET /dossiers/{id}` | Secrétaire, médecin | Renvoie un dossier, avec ses consultations, les plus récentes d'abord |
| `GET /dossiers/patient/{patientId}` | Secrétaire, médecin | Renvoie le dossier d'un patient, à partir de l'`id` du patient |
| `POST /consultations` | Médecin | Enregistre la consultation d'un de **ses** rendez-vous confirmés. Le rendez-vous passe à `TERMINE`. Répond `201 Created` |
| `GET /consultations/{id}` | Médecin | Renvoie une consultation |
| `POST /consultations/{id}/ordonnance` | Médecin | Rédige l'ordonnance d'une de **ses** consultations, une seule fois. Répond `201 Created` |
| `GET /consultations/{id}/ordonnance` | Médecin, pharmacien | Renvoie l'ordonnance d'une consultation |
| `GET /ordonnances/{id}` | Médecin, pharmacien | Renvoie une ordonnance |
| `POST /demandes` | Patient | Demande une quantité d'un médicament. Répond `201 Created` |
| `GET /demandes/moi` | Patient | Liste ses demandes, avec la réponse du pharmacien |
| `GET /demandes?statut=EN_ATTENTE` | Pharmacien | Liste les demandes à traiter. Sans `statut`, liste toutes les demandes |
| `POST /demandes/{id}/validation` | Pharmacien | Le produit est disponible. Refusé si le stock ne suffit pas |
| `POST /demandes/{id}/refus` | Pharmacien | Le produit n'est pas disponible |
| `POST /demandes/{id}/retrait` | Pharmacien | Le patient a pris le produit : **le stock baisse** |
| `POST /rapports` | Administrateur | Génère un rapport sur une période. Répond `201 Created` |
| `GET /rapports` | Administrateur | Liste les rapports, les plus récents d'abord |

**Ce qui n'existe pas encore**, en attendant la décision du groupe sur les droits de chaque rôle : le patient ne peut lire ni son dossier ni ses ordonnances, et tout médecin peut lire n'importe quel dossier ou consultation.

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
| Réponse `400` avec le code `PARAMETRE_INVALIDE` | Une valeur de l'adresse a un mauvais format : `/personnel/abc`, `?type=HOPITAL`... | Le message de la réponse indique le paramètre fautif et, si besoin, les valeurs possibles |
| Réponse `409` avec le code `EMAIL_DEJA_UTILISE` | Un compte existe déjà avec cet email | Connectez-vous, ou choisissez un autre email |
| Réponse `409` avec le code `NUMERO_ORDRE_DEJA_UTILISE` | Un autre médecin a déjà ce numéro d'ordre | Vérifiez le numéro d'ordre |
| Réponse `409` avec le code `DONNEE_DEJA_UTILISEE` | La base a refusé une valeur déjà prise. Arrive surtout quand la même demande est envoyée deux fois en même temps (double clic) | Vérifiez si le compte n'a pas déjà été créé |
| Réponse `503` avec le code `ENVOI_EMAIL_IMPOSSIBLE` | Le serveur d'emails n'a pas répondu. Rien n'a été créé | Vérifiez `spring.mail.username` et `spring.mail.password` dans `.env` et la connexion Internet, puis réessayez |
| Réponse `403` avec le code `ETABLISSEMENT_NON_CHOISI` | Vous travaillez dans plusieurs établissements et n'en avez pas encore choisi un | Envoyez `POST /auth/etablissement`, puis utilisez le nouveau jeton (voir « Essayer l'API », étape 2) |
| Réponse `403` avec le code `ETABLISSEMENT_NON_AUTORISE` | Vous n'êtes pas affilié à cet établissement, ou votre accès y est suspendu | Choisissez un établissement de la liste reçue à la connexion |
| Réponse `401` avec le code `ACCES_ETABLISSEMENT_RETIRE` | Votre accès à l'établissement du jeton a été retiré, ou l'établissement a été désactivé | Reconnectez-vous et choisissez un autre établissement |
| Réponse `403` | Vous êtes connecté, mais votre rôle n'a pas la permission d'utiliser cette route (voir `RolePermissions`) | Connectez-vous avec le bon compte, par exemple l'administrateur pour `POST /personnel` |
| Réponse `400` avec le code `MOT_DE_PASSE_INCORRECT` | L'ancien mot de passe envoyé pour en changer est faux | Vérifiez l'ancien mot de passe. Ce n'est pas un `401` : la session est toujours valable |
| Réponse `400` avec le code `JETON_INVALIDE` | Le lien reçu par email (invitation ou mot de passe oublié) est faux, a expiré ou a déjà servi | Copiez le lien du **dernier** email reçu, avec un clic droit plutôt qu'à la main. S'il a expiré : pour une invitation, l'administrateur peut la renvoyer (voir « Créer un membre du personnel », étape 4) ; pour un mot de passe oublié, refaites la demande |
| `cannot find symbol` sur `MEDECIN`, ou `incompatible types` avec `Role` | VS Code a importé une autre classe `Role` : `javax.management.relation.Role` | Remplacez l'import par `com.forceN.gestionpatient.utilisateur.enums.Role` |
| `Schema-validation: missing table` ou `missing column` | Une entité a une table ou un champ qu'aucune migration ne crée | Ajoutez une migration (voir « Modifier la base de données ») |
| `Schema-validation: wrong column type` | Le type d'un champ Java ne correspond pas à la colonne | Corrigez l'entité, ou ajoutez une migration qui change la colonne |
| `Validate failed: Migrations have failed validation` | Une migration déjà appliquée a été modifiée | Annulez la modification du fichier (`git restore`), puis mettez votre changement dans une nouvelle migration |
| Réponse `404` avec le code `ETABLISSEMENT_INTROUVABLE` | Aucun établissement ne porte cet `id`. Pour l'annuaire, l'inscription et l'adhésion d'un patient : ou bien il est désactivé | Vérifiez l'`id` avec `GET /annuaire/etablissements` (ou `GET /etablissements` pour le super-administrateur) |
| Réponse `403` avec le code `ADHESION_SUSPENDUE` | La clinique a suspendu l'adhésion de ce patient | Le patient doit contacter la clinique |
| `Super-administrateur non créé : ... appartient déjà à un autre compte` dans la console | L'adresse de `super-administrateur.email` est déjà celle d'un autre compte | Choisissez une autre adresse dans `.env`, puis redémarrez |
| `'url' attribute is not specified` après un redémarrage automatique | Un `clean` a vidé le dossier `target` pendant que l'application tournait | Arrêtez l'application, puis relancez-la. Ne lancez pas `clean` pendant qu'elle tourne |

## 5. Où en est le projet

### Ce qui fonctionne

**Partie 1 (comptes et sécurité) et socle SaaS : terminés, côté backend et côté frontend.**

| Pour qui | Ce qu'il peut faire | Écran (frontend) |
|---|---|---|
| Visiteur | Parcourir l'annuaire des établissements et de leurs médecins, sans compte | `/annuaire` |
| Visiteur | Créer un compte patient, en choisissant sa clinique ; mot de passe oublié | `/inscription`, `/mot-de-passe-oublie` |
| Patient | Rejoindre une ou plusieurs cliniques, voir ses cliniques | `/annuaire/:id`, `/mes-cliniques` |
| Tout compte | Voir et modifier son profil, changer son mot de passe | `/profil` |
| Personnel invité | Activer son compte avec le lien reçu par email | `/activation` |
| Personnel de plusieurs établissements | Choisir ou changer l'établissement de la session | `/choix-etablissement` |
| Administrateur | Inviter, suspendre, réactiver le personnel de **son** établissement | `/personnel` |
| Super-administrateur | Créer, modifier, désactiver, réactiver les établissements, inviter leur administrateur, **sans voir leurs données** | `/etablissements` |

Et en dessous : la sécurité par jeton avec l'établissement dans le jeton, les permissions décidées dans `RolePermissions`, des erreurs toujours accompagnées d'un `code`, Flyway pour la base, Swagger pour tester, et les tests (backend : `./mvnw test` ; frontend : `npm test` dans `frontend/`).

**Partie 3 (`medical`) :** dossier, consultation, ordonnance, médicaments, demandes de disponibilité et rapports, côté backend. Voir « Le parcours médical (Partie 3) ».

### Ce qui reste à faire

**Parties des collègues**

- [ ] **Partie 2 (`rendezvous`) :** contrôleurs, services et repositories, puis les écrans (prise de rendez-vous depuis la fiche d'une clinique, tableau de bord du patient). La carte « Mes rendez-vous » de l'accueil du patient attend cet écran.
- [ ] **Partie 3 (`medical`) par établissement :** rattacher dossiers, consultations, ordonnances et médicaments à un établissement, puis les écrans.
- **Pour ces deux parties, la règle est prête :** une clinique ne voit un patient que s'il l'a rejointe. Le service vérifie `affiliationService.estPatientDe(patientId, connecte.etablissementId())` et répond `404` sinon. Voir [ARCHITECTURE.md](ARCHITECTURE.md), section 7.

**À décider en groupe, avant de coder**

- [ ] **Les droits de chaque rôle :** ce que chacun peut voir et faire. Il suffira ensuite de modifier `RolePermissions` : aucun contrôleur à toucher.
- [ ] **Le patient sans email** ou qui ne maîtrise pas le numérique : qui crée son compte, avec quel identifiant.
- [ ] **L'administrateur et les patients de sa clinique :** peut-il voir leur liste ? Si oui : une permission `PATIENT_GERER` pour l'administrateur, une route `GET /patients` limitée à son établissement (avec `AffiliationService.comptesDe`), et des routes pour suspendre ou réactiver l'adhésion d'un patient (`AffiliationService.changerStatut` existe déjà ; le patient suspendu reçoit déjà `403 ADHESION_SUSPENDUE` s'il tente de se réinscrire).
- [ ] **Un même médecin dans deux cliniques :** aujourd'hui, inviter un email qui a déjà un compte donne `409 EMAIL_DEJA_UTILISE`. Proposition : l'administrateur invite par email ; si le compte existe, la personne reçoit une **demande de rattachement** qu'elle accepte depuis son espace. La réponse à l'administrateur doit rester la même dans les deux cas, pour ne pas révéler quels emails ont un compte. `AffiliationService.affilier` et le choix de l'établissement à la connexion gèrent déjà plusieurs établissements.
- [ ] **Les responsables de chaque partie.** Voir [ARCHITECTURE.md](ARCHITECTURE.md), section 4.

**Avant une mise en production**

- [ ] Mettre `SWAGGER_ACTIF=false` et la vraie adresse du frontend dans `FRONTEND_URL`.
- [ ] Mettre la vraie adresse de l'API dans `frontend/src/environments/environment.ts`.
- [ ] Servir l'API et le frontend en HTTPS.
