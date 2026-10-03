import { TestBed } from '@angular/core/testing';
import { SeoService } from './seo.service';

describe('SeoService', () => {
  let seo: SeoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    seo = TestBed.inject(SeoService);
  });

  it('pose le titre, la description, l\'aperçu de partage et l\'adresse canonique', () => {
    seo.setPage({ title: 'Auto-École Le Volant | DriveHub', description: 'Auto-école à Douala.', path: '/auto-ecoles/le-volant' });
    expect(document.title).toBe('Auto-École Le Volant | DriveHub');
    expect(document.querySelector('meta[name="description"]')?.getAttribute('content')).toBe('Auto-école à Douala.');
    expect(document.querySelector('meta[property="og:title"]')?.getAttribute('content')).toBe('Auto-École Le Volant | DriveHub');
    expect(document.querySelector('link[rel="canonical"]')?.getAttribute('href')).toContain('/auto-ecoles/le-volant');
  });

  it('raccourcit une description trop longue pour Google', () => {
    seo.setPage({ title: 't', description: 'a'.repeat(300), path: '/' });
    expect(document.querySelector('meta[name="description"]')!.getAttribute('content')!.length).toBeLessThanOrEqual(160);
  });

  it('ajoute puis retire les données structurées schema.org', () => {
    seo.setStructuredData({ '@type': 'DrivingSchool', name: 'Le Volant' });
    const script = document.getElementById('drivehub-structured-data');
    expect(JSON.parse(script!.textContent!).name).toBe('Le Volant');
    seo.setStructuredData(null);
    expect(document.getElementById('drivehub-structured-data')).toBeNull();
  });
});
