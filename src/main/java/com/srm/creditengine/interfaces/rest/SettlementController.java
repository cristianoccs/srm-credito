package com.srm.creditengine.interfaces.rest;

import com.srm.creditengine.application.dto.ApiDtos.SettlementRequest;
import com.srm.creditengine.application.dto.ApiDtos.SettlementResponse;
import com.srm.creditengine.application.dto.ApiDtos.SettlementStatementItem;
import com.srm.creditengine.application.dto.PagedResponse;
import com.srm.creditengine.application.service.SettlementService;
import com.srm.creditengine.application.service.SettlementStatementService;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementStatementService statementService;

    public SettlementController(SettlementService settlementService, SettlementStatementService statementService) {
        this.settlementService = settlementService;
        this.statementService = statementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> settle(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody SettlementRequest request
    ) {
        var outcome = settlementService.settle(request, idempotencyKey);
        return ResponseEntity.status(outcome.replayed() ? 200 : 201).body(outcome.response());
    }

    @GetMapping
    public PagedResponse<SettlementStatementItem> statement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String assignor,
            @RequestParam(required = false) Currency currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page deve ser >= 0 e size deve estar entre 1 e 100");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "settledAt"));
        return PagedResponse.from(statementService.search(from, to, assignor, currency, pageable));
    }
}
