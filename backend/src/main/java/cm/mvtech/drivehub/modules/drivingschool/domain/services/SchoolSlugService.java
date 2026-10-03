package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Adresse publique lisible d'une auto-école, utilisée dans l'URL de sa page et par Google :
 * « Auto-École Le Volant » à Douala devient {@code auto-ecole-le-volant-douala}.
 *
 * <p>L'adresse est créée avec la demande d'auto-école et ne change plus ensuite (un lien partagé
 * ou référencé par Google reste valable). Les auto-écoles créées avant cette fonctionnalité reçoivent
 * la leur au démarrage de l'application.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolSlugService {

    private static final int MAX_LENGTH = 100;

    private final DrivingSchoolRegistryRepository registryRepository;

    /** « Auto-École Le Volant », « Douala » → « auto-ecole-le-volant-douala » (sans accents ni caractères spéciaux). */
    public static String slugify(String text) {
        if (text == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String slug = noAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug;
    }

    /** Adresse unique : nom + ville (si la ville n'est pas déjà dans le nom), puis -2, -3... en cas de doublon. */
    public String uniqueSlug(String schoolName, String city) {
        String base = slugify(schoolName);
        String citySlug = slugify(city);
        if (!citySlug.isEmpty() && !base.contains(citySlug)) {
            base = slugify(base + "-" + citySlug);
        }
        if (base.isEmpty()) {
            base = "auto-ecole";
        }
        String slug = base;
        for (int i = 2; registryRepository.existsBySlug(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    /** Au démarrage : donne une adresse aux auto-écoles qui n'en ont pas encore. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        for (DrivingSchoolRegistry registry : registryRepository.findAllBySlugIsNull()) {
            registry.setSlug(uniqueSlug(registry.getSchoolName(), registry.getCity()));
            registryRepository.saveAndFlush(registry);
            log.info("Adresse publique créée : /auto-ecoles/{}", registry.getSlug());
        }
    }
}
