import { TestBed } from '@angular/core/testing';
import { SpamHintComponent } from './spam-hint.component';

describe('SpamHintComponent', () => {
  function render(audience?: 'self' | 'other'): string {
    const fixture = TestBed.createComponent(SpamHintComponent);
    if (audience) fixture.componentRef.setInput('audience', audience);
    fixture.detectChanges();
    return fixture.nativeElement.textContent;
  }

  it("conseille à l'utilisateur de regarder dans ses spams (par défaut)", () => {
    const text = render();
    expect(text).toContain('courriers indésirables');
    expect(text).toContain("ajoutez l'expéditeur à vos contacts");
  });

  it("parle de la personne invitée quand l'email part vers quelqu'un d'autre", () => {
    expect(render('other')).toContain('Demandez-lui de vérifier aussi ses courriers indésirables');
  });
});
