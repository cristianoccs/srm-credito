package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.CreateReceivableRequest;
import com.srm.creditengine.application.dto.ApiDtos.ReceivableResponse;
import com.srm.creditengine.domain.exception.ResourceNotFoundException;
import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ReceivableService {

    private final ReceivableRepository repository;

    public ReceivableService(ReceivableRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReceivableResponse create(CreateReceivableRequest request) {
        var entity = new ReceivableEntity(
                request.assignor().trim(),
                request.faceValue().setScale(6, RoundingMode.HALF_EVEN),
                request.termMonths(),
                request.maturityDate(),
                request.type()
        );
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ReceivableResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Recebível não encontrado: " + id));
    }

    private ReceivableResponse toResponse(ReceivableEntity entity) {
        return new ReceivableResponse(
                entity.getId(), entity.getAssignor(), entity.getFaceValue(), entity.getTermMonths(),
                entity.getMaturityDate(), entity.getType(), entity.getOriginalCurrency(),
                entity.getStatus(), entity.getCreatedAt()
        );
    }
}
