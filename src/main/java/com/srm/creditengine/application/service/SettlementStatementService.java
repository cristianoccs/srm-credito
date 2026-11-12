package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.SettlementStatementItem;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.infrastructure.persistence.SettlementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class SettlementStatementService {

    private final SettlementRepository repository;

    public SettlementStatementService(SettlementRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<SettlementStatementItem> search(
            LocalDate from,
            LocalDate to,
            String assignor,
            Currency currency,
            Pageable pageable
    ) {
        Instant fromInstant = from == null ? null : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to == null ? null : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return repository.search(
        fromInstant,
        toInstant,
        blankToNull(assignor),
        currency == null ? null : currency.name(),
        pageable
)

                .map(row -> new SettlementStatementItem(
                        row.getId(), row.getReceivableId(), row.getAssignor(), row.getFaceValueBrl(),
                        row.getPresentValueBrl(), row.getDiscountBrl(), row.getFinalAmount(),
                        row.getPaymentCurrency(), row.getExchangeRate(), row.getSettledAt()
                ));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
