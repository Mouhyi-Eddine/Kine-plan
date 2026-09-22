package com.kineplan.caretype.application;

import com.kineplan.caretype.api.CareTypeRequest;
import com.kineplan.caretype.api.CareTypeResponse;
import com.kineplan.caretype.domain.CareType;
import com.kineplan.caretype.domain.CareTypeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareTypeService {
    private final CareTypeRepository repository;

    public CareTypeService(CareTypeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CareTypeResponse> list(UUID cabinetId) {
        return repository.findByCabinetIdAndActiveTrueOrderByName(cabinetId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CareTypeResponse create(UUID cabinetId, CareTypeRequest request) {
        return toResponse(repository.save(new CareType(UUID.randomUUID(), cabinetId, request.name(),
                request.defaultDurationMinutes(), request.displayColor())));
    }

    private CareTypeResponse toResponse(CareType careType) {
        return new CareTypeResponse(careType.getId(), careType.getName(), careType.getDefaultDurationMinutes(),
                careType.getDisplayColor());
    }
}