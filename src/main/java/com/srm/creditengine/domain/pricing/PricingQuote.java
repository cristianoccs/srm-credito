package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;

import java.math.BigDecimal;

public record PricingQuote(
        ReceivableType receivableType,
        BigDecimal faceValueBrl,
        BigDecimal presentValueBrl,
        BigDecimal discountBrl,
        BigDecimal finalAmount,
        Currency paymentCurrency,
        BigDecimal exchangeRate
) {
}
