import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { EmailsComponent } from './emails.component';
import { BASE_URL } from '../../../../utils/UTILS';

describe('EmailsComponent', () => {
  let fixture: ComponentFixture<EmailsComponent>;
  // Accès aux membres protégés du composant pour les tests
  let c: any;
  let http: HttpTestingController;

  /** Répond aux appels lancés à l'ouverture de la page (profil, nombre de destinataires, historique). */
  function flushInitialCalls(role = 'ROOT'): void {
    http.expectOne(`${BASE_URL}admin/me`).flush({ id: '1', name: 'Root', email: 'root@dh.cm', role, adminStatus: 'ACTIVE' });
    http.expectOne((r) => r.url === `${BASE_URL}emails/audience-count`).flush({ recipientCount: 42 });
    http.expectOne((r) => r.url === `${BASE_URL}emails` && r.method === 'GET')
      .flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true });
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailsComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(EmailsComponent);
    c = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('affiche le nombre de destinataires de l\'audience choisie', async () => {
    flushInitialCalls();
    await fixture.whenStable();
    expect(c.recipientCount()).toBe(42);
    expect(fixture.nativeElement.textContent).toContain('Ce message sera envoyé à 42 destinataires');
  });

  it('valide l\'objet, le message et les destinataires avant d\'ouvrir la confirmation', () => {
    flushInitialCalls();
    c.selectAudience('INDIVIDUAL');
    c.subject.set('ab');
    c.message.set('court');
    c.review();
    expect(c.confirmOpen()).toBe(false);
    expect(c.errorFor('subject')).toContain('au moins 3');
    expect(c.errorFor('message')).toContain('au moins 10');
    expect(c.errorFor('recipients')).toContain('au moins un destinataire');
  });

  it('ajoute des puces depuis le texte saisi, refuse les adresses invalides et les doublons', () => {
    flushInitialCalls();
    c.selectAudience('INDIVIDUAL');
    c.addFromText('Awa@Gmail.com, awa@gmail.com; bob@test.cm');
    expect(c.recipients().map((r: any) => r.email)).toEqual(['awa@gmail.com', 'bob@test.cm']);
    expect(c.recipientCount()).toBe(2);

    c.addFromText('pas-une-adresse');
    expect(c.chipError()).toContain('invalide');
    expect(c.recipients().length).toBe(2);

    c.removeRecipient('bob@test.cm');
    expect(c.recipients().length).toBe(1);
  });

  it('envoie après confirmation puis recharge l\'historique', () => {
    flushInitialCalls();
    c.selectAudience('INDIVIDUAL');
    c.addFromText('awa@gmail.com');
    c.subject.set('Rappel important');
    c.message.set('Bonjour, merci de passer à l\'agence.');
    c.review();
    expect(c.confirmOpen()).toBe(true);

    c.send();
    const req = http.expectOne((r) => r.url === `${BASE_URL}emails` && r.method === 'POST');
    expect(req.request.body).toEqual({
      audience: 'INDIVIDUAL', recipients: ['awa@gmail.com'], subject: 'Rappel important',
      message: 'Bonjour, merci de passer à l\'agence.',
    });
    req.flush({ id: 'e1', recipientCount: 1, message: 'Envoi accepté' }, { status: 202, statusText: 'Accepted' });

    expect(c.sendSuccess()).toContain('1 destinataire');
    expect(c.subject()).toBe('');
    http.expectOne((r) => r.url === `${BASE_URL}emails` && r.method === 'GET')
      .flush({ content: [], page: 0, size: 10, totalElements: 1, totalPages: 1, last: true });
  });

  it('affiche les erreurs par champ et le message précis renvoyés par l\'API', () => {
    flushInitialCalls();
    c.subject.set('Objet valide');
    c.message.set('Un message suffisamment long.');
    c.send();
    http.expectOne((r) => r.url === `${BASE_URL}emails` && r.method === 'POST').flush(
      { message: 'Requête invalide', fieldErrors: { subject: 'Objet déjà utilisé' } },
      { status: 400, statusText: 'Bad Request' },
    );
    expect(c.sendError()).toBe('Requête invalide');
    expect(c.errorFor('subject')).toBe('Objet déjà utilisé');
  });

  it('empêche un REVIEWER d\'envoyer', () => {
    flushInitialCalls('REVIEWER');
    expect(c.canSend()).toBe(false);
    c.subject.set('Objet valide');
    c.message.set('Un message suffisamment long.');
    c.review();
    expect(c.confirmOpen()).toBe(false);
  });
});
