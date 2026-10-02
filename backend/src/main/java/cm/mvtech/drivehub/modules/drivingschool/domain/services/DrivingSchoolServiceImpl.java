package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.*;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.core.domain.service.TenantProvisioningService;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import org.springframework.transaction.annotation.Transactional;
import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.core.infrastructure.TenantSchemas;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingSchoolServiceImpl implements DrivingSchoolService {

    private final DrivingSchoolRepository drivingSchoolRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository drivingSchoolRegistryRepository;
    private final DrivingSchoolMapper mapper;
    private final TenantExecutor tenantExecutor;
    private final MonitorsRepository monitorsRepository;
    private final EmailService emailService;

    /**
     * Soumet une demande de création d'auto-école au nom de l'utilisateur authentifié. La méthode
     * vérifie que l'utilisateur existe, que son email est vérifié et qu'il possède le rôle MONITOR
     * avant de construire et persister un {@link DrivingSchoolRegistry} avec le statut {@code PENDING}.
     * Elle lance respectivement une {@link UsernameNotFoundException}, une {@link IllegalAccessException}
     * ou une {@link AccessDeniedException} si l'une de ces conditions n'est pas satisfaite.
     */
    @Transactional
    @Override
    public void createSchool(DrivingSchoolRequestDto req, UserPrincipal userPrincipal) throws IllegalAccessException {

        Optional<User> admin = userRepository.findById(userPrincipal.getId());

        if (admin.isEmpty()) {
            throw new UsernameNotFoundException("cet utilisateur n'existe pas !");
        }

        if (admin.get().getRoles() != Role.MONITOR) {
            throw new AccessDeniedException("seul les Encadreur peuvent cree des auto écoles");
        }

        if (admin.get().getProfileStatus() == ProfileStatus.EMAIL_VERIFIED) {
            // Contraintes UNIQUE de driving_school_registry : on les vérifie avant d'écrire
            // pour renvoyer une 409 claire plutôt qu'une erreur SQL (500).
            if (drivingSchoolRegistryRepository.findByAdmin(admin.get()).isPresent()) {
                throw new ConflictException("Vous avez déjà soumis une demande d'auto-école");
            }
            if (drivingSchoolRegistryRepository.existsBySchoolNameIgnoreCase(req.name().trim())) {
                throw new ConflictException("Une auto-école porte déjà ce nom");
            }
            DrivingSchoolRegistry dr = getDrivingSchoolRegistry(req, admin);
            drivingSchoolRegistryRepository.save(dr);

            // Accusé de réception : la demande sera examinée sous 48 à 72 heures
            emailService.sendSchoolRequestReceivedEmail(admin.get().getEmail(), admin.get().getFirstname(), dr.getSchoolName());
        } else {
            throw new IllegalAccessException(
                    "Vérifiez d'abord votre adresse email : cliquez sur le lien reçu après votre inscription");
        }
    }

    private static DrivingSchoolRegistry getDrivingSchoolRegistry(DrivingSchoolRequestDto req, Optional<User> admin) {
        // Nom de schéma sûr et unique (ex : "ae_auto_ecole_le_volant_3f9a1c").
        // L'ancien calcul pouvait commencer par un chiffre, contenir des accents remplacés par "_"
        // et produire le même schéma pour "Le Volant" et "Le-Volant".
        String schemaName = TenantSchemas.fromSchoolName(req.name());

        DrivingSchoolRegistry dr = new DrivingSchoolRegistry();
        dr.setSchoolName(req.name());
        dr.setSchemaName(schemaName);
        dr.setAdmin(admin.get());
        dr.setAddress(req.address());
        dr.setPhoneNumber(req.phoneNumber());
        dr.setDescription(req.description());
        dr.setEmail(req.email());
        dr.setCountry(req.country());
        dr.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        dr.setCity(req.city());
        dr.setWebsiteUrl(req.websiteUrl());
        dr.setWhatsappNumber(req.whatsappNumber());
        return dr;
    }

    /**
     * Retourne la liste de toutes les auto-écoles actives enregistrées dans le système, en mappant
     * chaque entité {@link DrivingSchool} vers un {@link DrivingSchoolResponseDto} contenant les
     * informations publiques de l'établissement.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DrivingSchoolResponseDto> retreiveSchool() {
        // La table driving_school vit dans le schéma de CHAQUE auto-école : le catalogue public
        // est donc construit à partir du registre (schéma public), filtré sur les écoles validées.
        return drivingSchoolRegistryRepository
                .findAllByDrivingSchoolStatusIn(EnumSet.of(DrivingSchoolStatus.APPROVED, DrivingSchoolStatus.ACTIVE))
                .stream()
                .map(e -> new DrivingSchoolResponseDto(
                        e.getId(),
                        e.getSchoolName(),
                        e.getPhoneNumber(),
                        e.getAddress(),
                        e.getEmail(),
                        e.getCountry(),
                        e.getCity(),
                        e.getCreatedAt(),
                        e.getWhatsappNumber(),
                        e.getWebsiteUrl(),
                        e.getDrivingSchoolStatus()))
                .toList();
    }

    /**
     * Approuve une demande d'enregistrement identifiée par son {@code registryId}. La méthode
     * provisionne le schéma de base de données du tenant, active le compte du moniteur associé,
     * bascule le contexte sur le bon tenant pour y persister l'entité {@link DrivingSchool}, puis
     * met à jour le statut du registre à {@code APPROVED}. Le contexte tenant est systématiquement
     * nettoyé dans le bloc {@code finally}, même en cas d'erreur. Une {@link IllegalArgumentException}
     * est levée si le registre est introuvable, et une {@link AccessDeniedException} si l'administrateur
     * du registre n'est pas un MONITOR.
     */
    @Override
    @Transactional
    public void approveRegistry(UUID registryId) {
        DrivingSchoolRegistry schoolRegistry = drivingSchoolRegistryRepository.findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException("ce auto-ecole n'existe pas dans les registres"));

        if (schoolRegistry.getDrivingSchoolStatus() != DrivingSchoolStatus.PENDING) {
            throw new IllegalStateException("Seule une demande en attente peut être approuvée");
        }

        User monitor = schoolRegistry.getAdmin();

        if (monitor.getRoles() != Role.MONITOR) {
            throw new AccessDeniedException("cet utilisateur n'est pas un Moniteur");
        }

        String schemaName = schoolRegistry.getSchemaName();

        // 1. Schéma + tables métier (Flyway) + enregistrement dans public.tenants
        tenantProvisioningService.createTenantSchema(schemaName);

        // 2. Dans le schéma de l'auto-école : la fiche auto-école et le moniteur fondateur.
        //    TenantExecutor ouvre une NOUVELLE transaction sur le schéma du tenant :
        //    changer TenantContext au milieu de cette méthode n'aurait aucun effet
        //    (la session Hibernate courante est déjà ouverte sur "public").
        Monitor publicProfile = monitor.getMonitors().stream().findFirst().orElse(null);
        tenantExecutor.runInTenant(schemaName, () -> {
            DrivingSchool ds = drivingSchoolRepository.save(getDrivingSchool(schoolRegistry, monitor));
            if (publicProfile != null) {
                monitorsRepository.save(copyMonitorProfile(publicProfile, monitor, ds));
            }
        });

        // 3. Schéma public : activation du moniteur et du registre.
        monitor.setProfileStatus(ProfileStatus.ACTIVE);
        schoolRegistry.setDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        drivingSchoolRegistryRepository.save(schoolRegistry);

        // 4. Prévenir le moniteur (envoi asynchrone : n'annule rien en cas d'échec)
        emailService.sendSchoolApprovedEmail(monitor.getEmail(), monitor.getFirstname(), schoolRegistry.getSchoolName());
    }

    /** Copie du profil moniteur (schéma public) vers le schéma de l'auto-école. */
    private static Monitor copyMonitorProfile(Monitor source, User user, DrivingSchool school) {
        Monitor copy = new Monitor();
        copy.setUser(user);
        copy.setDrivingSchool(school);
        copy.setPhoneNumber(source.getPhoneNumber());
        copy.setDateOfBirth(source.getDateOfBirth());
        copy.setGender(source.getGender());
        copy.setNationality(source.getNationality());
        copy.setResidenceCity(source.getResidenceCity());
        return copy;
    }

    /**
     * Retourne la liste de toutes les demandes d'enregistrement en attente ({@code PENDING}),
     * en enrichissant chaque entrée avec les informations personnelles du moniteur associé
     * (ville de résidence, nationalité, genre) extraites depuis son profil {@link Monitor}.
     */
    @Override
    public List<DrivingSchoolPendingRequestDTO> retreivePendingRequest() {

        List<DrivingSchoolPendingRequestDTO> list = new ArrayList<>();

        for (DrivingSchoolRegistry registry : drivingSchoolRegistryRepository.findByDrivingSchoolStatusWithAdmin(DrivingSchoolStatus.PENDING)) {
            list.add(new DrivingSchoolPendingRequestDTO(
                    registry.getId(),
                    registry.getSchoolName(),
                    registry.getCountry(),
                    registry.getCity(),
                    registry.getAddress(),
                    registry.getWhatsappNumber(),
                    "%s %s".formatted(registry.getAdmin().getFirstname(), registry.getAdmin().getLastname()),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getResidenceCity).orElse(null),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getNationality).orElse(null),
                    registry.getAdmin().getMonitors().stream().findFirst()
                            .map(Monitor::getGender).orElse(null)
            ));
        }

        return list;
    }

    private static DrivingSchool getDrivingSchool(DrivingSchoolRegistry schoolRegistry, User monitor) {
        DrivingSchool ds = new DrivingSchool();
        ds.setName(schoolRegistry.getSchoolName());
        ds.setAddress(schoolRegistry.getAddress());
        ds.setPhoneNumber(schoolRegistry.getPhoneNumber());
        ds.setDescription(schoolRegistry.getDescription());
        ds.setUser(monitor);
        ds.setEmail(schoolRegistry.getEmail());
        ds.setCountry(schoolRegistry.getCountry());
        ds.setCity(schoolRegistry.getCity());
        ds.setWebsiteUrl(schoolRegistry.getWebsiteUrl());
        ds.setWhatsappNumber(schoolRegistry.getWhatsappNumber());
        ds.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        return ds;
    }

    /**
     * Retourne une page de registres d'auto-écoles, filtrée par statut si le paramètre
     * {@code status} est renseigné, ou portant sur l'ensemble des registres sinon. Chaque
     * entrée est mappée vers un {@link DrivingSchoolRegistryPageDTO} via le helper {@link #toPageDTO}.
     */
    @Override
    public Page<DrivingSchoolRegistryPageDTO> getAllRegistries(Pageable pageable, DrivingSchoolStatus status) {
        Page<DrivingSchoolRegistry> page = status != null
                ? drivingSchoolRegistryRepository.findByDrivingSchoolStatus(status, pageable)
                : drivingSchoolRegistryRepository.findAllWithAdmin(pageable);

        return page.map(this::toPageDTO);
    }

    /**
     * Retourne le détail complet d'un registre identifié par son {@code registryId}, mappé vers
     * un {@link DrivingSchoolRegistryPageDTO}. Lance une {@link IllegalArgumentException} si
     * aucun registre ne correspond à l'identifiant fourni.
     */
    @Override
    public DrivingSchoolRegistryPageDTO getRegistryById(UUID registryId) {
        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository
                .findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Auto-école introuvable : " + registryId));
        return toPageDTO(registry);
    }

    /**
     * Rejette une demande d'enregistrement identifiée par son {@code registryId} en passant
     * son statut à {@code REJECTED}. L'opération est refusée si le registre est déjà en statut
     * {@code APPROVED} ou {@code ACTIVE}, auquel cas une {@link IllegalStateException} est levée.
     * Une {@link IllegalArgumentException} est levée si le registre est introuvable.
     */
    @Override
    @Transactional
    public void rejectRegistry(UUID registryId) {
        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository
                .findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Auto-école introuvable : " + registryId));

        if (registry.getDrivingSchoolStatus() == DrivingSchoolStatus.APPROVED ||
                registry.getDrivingSchoolStatus() == DrivingSchoolStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Impossible de rejeter une auto-école déjà approuvée");
        }

        registry.setDrivingSchoolStatus(DrivingSchoolStatus.REJECTED);
        drivingSchoolRegistryRepository.save(registry);
        log.info("Auto-école {} rejetée", registry.getSchoolName());

        User monitor = registry.getAdmin();
        emailService.sendSchoolRejectedEmail(monitor.getEmail(), monitor.getFirstname(), registry.getSchoolName());
    }

    /**
     * Suspend un registre identifié par son {@code registryId} en passant son statut à
     * {@code SUSPENDED}. Seuls les registres en statut {@code ACTIVE} ou {@code APPROVED}
     * peuvent être suspendus ; tout autre statut provoque une {@link IllegalStateException}.
     * Une {@link IllegalArgumentException} est levée si le registre est introuvable.
     */
    @Override
    @Transactional
    public void suspendRegistry(UUID registryId) {
        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository
                .findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Auto-école introuvable : " + registryId));

        if (registry.getDrivingSchoolStatus() != DrivingSchoolStatus.ACTIVE &&
                registry.getDrivingSchoolStatus() != DrivingSchoolStatus.APPROVED) {
            throw new IllegalStateException(
                    "Seule une auto-école active peut être suspendue");
        }

        registry.setDrivingSchoolStatus(DrivingSchoolStatus.SUSPENDED);
        drivingSchoolRegistryRepository.save(registry);
        // Bloque l'accès aux données de l'auto-école (TenantResolutionFilter refuse le tenant).
        tenantProvisioningService.setActive(registry.getSchemaName(), false);
        log.info("Auto-école {} suspendue", registry.getSchoolName());
    }

    /**
     * Supprime définitivement un registre identifié par son {@code registryId}, sans contrainte
     * sur son statut courant. Lance une {@link IllegalArgumentException} si le registre est
     * introuvable avant la suppression.
     */
    @Override
    @Transactional
    public void deleteRegistry(UUID registryId) {
        DrivingSchoolRegistry registry = drivingSchoolRegistryRepository
                .findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Auto-école introuvable : " + registryId));

        // Le schéma PostgreSQL n'est PAS supprimé (sauvegarde / obligations comptables) :
        // il est seulement désactivé. Sa suppression définitive reste une opération manuelle.
        tenantProvisioningService.setActive(registry.getSchemaName(), false);
        drivingSchoolRegistryRepository.delete(registry);
        log.info("Auto-école {} supprimée définitivement", registry.getSchoolName());
    }

    /**
     * Calcule et retourne les statistiques globales du registre des auto-écoles : nombre total
     * d'entrées, nombre d'actives (somme des statuts {@code ACTIVE} et {@code APPROVED}), en attente,
     * rejetées, suspendues, nombre total de moniteurs enregistrés, et nombre de nouvelles inscriptions
     * depuis le premier jour du mois en cours.
     */
    @Override
    public SchoolRegistryStatsResponse getRegistryStats() {
        long total = drivingSchoolRegistryRepository.count();
        long active = drivingSchoolRegistryRepository
                .countByDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        long approved = drivingSchoolRegistryRepository
                .countByDrivingSchoolStatus(DrivingSchoolStatus.APPROVED);
        long pending = drivingSchoolRegistryRepository
                .countByDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        long rejected = drivingSchoolRegistryRepository
                .countByDrivingSchoolStatus(DrivingSchoolStatus.REJECTED);
        long suspended = drivingSchoolRegistryRepository
                .countByDrivingSchoolStatus(DrivingSchoolStatus.SUSPENDED);
        long newThisMonth = drivingSchoolRegistryRepository
                .countByCreatedAtAfter(LocalDate.now().withDayOfMonth(1));
        long totalsMonitors = drivingSchoolRegistryRepository
                .countByAdminRole(Role.MONITOR);

        return new SchoolRegistryStatsResponse(
                total,
                active + approved,
                pending,
                rejected,
                suspended,
                totalsMonitors,
                newThisMonth
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DrivingSchoolRegistryPageDTO> getMyRegistry(UUID userId) {
        return userRepository.findById(userId)
                .flatMap(drivingSchoolRegistryRepository::findByAdmin)
                .map(this::toPageDTO);
    }

    private DrivingSchoolRegistryPageDTO toPageDTO(DrivingSchoolRegistry registry) {
        User admin = registry.getAdmin();
        Monitor monitor = admin.getMonitors().stream().findFirst().orElse(null);

        return new DrivingSchoolRegistryPageDTO(
                registry.getId(),
                registry.getSchoolName(),
                registry.getCity(),
                registry.getCountry(),
                registry.getAddress(),
                registry.getEmail(),
                registry.getPhoneNumber(),
                registry.getWhatsappNumber(),
                registry.getWebsiteUrl(),
                registry.getDescription(),
                registry.getDrivingSchoolStatus(),
                "%s %s".formatted(admin.getFirstname(), admin.getLastname()),
                monitor != null ? monitor.getPhoneNumber() : null,
                registry.getCreatedAt()
        );
    }
}