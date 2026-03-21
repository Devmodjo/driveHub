package cm.mvtech.drivehub.modules.auth.application.dto;

import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String token;
    private Role role;
    private ProfileStatus profileStatus;
    private Boolean fullProfile;
}
