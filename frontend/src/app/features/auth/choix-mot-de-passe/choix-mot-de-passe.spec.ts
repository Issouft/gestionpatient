import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { ChoixMotDePasse, ModeChoixMotDePasse } from './choix-mot-de-passe';

const API = environment.apiUrl;

describe('ChoixMotDePasse', () => {
  let fixture: ComponentFixture<ChoixMotDePasse>;
  let page: ChoixMotDePasse;
  let backend: HttpTestingController;

  function ouvrir(mode: ModeChoixMotDePasse, jeton?: string): void {
    TestBed.configureTestingModule({
      imports: [ChoixMotDePasse],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(ChoixMotDePasse);
    page = fixture.componentInstance;
    backend = TestBed.inject(HttpTestingController);
    fixture.componentRef.setInput('mode', mode);
    fixture.componentRef.setInput('jeton', jeton);
    fixture.detectChanges();
  }

  afterEach(() => backend.verify());

  it('explique qu\'un lien sans jeton est incomplet', () => {
    ouvrir('activation');
    expect(page.lienIncomplet()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Lien incomplet');
  });

  it('active le compte avec le jeton du lien', () => {
    ouvrir('activation', 'jeton-du-lien');
    page.formulaire.setValue({ motDePasse: 'secret123', confirmation: 'secret123' });
    page.valider();

    const requete = backend.expectOne(`${API}/auth/activation`);
    expect(requete.request.body).toEqual({ jeton: 'jeton-du-lien', motDePasse: 'secret123' });
    requete.flush(null, { status: 204, statusText: 'No Content' });

    expect(page.reussi()).toBe(true);
  });

  it('change le mot de passe en mode réinitialisation', () => {
    ouvrir('reinitialisation', 'jeton-oubli');
    page.formulaire.setValue({ motDePasse: 'nouveau123', confirmation: 'nouveau123' });
    page.valider();

    backend.expectOne(`${API}/auth/reinitialisation`).flush(null, { status: 204, statusText: 'No Content' });
    expect(page.reussi()).toBe(true);
  });

  it('explique quoi faire quand le lien a expiré', () => {
    ouvrir('reinitialisation', 'jeton-expire');
    page.formulaire.setValue({ motDePasse: 'nouveau123', confirmation: 'nouveau123' });
    page.valider();

    backend.expectOne(`${API}/auth/reinitialisation`).flush(
      { code: 'JETON_INVALIDE', detail: 'Lien invalide' },
      { status: 400, statusText: 'Bad Request' },
    );
    expect(page.reussi()).toBe(false);
    expect(page.erreur()).toContain('Refaites une demande');
  });

  it('n\'envoie rien si les deux mots de passe sont différents', () => {
    ouvrir('activation', 'jeton');
    page.formulaire.setValue({ motDePasse: 'secret123', confirmation: 'autre1234' });
    page.valider();
    backend.expectNone(`${API}/auth/activation`);
  });
});
