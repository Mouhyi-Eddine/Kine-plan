import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../core/api/api.config';
import { OnboardingComponent } from './onboarding.component';

describe('OnboardingComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OnboardingComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'auth/login', redirectTo: '', pathMatch: 'full' }]),
        { provide: API_BASE_URL, useValue: '/api/v1' },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('submits the real onboarding contract', () => {
    const fixture = TestBed.createComponent(OnboardingComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.setValue({
      cabinetName: 'Cabinet Test', address: '', siret: '', timezone: 'Europe/Paris',
      adminEmail: 'admin@example.com', adminPassword: 'a-strong-password',
      adminFirstName: 'Ada', adminLastName: 'Lovelace',
    });

    component.submit();

    const request = http.expectOne('/api/v1/onboarding');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.adminEmail).toBe('admin@example.com');
    request.flush({ cabinetId: 'cabinet-id', membershipId: 'membership-id' });
  });
});
