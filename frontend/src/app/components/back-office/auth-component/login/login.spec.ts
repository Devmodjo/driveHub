import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginComponent as Login } from './login.component';
import { BASE_URL } from '../../../../utils/UTILS';

describe('Login (back-office)', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  it('n\'appelle pas l\'API si le formulaire est incomplet', () => {
    component.onSubmit();
    http.expectNone(`${BASE_URL}admin/login`);
    expect(component.loginForm.controls.email.touched).toBe(true);
  });

  it('affiche le message précis du backend et les erreurs par champ', () => {
    component.loginForm.setValue({ email: 'root@drivehub.cm', password: 'mauvais' });
    component.onSubmit();
    http.expectOne(`${BASE_URL}admin/login`).flush(
      { message: 'Compte suspendu', fieldErrors: { password: 'Mot de passe expiré' } },
      { status: 403, statusText: 'Forbidden' },
    );
    expect(component.errorMessage()).toBe('Compte suspendu');
    expect(component.serverErrors()['password']).toBe('Mot de passe expiré');
    expect(component.isLoading()).toBe(false);
  });

  it('utilise un message selon le code HTTP si le backend n\'en fournit pas', () => {
    component.loginForm.setValue({ email: 'root@drivehub.cm', password: 'x' });
    component.onSubmit();
    http.expectOne(`${BASE_URL}admin/login`).flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(component.errorMessage()).toBe('Email ou mot de passe incorrect.');
  });
});
