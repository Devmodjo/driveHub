import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PlatformEmailService } from './platform-email.service';
import { BASE_URL } from '../../utils/UTILS';

describe('PlatformEmailService', () => {
  let service: PlatformEmailService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PlatformEmailService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envoie le message en POST sur /emails', () => {
    const body = { audience: 'INDIVIDUAL' as const, recipients: ['a@b.cm'], subject: 'Objet', message: 'Bonjour à tous' };
    service.send(body).subscribe((res) => expect(res.recipientCount).toBe(1));
    const req = http.expectOne(`${BASE_URL}emails`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ id: 'e1', recipientCount: 1, message: 'Envoi accepté' }, { status: 202, statusText: 'Accepted' });
  });

  it('demande le nombre de destinataires avec schoolId seulement s\'il est fourni', () => {
    service.audienceCount('ALL_STUDENTS').subscribe();
    const req1 = http.expectOne((r) => r.url === `${BASE_URL}emails/audience-count`);
    expect(req1.request.params.get('audience')).toBe('ALL_STUDENTS');
    expect(req1.request.params.has('schoolId')).toBe(false);
    req1.flush({ recipientCount: 10 });

    service.audienceCount('SCHOOL_MEMBERS', 'reg-1').subscribe();
    const req2 = http.expectOne((r) => r.url === `${BASE_URL}emails/audience-count`);
    expect(req2.request.params.get('schoolId')).toBe('reg-1');
    req2.flush({ recipientCount: 3 });
  });

  it('cherche des destinataires et lit l\'historique paginé', () => {
    service.searchRecipients('pau').subscribe();
    const search = http.expectOne((r) => r.url === `${BASE_URL}emails/recipients`);
    expect(search.request.params.get('q')).toBe('pau');
    search.flush([]);

    service.history(2, 10).subscribe();
    const hist = http.expectOne((r) => r.url === `${BASE_URL}emails` && r.method === 'GET');
    expect(hist.request.params.get('page')).toBe('2');
    expect(hist.request.params.get('size')).toBe('10');
    hist.flush({ content: [], page: 2, size: 10, totalElements: 0, totalPages: 0, last: true });
  });
});
