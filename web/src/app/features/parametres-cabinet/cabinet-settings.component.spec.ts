import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../../core/api/api.config';
import { CabinetSettingsComponent } from './cabinet-settings.component';

describe('CabinetSettingsComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CabinetSettingsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: API_BASE_URL, useValue: '/api/v1' }],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads settings and quota usage from the API', () => {
    const fixture = TestBed.createComponent(CabinetSettingsComponent);
    fixture.detectChanges();

    const settings = http.expectOne('/api/v1/cabinet/settings');
    expect(settings.request.method).toBe('GET');
    settings.flush({ cabinetId: 'cabinet-id', reminderDelayMinutes: 1440, defaultSlotDurationMinutes: 30, notificationTexts: {} });

    const quotas = http.expectOne('/api/v1/cabinet/quotas');
    expect(quotas.request.method).toBe('GET');
    quotas.flush({ subscriptionPlan: 'ESSENTIAL', activePractitioners: 1, maxPractitioners: 3, smsSentThisMonth: 4, maxSmsPerMonth: 500 });

    expect(fixture.componentInstance).toBeTruthy();
  });
});
