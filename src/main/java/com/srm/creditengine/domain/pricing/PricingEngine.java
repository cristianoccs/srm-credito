package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.exception.BusinessRuleException;
import com.srm.creditengine.config.PricingProperties;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PricingEngine {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private final Map<ReceivableType, PricingStrategy> strategies;
    private final BigDecimal baseRate;
    private final int monetaryScale;
    private final RoundingMode roundingMode;

    public PricingEngine(
            List<PricingStrategy> pricingStrategies,
            PricingProperties properties
    ) {
        this.strategies = new EnumMap<>(ReceivableType.class);
        pricingStrategies.forEach(strategy -> this.strategies.put(strategy.supportedType(), strategy));
        this.baseRate = properties.baseRate();
        this.monetaryScale = properties.scale();
        this.roundingMode = properties.roundingMode();
    }

    public PricingQuote calculate(
            BigDecimal faceValueBrl,
            int termMonths,
            ReceivableType receivableType,
            Currency paymentCurrency,
            BigDecimal exchangeRate
    ) {
        validateInput(faceValueBrl, termMonths, receivableType, paymentCurrency, exchangeRate);

        PricingStrategy strategy = strategies.get(receivableType);
        if (strategy == null) {
            throw new BusinessRuleException("UNSUPPORTED_RECEIVABLE_TYPE", "Tipo de recebível sem estratégia de precificação");
        }

        BigDecimal monthlyRate = baseRate.add(strategy.monthlySpread(), CALCULATION_CONTEXT);
        BigDecimal discountFactor = ONE.add(monthlyRate, CALCULATION_CONTEXT)
                .pow(termMonths, CALCULATION_CONTEXT);
        BigDecimal rawPresentValue = faceValueBrl.divide(discountFactor, CALCULATION_CONTEXT);
        BigDecimal presentValueBrl = rawPresentValue.setScale(monetaryScale, roundingMode);
        BigDecimal faceValueRounded = faceValueBrl.setScale(monetaryScale, roundingMode);
        BigDecimal discountBrl = faceValueRounded.subtract(presentValueBrl).setScale(monetaryScale, roundingMode);

        BigDecimal finalAmount = presentValueBrl;
        if (paymentCurrency == Currency.USD) {
            finalAmount = presentValueBrl.divide(exchangeRate, CALCULATION_CONTEXT)
                    .setScale(monetaryScale, roundingMode);
        }

        return new PricingQuote(
                receivableType,
                faceValueRounded,
                presentValueBrl,
                discountBrl,
                finalAmount,
                paymentCurrency,
                exchangeRate
        );
    }

    private void validateInput(
            BigDecimal faceValueBrl,
            int termMonths,
            ReceivableType receivableType,
            Currency paymentCurrency,
            BigDecimal exchangeRate
    ) {
        if (faceValueBrl == null || faceValueBrl.signum() <= 0) {
            throw new BusinessRuleException("INVALID_FACE_VALUE", "O valor de face deve ser maior que zero");
        }
        if (termMonths <= 0) {
            throw new BusinessRuleException("INVALID_TERM", "O prazo deve ser maior que zero mês");
        }
        if (receivableType == null) {
            throw new BusinessRuleException("INVALID_RECEIVABLE_TYPE", "O tipo de recebível é obrigatório");
        }
        if (paymentCurrency == null) {
            throw new BusinessRuleException("INVALID_PAYMENT_CURRENCY", "A moeda de pagamento é obrigatória");
        }
        if (paymentCurrency == Currency.USD && (exchangeRate == null || exchangeRate.signum() <= 0)) {
            throw new BusinessRuleException("INVALID_EXCHANGE_RATE", "A taxa BRL/USD deve ser maior que zero para pagamentos em USD");
        }
    }
}
