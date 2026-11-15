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
│   ├── auth/
│   │   ├── session.service.ts  ← qui est connecté : jeton, profil, établissement de travail
│   │   ├── jeton.interceptor.ts← ajoute le jeton à chaque requête vers l'API ; un 401 ferme la session
│   │   └── gardes.ts           ← quelles pages peuvent s'ouvrir (connecté, rôle...)
│   ├── erreurs/
│   │   └── message-erreur.ts   ← transforme une erreur du backend (detail, code, erreurs) en message
│   └── modeles/
│       └── compte.ts           ← les types qui reprennent les DTO du backend
├── features/                   ← un dossier par domaine, comme le backend
│   ├── auth/                   ← connexion, choix de l'établissement
│   ├── accueil/
│   └── compte/                 ← mon profil
├── app.routes.ts               ← les pages de l'application
├── app.config.ts               ← intercepteur, routeur, langue française...
└── app.ts / app.html           ← la barre du haut, commune à tous les écrans
```

## Les règles

1. **Les types de `core/modeles` reprennent exactement les DTO du backend.** Si un DTO change en Java, changez le type ici.
2. **Aucun écran ne lit ni n'écrit le jeton.** C'est le rôle de `SessionService` et de l'intercepteur.
3. **Les erreurs s'affichent avec `messageErreur(erreur)`** : le message vient du backend (`detail`), donc il est le même partout.
4. **Cacher un bouton ne protège rien.** Les gardes et les menus sont du confort ; c'est le backend qui vérifie les droits.
5. **L'adresse de l'API est dans `src/environments/`**, jamais écrite en dur dans le code.
6. **Une page = un dossier dans `features/`**, chargée seulement quand on l'ouvre (`loadComponent` dans `app.routes.ts`).

## La sécurité de la session

- Le jeton est gardé dans `sessionStorage` : il disparaît à la fermeture de l'onglet. Le mot de passe n'est jamais gardé.
- Le jeton n'est envoyé **qu'à l'API** (`environment.apiUrl`), jamais à un autre site.
- Si l'API répond `401` (jeton expiré, mot de passe changé, accès retiré), la session est fermée et la personne revient à la connexion avec le message « Votre session est terminée ».
- Un `403` ne ferme pas la session : la personne est connectée, mais n'a pas ce droit.
