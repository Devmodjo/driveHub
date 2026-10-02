package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.mapper.MonitorMapper;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Moniteurs de l'auto-école courante (tenant). */
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final MonitorsRepository monitorsRepository;
    private final MonitorMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<MonitorResponseDto> list() {
        return monitorsRepository.findAllByOrderByCreatedOnAsc().stream()
                .filter(monitor -> !monitor.isDeleted())
                .map(mapper::fromEntityToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonitorResponseDto me() {
        UUID userId = currentUserProvider.get().getId();
        return monitorsRepository.findFirstByUser_Id(userId)
                .map(mapper::fromEntityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun profil moniteur dans cette auto-école"));
    }

    public Monitor getEntity(UUID id) {
        return monitorsRepository.findById(id)
                .filter(monitor -> !monitor.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Moniteur introuvable : " + id));
    }
}
