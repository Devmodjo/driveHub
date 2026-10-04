package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Adresses publiques des auto-écoles (URL de leur page, référencée par Google). */
class SchoolSlugServiceTest {

    private final DrivingSchoolRegistryRepository repository = mock(DrivingSchoolRegistryRepository.class);
    private final SchoolSlugService service = new SchoolSlugService(repository);

    @Test
    void slugify_RemovesAccentsAndSpecialCharacters() {
        assertEquals("auto-ecole-le-volant", SchoolSlugService.slugify("Auto-École  Le Volant !"));
        assertEquals("ecole-de-conduite-l-etoile", SchoolSlugService.slugify("École de conduite L'Étoile"));
        assertEquals("", SchoolSlugService.slugify(null));
        assertTrue(SchoolSlugService.slugify("a".repeat(300)).length() <= 100);
    }

    @Test
    void uniqueSlug_AddsCityOnce_AndNumbersDuplicates() {
        when(repository.existsBySlug(anyString())).thenReturn(false);
        assertEquals("auto-ecole-le-volant-douala", service.uniqueSlug("Auto-École Le Volant", "Douala"));
        assertEquals("auto-ecole-de-douala", service.uniqueSlug("Auto-École de Douala", "Douala"), "ville déjà dans le nom");

        when(repository.existsBySlug("auto-ecole-le-volant-douala")).thenReturn(true);
        when(repository.existsBySlug("auto-ecole-le-volant-douala-2")).thenReturn(true);
        assertEquals("auto-ecole-le-volant-douala-3", service.uniqueSlug("Auto-École Le Volant", "Douala"));
    }

    @Test
    void backfill_GivesAnAddressToExistingSchools() {
        DrivingSchoolRegistry registry = new DrivingSchoolRegistry();
        registry.setSchoolName("Auto-École La Prudence");
        registry.setCity("Yaoundé");
        when(repository.findAllBySlugIsNull()).thenReturn(List.of(registry));

        service.backfill();

        assertEquals("auto-ecole-la-prudence-yaounde", registry.getSlug());
        verify(repository).saveAndFlush(registry);
    }
}
