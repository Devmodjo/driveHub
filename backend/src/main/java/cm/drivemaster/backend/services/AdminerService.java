package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.PlatformAdminAuthResponse;
import cm.drivemaster.backend.models.dto.PlatformAdminCreateRequest;
import cm.drivemaster.backend.models.dto.PlatformAdminLoginRequest;
import cm.drivemaster.backend.models.dto.PlatformAdminResponse;

import java.util.List;

public interface AdminerService {


    PlatformAdminAuthResponse adminerLogin(PlatformAdminLoginRequest loginRequest);

    void adminerResgistry(PlatformAdminCreateRequest adminCreateRequest);

    List<PlatformAdminResponse> pendingAdminerRequest();
}
