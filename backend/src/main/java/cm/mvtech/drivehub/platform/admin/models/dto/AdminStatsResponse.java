package cm.mvtech.drivehub.platform.admin.models.dto;

public record AdminStatsResponse(
        long totalAdmins,
        long pendingAdmins,
        long activeAdmins,
        long inactiveAdmins
) {}