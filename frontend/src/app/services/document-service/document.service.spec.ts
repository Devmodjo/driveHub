import { TestBed } from '@angular/core/testing';
import { HttpEventType, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DocumentService } from './document.service';
import { UserDocument } from '../../interfaces/drivehub.models';
import { API_URL, BASE_URL } from '../../utils/UTILS';

/** Document de test, conforme à DocumentResponse (docs/API-JUSTIFICATIFS.md). */
const DOC: UserDocument = {
  id: 'd1', type: 'CNI', status: 'PENDING', documentNumberMasked: '••••4521', fileName: 'cni.jpg',
  contentType: 'image/jpeg', sizeBytes: 1000, uploadedAt: '2026-10-02T10:15:00', reviewComment: null,
};

describe('DocumentService', () => {
  let service: DocumentService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(DocumentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lit mes justificatifs et les pièces manquantes', () => {
    service.myDocuments().subscribe((docs) => expect(docs).toEqual([DOC]));
    http.expectOne(`${API_URL}documents/me`).flush([DOC]);

    service.requirements().subscribe((req) => expect(req.missing).toEqual(['CAPEC']));
    http.expectOne(`${API_URL}documents/requirements`).flush({ required: ['CNI', 'CAPEC'], missing: ['CAPEC'] });
  });

  it('envoie le fichier en multipart avec le type et le numéro (sans espaces), en suivant la progression', () => {
    const file = new File(['abc'], 'cni.jpg', { type: 'image/jpeg' });
    const events: HttpEventType[] = [];
    service.upload('CNI', file, '  123 ').subscribe((event) => events.push(event.type));

    const req = http.expectOne(`${API_URL}documents`);
    expect(req.request.method).toBe('POST');
    expect(req.request.reportProgress).toBe(true);
    const body = req.request.body as FormData;
    expect(body.get('type')).toBe('CNI');
    expect((body.get('file') as File).name).toBe('cni.jpg');
    expect(body.get('documentNumber')).toBe('123');
    req.flush(DOC, { status: 201, statusText: 'Created' });
    expect(events).toContain(HttpEventType.Response);
  });

  it("n'envoie pas de numéro vide", () => {
    service.upload('CAPEC', new File(['x'], 'capec.pdf', { type: 'application/pdf' }), '   ').subscribe();
    const req = http.expectOne(`${API_URL}documents`);
    expect((req.request.body as FormData).has('documentNumber')).toBe(false);
    req.flush(DOC);
  });

  it('télécharge les fichiers en Blob (jamais par une adresse brute)', () => {
    service.myFile('d1').subscribe((blob) => expect(blob).toBeInstanceOf(Blob));
    const mine = http.expectOne(`${API_URL}documents/d1/file`);
    expect(mine.request.responseType).toBe('blob');
    mine.flush(new Blob(['x'], { type: 'image/jpeg' }));

    service.joinRequestFile('r1', 'd1').subscribe();
    expect(http.expectOne(`${API_URL}join-school/admin/r1/documents/d1/file`).request.responseType).toBe('blob');

    service.studentFile('s1', 'd1').subscribe();
    expect(http.expectOne(`${API_URL}students/s1/documents/d1/file`).request.responseType).toBe('blob');

    service.platformFile('d1').subscribe();
    expect(http.expectOne(`${BASE_URL}documents/d1/file`).request.responseType).toBe('blob');
  });

  it('appelle les routes du responsable et du back-office', () => {
    service.joinRequestDocuments('r1').subscribe();
    http.expectOne(`${API_URL}join-school/admin/r1/documents`).flush([]);

    service.studentDocuments('s1').subscribe();
    http.expectOne(`${API_URL}students/s1/documents`).flush([]);

    service.registryDocuments('reg1').subscribe();
    http.expectOne(`${BASE_URL}registries/reg1/documents`).flush([]);

    service.deleteMine('d1').subscribe();
    expect(http.expectOne(`${API_URL}documents/d1`).request.method).toBe('DELETE');
  });

  it('envoie la décision du back-office avec le commentaire', () => {
    service.review('d1', { status: 'REJECTED', comment: 'Photo floue' }).subscribe();
    const req = http.expectOne(`${BASE_URL}documents/d1/review`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'REJECTED', comment: 'Photo floue' });
    req.flush({ ...DOC, status: 'REJECTED', reviewComment: 'Photo floue' });
  });
});
