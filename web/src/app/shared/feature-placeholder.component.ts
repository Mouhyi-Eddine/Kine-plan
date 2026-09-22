import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatIconModule],
  selector: 'app-feature-placeholder',
  template: `
    <section class="feature-page" aria-labelledby="feature-title">
      <p class="eyebrow">KINE-PLAN / MODULE</p>
      <h1 id="feature-title">{{ title }}</h1>
      <mat-card appearance="outlined">
        <mat-card-content>
          <mat-icon aria-hidden="true">construction</mat-icon>
          <div>
            <h2>Module prêt à être branché</h2>
            <p>La structure de navigation est en place. Les données seront reliées au contrat OpenAPI de l’API.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </section>
  `,
  styles: `
    .feature-page { max-width: 980px; margin: 0 auto; }
    .eyebrow { color: #0b7375; font-size: .75rem; font-weight: 800; letter-spacing: .16em; }
    h1 { margin: 0 0 28px; color: #173b3d; font-size: clamp(2rem, 5vw, 3.4rem); line-height: 1.05; }
    mat-card { border-color: #c9dcda; background: rgba(255,255,255,.78); }
    mat-card-content { display: flex; align-items: center; gap: 18px; padding: 28px; }
    mat-icon { color: #e67e22; }
    h2 { margin: 0 0 6px; font-size: 1.1rem; }
    p { color: #587174; }
  `,
})
export class FeaturePlaceholderComponent {
  private readonly route = inject(ActivatedRoute);
  protected readonly title = this.route.snapshot.data['title'] as string;
}
