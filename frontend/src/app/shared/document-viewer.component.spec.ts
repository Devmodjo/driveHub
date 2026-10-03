import { TestBed } from '@angular/core/testing';
import { NEVER } from 'rxjs';
import { DocumentViewerComponent } from './document-viewer.component';

describe('DocumentViewerComponent', () => {
  it("Échap ferme la visionneuse sans fermer la fenêtre qui l'a ouverte", () => {
    const fixture = TestBed.createComponent(DocumentViewerComponent);
    fixture.componentRef.setInput('title', "Pièce d'identité");
    fixture.componentRef.setInput('source', NEVER);
    fixture.detectChanges();

    let viewerClosed = 0;
    fixture.componentInstance.closed.subscribe(() => viewerClosed++);

    // Fenêtre parente (dialog, panneau) qui écoute Échap sur « document », comme (document:keydown.escape)
    let parentClosed = 0;
    const parentListener = (e: KeyboardEvent) => { if (e.key === 'Escape') parentClosed++; };
    document.addEventListener('keydown', parentListener);

    document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(viewerClosed).toBe(1);
    expect(parentClosed).toBe(0);

    // Une fois la visionneuse fermée (détruite), Échap revient à la fenêtre parente
    fixture.destroy();
    document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(viewerClosed).toBe(1);
    expect(parentClosed).toBe(1);

    document.removeEventListener('keydown', parentListener);
  });
});
