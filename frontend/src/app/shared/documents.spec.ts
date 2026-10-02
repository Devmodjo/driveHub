import {
  documentStatusLabel, formatFileSize, isMissingDocumentsError, missingDocumentsText, validateDocumentFile,
} from './documents';

describe('outils des justificatifs', () => {
  it('donne un libellé à chaque statut, « À fournir » sans document', () => {
    expect(documentStatusLabel(null)).toBe('À fournir');
    expect(documentStatusLabel('PENDING')).toBe('En vérification');
    expect(documentStatusLabel('VERIFIED')).toBe('Vérifié');
    expect(documentStatusLabel('REJECTED')).toBe('Refusé');
  });

  it('accepte PDF, JPEG, PNG et WebP de 5 Mo maximum', () => {
    expect(validateDocumentFile(new File(['x'], 'a.pdf', { type: 'application/pdf' }))).toBeNull();
    expect(validateDocumentFile(new File(['x'], 'a.webp', { type: 'image/webp' }))).toBeNull();
    expect(validateDocumentFile(new File(['x'], 'a.docx', { type: 'application/msword' }))).toContain('Format non accepté');
    expect(validateDocumentFile(new File([], 'vide.png', { type: 'image/png' }))).toContain('vide');
    const big = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'gros.jpg', { type: 'image/jpeg' });
    expect(validateDocumentFile(big)).toContain('trop lourd');
  });

  it('formate les tailles et la liste des pièces manquantes', () => {
    expect(formatFileSize(500)).toBe('500 o');
    expect(formatFileSize(2048)).toBe('2 Ko');
    expect(missingDocumentsText(['CNI'])).toBe("votre pièce d'identité");
    expect(missingDocumentsText(['CNI', 'CAPEC'])).toBe("votre pièce d'identité et votre CAPEC");
  });

  it('reconnaît le refus du backend pour justificatifs manquants', () => {
    expect(isMissingDocumentsError(400, "Ajoutez votre pièce d'identité et votre CAPEC avant d'envoyer votre demande")).toBe(true);
    expect(isMissingDocumentsError(400, 'Vous avez déjà une demande en cours')).toBe(false);
    expect(isMissingDocumentsError(500, 'CAPEC')).toBe(false);
  });
});
