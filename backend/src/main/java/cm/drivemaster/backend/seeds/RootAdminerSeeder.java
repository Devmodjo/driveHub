package cm.drivemaster.backend.seeds;

import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.enums.AdminRole;
import cm.drivemaster.backend.enums.AdminStatus;
import cm.drivemaster.backend.repositories.PlatformAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RootAdminerSeeder implements ApplicationRunner {

    private final PlatformAdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {

        //  Vérifier si un ROOT existe déjà
        if (adminRepository.existsByRole(AdminRole.ROOT)) {
            return;
        }

        //  Lire les infos depuis la config
        String email = environment.getProperty("app.root-admin.email");
        String password = environment.getProperty("app.root-admin.password");

        if (email == null || password == null) {
            throw new IllegalStateException(
                    "ROOT admin credentials missing in configuration"
            );
        }

        //  Créer le ROOT
        PlatformAdmin root = new PlatformAdmin();
        root.setName("root");
        root.setEmail(email);
        root.setPassword(passwordEncoder.encode(password));
        root.setRole(AdminRole.ROOT);
        root.setStatus(AdminStatus.ACTIVE);
        root.setResidence("SYSTEM");
        root.setEnabled(true);

        adminRepository.save(root);

        log.info("ROOT admin created: " + email);
    }
}
