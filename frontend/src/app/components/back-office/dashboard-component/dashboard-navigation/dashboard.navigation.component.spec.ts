import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardNavigationComponent } from './dashboard.navigation.component';
import { BASE_URL } from '../../../../utils/UTILS';

describe('DashboardNavigationComponent', () => {
  let component: DashboardNavigationComponent;
  let fixture: ComponentFixture<DashboardNavigationComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardNavigationComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardNavigationComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  it('affiche le lien Emails et ouvre / ferme le tiroir mobile', () => {
    http.expectOne(`${BASE_URL}admin/me`).flush({ id: '1', name: 'Victor Modjo', role: 'ROOT' });
    expect(component.navLinks().map((l) => l.label)).toContain('Emails');
    expect(component.userInitials()).toBe('VM');
    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);
    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(false);
  });

  it('masque la gestion des admins aux comptes qui ne sont pas ROOT', () => {
    http.expectOne(`${BASE_URL}admin/me`).flush({ id: '2', name: 'Rev', role: 'REVIEWER' });
    expect(component.navLinks().map((l) => l.label)).not.toContain('Admins');
  });
});
