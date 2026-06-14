package cm.mvtech.drivehub.modules.drivingschool.application.dto;

public record SchoolRegistryStatsResponse(
        long totalRegistries,
        long activeRegistries,
        long pendingRegistries,
        long rejectedRegistries,
        long suspendedRegistries,
        long totalMonitors,
        long newThisMonth
) {
}
