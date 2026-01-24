package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private long id;
    private String token;
    private Role role;
    private ProfileStatus profileStatus;
    private Boolean fullProfile;
}
