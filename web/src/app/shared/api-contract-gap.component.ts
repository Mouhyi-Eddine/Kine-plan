import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-api-contract-gap',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<section class="page" aria-labelledby="title"><p class="eyebrow">CONTRAT API</p><h1 id="title">{{ title() }}</h1><p class="message">Cette surface est préparée côté interface, mais l'API actuelle ne publie pas encore d'endpoint pour ce module. Aucun appel fictif n'est effectué.</p><p class="detail">Le backend doit d'abord exposer le contrat REST et ses DTO pour permettre une implémentation complète et sûre.</p></section>`,
  styles: `.page{max-width:760px;margin:auto;padding-top:12vh}.eyebrow{color:#0b7375;font-size:.72rem;font-weight:800;letter-spacing:.16em;margin:0 0 14px}h1{color:#173b3d;font-size:clamp(2.4rem,5vw,4.2rem);line-height:.95;margin:0 0 20px}.message{font-size:1.15rem;line-height:1.6;color:#426366}.detail{color:#71898a;line-height:1.6}`,
})
export class ApiContractGapComponent { readonly title = input('Module indisponible'); }
