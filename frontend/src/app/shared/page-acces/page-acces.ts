import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Mise en page commune aux pages d'accès (connexion, inscription, activation, mot de passe oublié...) :
 * la vitrine de la plateforme à gauche, le titre et le contenu de la page à droite.
 * Chaque page ne fournit que son contenu :
 *   <app-page-acces titre="Connexion" intro="..."> le formulaire </app-page-acces>
 */
@Component({
  selector: 'app-page-acces',
  imports: [MatIconModule],
  templateUrl: './page-acces.html',
})
export class PageAcces {
  readonly titre = input.required<string>();
  readonly intro = input<string>();
  /** Zone de contenu plus large, pour un long formulaire (inscription). */
  readonly large = input(false);
}
