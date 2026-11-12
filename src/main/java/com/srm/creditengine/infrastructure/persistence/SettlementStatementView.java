package com.srm.creditengine.infrastructure.persistence;

import com.srm.creditengine.domain.model.DomainEnums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface SettlementStatementView {
    UUID getId();
    UUID getReceivableId();
    String getAssignor();
    BigDecimal getFaceValueBrl();
    BigDecimal getPresentValueBrl();
    BigDecimal getDiscountBrl();
    BigDecimal getFinalAmount();
    Currency getPaymentCurrency();
    BigDecimal getExchangeRate();
    Instant getSettledAt();
}
