package cm.mvtech.drivehub.support;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

/**
 * Fabrique d'objets de test (« fixtures ») partagée par les tests unitaires.
 *
 * <p>Chaque méthode renvoie un objet NEUF, déjà rempli avec des valeurs valides : un test ne
 * modifie ensuite que le champ qui l'intéresse (par exemple l'état d'un véhicule). Cela évite
 * de recopier dix lignes de {@code setX(...)} dans chaque classe de test.</p>
 *
 * <p>Les identifiants sont générés aléatoirement : deux appels donnent deux objets distincts.</p>
 */
public final class TestData {

    private TestData() {
    }

    /** Compte utilisateur (schéma public) avec le rôle demandé et un email déjà vérifié. */
    public static User user(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstname("Prenom" + role.name());
        user.setLastname("Nom" + role.name());
        user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.cm");
        user.setPassword("motDePasseHache");
        user.setRoles(role);
        user.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);
        user.setFullProfile(false);
        user.setStudents(new HashSet<>());
        user.setMonitors(new HashSet<>());
        return user;
    }

    /** Principal Spring Security construit à partir d'un utilisateur (comme le fait le filtre JWT). */
    public static UserPrincipal principal(User user) {
        return UserPrincipal.build(user);
    }

    /** Fiche élève rattachée à l'utilisateur donné (permis B par défaut). */
    public static Student student(User user) {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setUser(user);
        student.setPhoneNumber("+237677000000");
        student.setDateOfBirth(new Date());
        student.setGender(Gender.MALE);
        student.setNationality("Camerounaise");
        student.setResidenceCity("Douala");
        student.setLicenseCategory(LicenseCategory.B);
        return student;
    }

    /** Fiche moniteur rattachée à l'utilisateur donné. */
    public static Monitor monitor(User user) {
        Monitor monitor = new Monitor();
        monitor.setId(UUID.randomUUID());
        monitor.setUser(user);
        monitor.setPhoneNumber("+237699000000");
        monitor.setDateOfBirth(new Date());
        monitor.setGender(Gender.FEMALE);
        monitor.setNationality("Camerounaise");
        monitor.setResidenceCity("Yaoundé");
        return monitor;
    }

    /** Véhicule disponible (état DISPOSABLE). */
    public static Vehicle vehicle(String matriculation) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setMatriculation(matriculation);
        vehicle.setModel("Toyota Yaris");
        vehicle.setState(State.DISPOSABLE);
        return vehicle;
    }

    /** Fiche de l'auto-école telle qu'elle existe dans le schéma du tenant. */
    public static DrivingSchool school() {
        DrivingSchool school = new DrivingSchool();
        school.setId(UUID.randomUUID());
        school.setName("Auto-École Test");
        school.setEmail("contact@ecole-test.cm");
        school.setPhoneNumber("+237699111222");
        school.setAddress("Akwa");
        school.setDrivingSchoolStatus(DrivingSchoolStatus.ACTIVE);
        return school;
    }

    /** Entrée du registre public (une ligne par auto-école), avec son responsable. */
    public static DrivingSchoolRegistry registry(User admin, DrivingSchoolStatus status) {
        DrivingSchoolRegistry registry = new DrivingSchoolRegistry();
        registry.setId(UUID.randomUUID());
        registry.setSchoolName("Auto-École Test");
        registry.setSchemaName("ae_auto_ecole_test_abc123");
        registry.setEmail("contact@ecole-test.cm");
        registry.setPhoneNumber("+237699111222");
        registry.setAddress("Akwa");
        registry.setCity("Douala");
        registry.setDrivingSchoolStatus(status);
        registry.setAdmin(admin);
        return registry;
    }
}
