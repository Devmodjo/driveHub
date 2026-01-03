package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.enums.AdminRole;
import cm.drivemaster.backend.enums.AdminStatus;
import cm.drivemaster.backend.models.dto.PlatformAdminAuthResponse;
import cm.drivemaster.backend.models.dto.PlatformAdminCreateRequest;
import cm.drivemaster.backend.models.dto.PlatformAdminLoginRequest;
import cm.drivemaster.backend.models.dto.PlatformAdminResponse;
import cm.drivemaster.backend.models.mappers.PlatformAdminMapper;
import cm.drivemaster.backend.repositories.PlatformAdminRepository;
import cm.drivemaster.backend.services.AdminerService;
import cm.drivemaster.backend.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
@Transactional
@RequiredArgsConstructor
public class PlatformAdminService implements AdminerService {


    private final PlatformAdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformAdminMapper adminMapper;

    @Override
    public PlatformAdminAuthResponse adminerLogin(PlatformAdminLoginRequest loginRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        PlatformAdmin admin = adminRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("utilisateur introuvable"));

        String adminToken = jwtService.generatePlatformAdminToken(admin);

        return new PlatformAdminAuthResponse(
                adminToken,
                admin.getRole(),
                admin.getAdminStatus()
        );

    }

    /**
     * inscription utilisateur
     * @param adminCreateRequest
     */
    @Override
    public void adminerResgistry(PlatformAdminCreateRequest adminCreateRequest) {

        if (adminCreateRequest.role() == AdminRole.ROOT) {
            throw new IllegalArgumentException("vous ne pouvez pas vous inscrire en tant que ROOT");
        }

        if (adminRepository.findByEmail(adminCreateRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Email déja utilisé !");
        }

        PlatformAdmin admin = new PlatformAdmin();
        admin.setName(adminCreateRequest.name());
        admin.setEmail(adminCreateRequest.email());
        admin.setRole(adminCreateRequest.role());
        admin.setPassword(passwordEncoder.encode(adminCreateRequest.password()));
        admin.setAdminStatus(AdminStatus.PENDING);
        admin.setPhoneNumber(adminCreateRequest.phoneNumber());
        admin.setResidence(adminCreateRequest.residence());

        adminRepository.save(admin);

    }

    /**
     * permmet a un admin ROOT
     * de voir les requete admin en attentes
     * @return list of pending request admin
     */
    @Override
    public List<PlatformAdminResponse> pendingAdminerRequest() {

        List<PlatformAdminResponse> list = new ArrayList<>();
        adminRepository.findByAdminStatus(AdminStatus.PENDING).forEach(
                el -> list.add(adminMapper.toResponse(el))
        );

        return list;
    }
}
