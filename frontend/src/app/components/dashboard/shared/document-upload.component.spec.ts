import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DocumentUploadComponent } from './document-upload.component';
import { UserDocument } from '../../../interfaces/drivehub.models';
import { API_URL } from '../../../utils/UTILS';

const DOC: UserDocument = {
  id: 'd1', type: 'CNI', status: 'PENDING', documentNumberMasked: null, fileName: 'cni.jpg',
  contentType: 'image/jpeg', sizeBytes: 2048, uploadedAt: '2026-10-02T10:15:00', reviewComment: null,
};

/** Fichier de test de la taille voulue (le contenu n'a pas d'importance). */
function fileOf(name: string, type: string, size = 10): File {
  return new File([new Uint8Array(size)], name, { type });
}

describe('DocumentUploadComponent', () => {
  let fixture: ComponentFixture<DocumentUploadComponent>;
  // Accès aux membres protégés du composant pour les tests
  let c: any;
  let http: HttpTestingController;
  let emitted: (UserDocument | null)[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentUploadComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DocumentUploadComponent);
    fixture.componentRef.setInput('type', 'CNI');
    c = fixture.componentInstance;
    emitted = [];
    c.changed.subscribe((d: UserDocument | null) => emitted.push(d));
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function text(): string {
    fixture.detectChanges();
    return fixture.nativeElement.textContent;
  }

  it('affiche « À fournir » et le libellé complet quand aucun document n\'est envoyé', () => {
    expect(text()).toContain("Pièce d'identité (CNI ou passeport)");
    expect(text()).toContain('À fournir');
    expect(text()).toContain('Choisir un fichier');
  });

  it('refuse un format non accepté sans appeler le backend', () => {
    c.send(fileOf('photo.gif', 'image/gif'));
    expect(c.error()).toContain('Format non accepté');
    http.expectNone(`${API_URL}documents`);
  });

  it('refuse un fichier de plus de 5 Mo sans appeler le backend', () => {
    c.send(fileOf('scan.pdf', 'application/pdf', 5 * 1024 * 1024 + 1));
    expect(c.error()).toContain('5 Mo');
    http.expectNone(`${API_URL}documents`);
  });

  it('envoie un fichier valide avec le numéro, puis prévient le parent', () => {
    c.number.set('AB123');
    c.send(fileOf('cni.jpg', 'image/jpeg'));
    expect(c.uploading()).toBe(true);
    const req = http.expectOne(`${API_URL}documents`);
    expect((req.request.body as FormData).get('documentNumber')).toBe('AB123');
    req.flush(DOC, { status: 201, statusText: 'Created' });

    expect(c.uploading()).toBe(false);
    expect(emitted).toEqual([DOC]);
    expect(text()).toContain('En vérification');
    expect(text()).toContain('Remplacer');
  });

  it("affiche le message précis du backend en cas d'erreur", () => {
    c.send(fileOf('cni.png', 'image/png'));
    http.expectOne(`${API_URL}documents`).flush(
      { status: 400, message: 'Le contenu du fichier ne correspond pas à une image' },
      { status: 400, statusText: 'Bad Request' },
    );
    expect(c.error()).toBe('Le contenu du fichier ne correspond pas à une image');
    expect(emitted).toEqual([]);
  });

  it('affiche le motif du refus et permet de supprimer le document', () => {
    fixture.componentRef.setInput('document', { ...DOC, status: 'REJECTED', reviewComment: 'Photo illisible' });
    expect(text()).toContain('Refusé');
    expect(text()).toContain('Photo illisible');

    c.remove();
    const req = http.expectOne(`${API_URL}documents/d1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(emitted).toEqual([null]);
    expect(text()).toContain('À fournir');
  });

  it('un document vérifié ne peut plus être remplacé ni supprimé', () => {
    fixture.componentRef.setInput('document', { ...DOC, status: 'VERIFIED' });
    const content = text();
    expect(content).toContain('Vérifié');
    expect(content).not.toContain('Remplacer');
    expect(content).not.toContain('Supprimer');
    expect(content).toContain('Voir');
  });
});
