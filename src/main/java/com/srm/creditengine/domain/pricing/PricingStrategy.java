package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;

import java.math.BigDecimal;

public interface PricingStrategy {

    ReceivableType supportedType();

    BigDecimal monthlySpread();
}
