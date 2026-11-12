package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PreDatedCheckPricingStrategy implements PricingStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.0250");

    @Override
    public ReceivableType supportedType() {
        return ReceivableType.CHEQUE_PRE_DATADO;
    }

    @Override
    public BigDecimal monthlySpread() {
        return MONTHLY_SPREAD;
    }
}
