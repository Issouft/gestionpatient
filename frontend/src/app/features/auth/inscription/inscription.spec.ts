import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { Inscription } from './inscription';

const API = environment.apiUrl;

describe('Inscription', () => {
  let fixture: ComponentFixture<Inscription>;
  let page: Inscription;
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [Inscription],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(Inscription);
    page = fixture.componentInstance;
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    fixture.componentRef.setInput('etablissement', '7');
    fixture.detectChanges();
    // L'annuaire public se charge à l'ouverture : la clinique de l'adresse (?etablissement=7) est présélectionnée.
    backend.expectOne(`${API}/annuaire/etablissements`).flush([
      { id: 7, nom: 'Clinique du Plateau', type: 'CLINIQUE', ville: 'Dakar', adresse: null, telephone: null, email: null },
    ]);
    await fixture.whenStable();
  });

  afterEach(() => backend.verify());

  function remplir(): void {
    page.formulaire.setValue({
      nom: ' Diop ',
      prenom: 'Awa',
      dateNaissance: '1995-04-12',
      sexe: 'FEMININ',
      telephone: '77 123 45 67',
      adresse: 'Dakar',
      email: 'awa@exemple.sn',
      motDePasse: 'secret123',
      confirmation: 'secret123',
      etablissementId: page.formulaire.controls.etablissementId.value,
    });
  }

  it('présélectionne la clinique de l\'adresse', () => {
    expect(page.formulaire.controls.etablissementId.value).toBe(7);
  });

  it('crée le compte avec la clinique, puis connecte le patient', () => {
    remplir();
    page.sInscrire();

    const inscription = backend.expectOne(`${API}/auth/inscription`);
    expect(inscription.request.body).toMatchObject({ nom: 'Diop', email: 'awa@exemple.sn', etablissementId: 7 });
    expect(inscription.request.body.confirmation).toBeUndefined(); // la confirmation reste dans le navigateur
    inscription.flush({ id: 1 });

    backend.expectOne(`${API}/auth/connexion`).flush({ jeton: 'j', etablissementId: null, etablissements: [] });
    backend.expectOne(`${API}/utilisateurs/moi`).flush({ id: 1, role: 'PATIENT', prenom: 'Awa', nom: 'Diop' });

    expect(router.navigateByUrl).toHaveBeenCalledWith('/accueil');
  });

  it('n\'envoie rien si le formulaire est incomplet', () => {
    page.sInscrire();
    backend.expectNone(`${API}/auth/inscription`);
    expect(page.formulaire.controls.nom.touched).toBe(true);
  });

  it('affiche un email déjà pris sous le champ email', () => {
    remplir();
    page.sInscrire();
    backend.expectOne(`${API}/auth/inscription`).flush(
      { code: 'EMAIL_DEJA_UTILISE', detail: 'Connectez-vous ou choisissez un autre email' },
      { status: 409, statusText: 'Conflict' },
    );

    expect(page.formulaire.controls.email.hasError('serveur')).toBe(true);
    expect(page.erreur()).toBeNull();
    expect(page.enCours()).toBe(false);
  });
});
