# Architecture du projet gestionpatient

> **Statut : validé par le groupe et fusionné dans `main`.**

## 1. Pourquoi on change

Aujourd'hui, toutes les entités sont dans le dossier `entities` et tous les DTO dans le dossier `dto`. Cela pose deux problèmes :

- **On ne voit pas qui s'occupe de quoi.** Les classes de tous les sujets sont mélangées, et il est difficile de se répartir le travail.
- **La plupart des DTO contiennent des entités.** 11 DTO sur 17 sont concernés, alors qu'un DTO sert justement à ne pas exposer les entités.

Nous allons donc ranger le code **par domaine** et corriger le contenu des DTO.

Ce document explique seulement l'organisation du code. Pour installer et lancer le projet, lisez le [README](README.md).

Ce changement ne modifie ni les fonctionnalités ni les tables de la base de données. Le déplacement des fichiers change seulement leur emplacement et leurs `import`.

## 2. La nouvelle organisation

```text
com.forceN.gestionpatient
├── commun
│   ├── AbstractEntity
│   └── exception : CodeErreur, ApplicationException, GlobalExceptionHandler
│
├── securite                        ← Partie 1
│   ├── config     : SecurityConfig
│   ├── jwt        : JwtService, JwtFiltre
│   ├── controller : AuthController
│   ├── service    : AuthService, UtilisateurConnecteService
│   ├── dto        : ConnexionRequete, ConnexionReponse
│   └── exception  : SecuriteErreur, SecuriteErreurHandler
│
├── utilisateur                     ← Partie 1
│   ├── entities : Utilisateur, Patient, Medecin, Secretaire, Pharmacien, Administrateur
│   ├── dto      : UtilisateurDto, PatientDto, MedecinDto, SecretaireDto, PharmacienDto, AdministrateurDto
│   ├── enums    : Role, Sexe
│   ├── exception : UtilisateurErreur
│   ├── controller
│   ├── service
│   └── repository
│
├── rendezvous                      ← Partie 2
│   ├── entities : Planning, Creneau, RendezVous, Notification
│   ├── dto      : PlanningDto, CreneauDto, RendezVousDto, NotificationDto
│   ├── enums    : StatutRendezVous, TypeNotification
│   ├── exception : RendezVousErreur
│   ├── controller
│   ├── service
│   └── repository
│
└── medical                         ← Partie 3
    ├── entities : DossierPatient, Consultation, Ordonnance, LigneOrdonnance,
    │              Medicament, DemandeDisponibilite, Rapport
    ├── dto      : DossierPatientDto, ConsultationDto, OrdonnanceDto, LigneOrdonnanceDto,
    │              MedicamentDto, DemandeDisponibiliteDto, RapportDto
    ├── enums    : StatutDemande, TypeRapport
    ├── exception : MedicalErreur
    ├── controller
    ├── service
    └── repository
```

Le fichier `dto/Consultation.java` sera renommé `ConsultationDto.java`, pour suivre le même nom que les autres DTO.

Les dossiers `controller`, `service` et `repository` sont prêts mais encore vides. Le fichier `.gitkeep` qu'ils contiennent sert seulement à les conserver dans Git, car Git n'enregistre pas les dossiers vides. La façon de les remplir sera ajoutée à ce document plus tard.

## 3. Les notions à connaître

### Une entité

Une **entité** représente une table de la base de données. `Patient`, par exemple, correspond à la table `patient`, et hérite de `Utilisateur`, qui contient notamment le hash du mot de passe.

### Un DTO

Un **DTO** (*Data Transfer Object*) contient uniquement les données échangées avec l'extérieur de l'application. On choisit ainsi ce qu'on montre, au lieu d'envoyer toute la table.

Par exemple, un `PatientDto` peut contenir le nom et la date de naissance du patient, mais jamais son mot de passe.

### Pourquoi un DTO ne doit pas contenir d'entité

Voici ce que contient `RendezVousDto` aujourd'hui :

```java
private Patient patient;     // le patient complet, avec son email et son mot de passe haché
private Medecin medecin;     // le médecin complet
```

Pour afficher un rendez-vous, on transporte donc tout le patient et tout le médecin, alors qu'il suffit de savoir de qui il s'agit. Voici ce qu'on veut à la place :

```java
private Integer patientId;   // uniquement l'identifiant du patient
private Integer medecinId;   // uniquement l'identifiant du médecin
```

## 4. Qui fait quoi

| Partie | Domaine | Responsable |
|---|---|---|
| 1 | `utilisateur` et `securite` : les comptes, les profils et la connexion | MLG |
| 2 | `rendezvous` : les plannings, les créneaux, les rendez-vous et les notifications | *à décider* |
| 3 | `medical` : les dossiers, les consultations, les ordonnances, les médicaments et les rapports | *à décider* |

Le responsable d'une partie s'en occupe en priorité et connaît bien ses classes. Cela n'interdit pas aux autres de la lire ou de poser des questions.

## 5. Les règles communes

1. **Chacun travaille dans le dossier de son domaine.** Cela limite les conflits Git.
2. **Une nouvelle classe va dans le domaine qu'elle concerne.** En cas de doute, demandez au groupe avant de la créer.
3. **`commun` contient seulement ce qui sert à tous les domaines.** Ce n'est pas un dossier pour les classes qu'on ne sait pas où ranger.
4. **Un DTO ne contient jamais d'entité.** Il utilise un identifiant, comme `patientId`, ou un autre DTO.
5. **Un DTO n'a pas d'annotation JPA** (`@Entity`, `@Column`, `@PrePersist`, etc.).
6. **Un domaine peut utiliser les classes d'un autre.** `RendezVous` utilise `Patient`, par exemple, et c'est normal.
7. **Avant de modifier une classe d'un autre domaine ou un fichier partagé, prévenez son responsable.** Les fichiers partagés sont notamment `commun`, `pom.xml` et `application.properties`.

## 6. Comment gérer une erreur

On ne sait pas encore quelles erreurs le projet aura. On les ajoute donc au fur et à mesure, dans le domaine concerné. **Le dossier `exception` d'un domaine montre toutes ses erreurs.**

Le service lance l'exception, et `GlobalExceptionHandler` la transforme toute seule en réponse JSON. On n'écrit donc pas de `try/catch` dans les contrôleurs pour renvoyer une erreur.

### Ajouter une erreur

**1. Ajoutez une ligne dans l'enum de votre domaine, avant le point-virgule :**

```java
public enum RendezVousErreur implements CodeErreur {
    CRENEAU_DEJA_RESERVE(HttpStatus.CONFLICT, "Créneau déjà réservé")
    ;
    // ...
}
```

Si l'enum contient plusieurs erreurs, séparez-les par des virgules.

**2. Lancez-la depuis votre service :**

```java
throw new ApplicationException(RendezVousErreur.CRENEAU_DEJA_RESERVE, "Le créneau 5 est déjà réservé");
```

**Le client reçoit alors cette réponse, avec le code HTTP 409 :**

```json
{
  "status": 409,
  "title": "Créneau déjà réservé",
  "detail": "Le créneau 5 est déjà réservé",
  "code": "CRENEAU_DEJA_RESERVE",
  "instance": "/chemin/de/la/requete"
}
```

### Créer une classe d'exception

Une ligne dans l'enum suffit dans la plupart des cas. Créez une classe seulement si l'erreur a besoin de données ou d'un traitement particuliers. Placez-la dans le dossier `exception` de votre domaine et faites-la hériter de `ApplicationException` :

```java
public class StockInsuffisantException extends ApplicationException {

    public StockInsuffisantException(String medicament) {
        super(MedicalErreur.STOCK_INSUFFISANT, "Stock insuffisant pour " + medicament);
    }
}
```

### Les règles

- **Ne modifiez pas `GlobalExceptionHandler` pour ajouter une erreur.** Il prend déjà en charge tout ce qui hérite de `ApplicationException`.
- **Ne changez pas le nom d'une erreur déjà utilisée.** Le client peut s'appuyer sur la valeur de `code`. Le texte de `detail`, lui, peut changer.
- **Ne mettez pas de donnée médicale, de mot de passe ou de détail technique dans le message.** Le client le reçoit tel quel.
- **Les erreurs imprévues sont déjà gérées.** Le client reçoit un message générique avec le code HTTP 500, et le détail reste dans les logs.

## 7. La sécurité

### Comment ça marche

```text
1. Connexion : POST /auth/connexion avec l'email et le mot de passe → le serveur renvoie un jeton JWT
2. Requêtes  : le client envoie l'en-tête  Authorization: Bearer <jeton>
3. Contrôle  : le filtre JWT identifie l'utilisateur, puis ses droits sont vérifiés
```

Un **jeton JWT** est comme un badge signé par le serveur. Il est valable 60 minutes. Ensuite, il faut se reconnecter.

**Toutes les routes sont fermées par défaut.** Seules trois routes sont publiques, car la personne n'a pas encore de jeton de connexion :

| Route | Pourquoi elle est publique |
|---|---|
| `POST /auth/inscription` | Le patient n'a pas encore de compte |
| `POST /auth/connexion` | C'est elle qui donne le jeton |
| `POST /auth/activation` | Le personnel n'a pas encore de mot de passe. C'est le jeton reçu par email, valable 48 heures et une seule fois, qui prouve son identité |

Pour rendre une autre route publique, il faut l'ajouter dans `SecurityConfig`. Expliquez pourquoi dans la description de la PR, pour que le groupe puisse la valider avant la fusion.

### Les trois niveaux de protection

| Niveau | Question | Où | Réponse si refus |
|---|---|---|---|
| 1. Identité | Qui es-tu ? | `JwtFiltre`, déjà en place | 401 |
| 2. Rôle | As-tu le droit de faire cette action ? | Contrôleur, avec `@PreAuthorize` | 403 |
| 3. Propriété | Cette donnée te concerne-t-elle ? | Service, à écrire pour chaque donnée sensible | 403 |

Pour limiter une route à un rôle :

```java
@PreAuthorize("hasRole('MEDECIN')")
```

**Le niveau 3 est obligatoire pour les données médicales.** Le rôle seul ne suffit pas : un patient connecté pourrait lire le dossier d'un autre en changeant l'`id` dans l'URL. Le service doit donc vérifier que la donnée appartient bien à la personne connectée, ou qu'elle a le droit d'y accéder.
