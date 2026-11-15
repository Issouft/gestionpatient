# Frontend GestionPatient (Angular)

L'interface web de l'application. Elle appelle l'API Spring Boot du dossier parent (port 8081).

**Technologies :** Angular 21 (composants *standalone*, *signals*, sans zone.js), Angular Material, Vitest pour les tests.

## Lancer le frontend

Il faut **Node.js 20 ou plus** (`node -v`). L'API doit être démarrée (voir le README principal, « Lancer le projet »).

```bash
cd frontend
npm ci          # la première fois, puis après chaque changement de package-lock.json
npm start       # ouvre l'application sur http://localhost:4200
```

`npm ci` installe exactement les versions de `package-lock.json`. Utilisez-le plutôt que `npm install`, qui peut changer les versions.

## Lancer les tests

```bash
npm test -- --watch=false
```

Les tests se trouvent à côté du code qu'ils vérifient (`*.spec.ts`). Ils n'appellent pas le vrai backend : `HttpTestingController` le remplace.

## Organisation

```text
src/app
├── core/                       ← ce qui sert à toute l'application, une seule fois
│   ├── api/                    ← les SEULS endroits qui connaissent les adresses du backend
│   │   ├── annuaire.service.ts      ← GET /annuaire/** (public)
│   │   ├── compte-public.service.ts ← inscription, activation, mot de passe oublié, réinitialisation
│   │   └── patient.service.ts       ← les cliniques du patient (rejoindre, mes cliniques)
│   ├── auth/
│   │   ├── session.service.ts  ← qui est connecté : jeton, profil, établissement de travail
│   │   ├── jeton.interceptor.ts← ajoute le jeton à chaque requête vers l'API ; un 401 ferme la session
│   │   └── gardes.ts           ← quelles pages peuvent s'ouvrir (connecté, rôle...)
│   ├── erreurs/
│   │   └── message-erreur.ts   ← transforme une erreur du backend (detail, code, erreurs) en message
│   ├── formulaires/
│   │   └── validateurs.ts      ← règles de saisie (mot de passe, confirmation...) et erreurs du backend sous les champs
│   └── modeles/                ← les types qui reprennent les DTO du backend
│       ├── compte.ts
│       └── etablissement.ts
├── shared/                     ← composants réutilisables par plusieurs écrans
│   ├── page-acces/             ← mise en page des pages d'accès : vitrine à gauche, contenu à droite
│   ├── bouton-visibilite/      ← bouton « œil » pour afficher un mot de passe
│   └── carte-etablissement/    ← un établissement en carte cliquable (annuaire, mes cliniques)
├── features/                   ← un dossier par domaine, comme le backend
│   ├── auth/                   ← connexion, inscription, activation, mot de passe oublié, choix de l'établissement
│   ├── annuaire/               ← annuaire public et fiche d'un établissement avec ses médecins
│   ├── patient/                ← mes cliniques
│   ├── accueil/
│   └── compte/                 ← mon profil
├── app.routes.ts               ← les pages de l'application
├── app.config.ts               ← intercepteur, routeur, langue française...
└── app.ts / app.html           ← la barre du haut, commune à tous les écrans
```

## Les règles

1. **Les types de `core/modeles` reprennent exactement les DTO du backend.** Si un DTO change en Java, changez le type ici.
2. **Un écran n'écrit jamais d'adresse du backend** : il appelle un service de `core/api`.
3. **Aucun écran ne lit ni n'écrit le jeton.** C'est le rôle de `SessionService` et de l'intercepteur.
4. **Les erreurs s'affichent avec `messageErreur(erreur)`** : le message vient du backend (`detail`), donc il est le même partout. Pour une erreur `DONNEES_INVALIDES`, `appliquerErreursServeur(formulaire, erreur)` affiche chaque message sous son champ.
5. **Cacher un bouton ne protège rien.** Les gardes et les menus sont du confort ; c'est le backend qui vérifie les droits.
6. **L'adresse de l'API est dans `src/environments/`**, jamais écrite en dur dans le code.
7. **Une page = un dossier dans `features/`**, chargée seulement quand on l'ouvre (`loadComponent` dans `app.routes.ts`).
8. **Les pages d'accès** (connexion, inscription...) ont `data: { pleinEcran: true }` dans `app.routes.ts` : elles s'affichent sans la barre du haut.
9. **Le style vient de `src/styles.scss`** : couleurs, polices, arrondis et classes communes (`.page`, `.carte-action`, `.formulaire-acces`, `.message`...). Un écran n'écrit pas de couleur en dur : il utilise les variables `--mat-sys-...` ou `--gp-...`.

## Les pages

| Adresse | Qui | Ce qu'elle fait |
|---|---|---|
| `/connexion` | Visiteur | Connexion. Le personnel à plusieurs établissements choisit ensuite le sien |
| `/inscription` | Visiteur | Inscription d'un patient, avec le choix facultatif d'une clinique (`?etablissement=7` la présélectionne), puis connexion automatique |
| `/mot-de-passe-oublie` | Visiteur | Envoie un lien valable 1 heure. Le message est le même que l'email ait un compte ou non |
| `/activation?jeton=...` | Lien de l'email | Le personnel invité choisit son mot de passe |
| `/reinitialisation?jeton=...` | Lien de l'email | Nouveau mot de passe après un oubli |
| `/annuaire` | Tout le monde | Chercher un établissement par ville et par type |
| `/annuaire/:id` | Tout le monde | La fiche d'un établissement et ses médecins (filtre par spécialité). Le visiteur peut y créer son compte, le patient peut rejoindre la clinique |
| `/mes-cliniques` | Patient | Les cliniques qu'il a rejointes |
| `/choix-etablissement` | Personnel | Choisir ou changer l'établissement de la session |
| `/accueil` | Connecté | Les écrans du rôle, sous forme de cartes |
| `/profil` | Connecté | Mon profil |

Les adresses `/activation` et `/reinitialisation` doivent rester celles des propriétés `application.url-activation` et `application.url-reinitialisation` du backend : ce sont elles qui sont dans les emails.

## La sécurité de la session

- Le jeton est gardé dans `sessionStorage` : il disparaît à la fermeture de l'onglet. Le mot de passe n'est jamais gardé.
- Le jeton n'est envoyé **qu'à l'API** (`environment.apiUrl`), jamais à un autre site.
- Si l'API répond `401` (jeton expiré, mot de passe changé, accès retiré), la session est fermée et la personne revient à la connexion avec le message « Votre session est terminée ».
- Un `403` ne ferme pas la session : la personne est connectée, mais n'a pas ce droit.
