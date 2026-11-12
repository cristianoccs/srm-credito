package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.exception.BusinessRuleException;
import com.srm.creditengine.config.PricingProperties;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingEngineTest {

    private PricingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PricingEngine(
                List.of(new MerchantInvoicePricingStrategy(), new PreDatedCheckPricingStrategy()),
                new PricingProperties(new BigDecimal("0.0100"), 2, RoundingMode.HALF_EVEN)
        );
    }

    @Test
    void shouldMatchGoldenCaseC1MerchantInvoiceInBrl() {
        var quote = engine.calculate(
                new BigDecimal("100000.00"), 3, ReceivableType.DUPLICATA_MERCANTIL,
                Currency.BRL, null
        );

        assertThat(quote.presentValueBrl()).isEqualByComparingTo("92859.94");
        assertThat(quote.discountBrl()).isEqualByComparingTo("7140.06");
        assertThat(quote.finalAmount()).isEqualByComparingTo("92859.94");
    }

    @Test
    void shouldMatchGoldenCaseC2PreDatedCheckInBrl() {
        var quote = engine.calculate(
                new BigDecimal("25000.00"), 2, ReceivableType.CHEQUE_PRE_DATADO,
                Currency.BRL, null
        );

        assertThat(quote.presentValueBrl()).isEqualByComparingTo("23337.77");
        assertThat(quote.discountBrl()).isEqualByComparingTo("1662.23");
        assertThat(quote.finalAmount()).isEqualByComparingTo("23337.77");
    }

    @Test
    void shouldMatchGoldenCaseC3CrossCurrencyAfterBrlRounding() {
        var quote = engine.calculate(
                new BigDecimal("100000.00"), 3, ReceivableType.DUPLICATA_MERCANTIL,
                Currency.USD, new BigDecimal("5.4321")
        );

        assertThat(quote.presentValueBrl()).isEqualByComparingTo("92859.94");
        assertThat(quote.finalAmount()).isEqualByComparingTo("17094.67");
        assertThat(quote.discountBrl()).isEqualByComparingTo("7140.06");
    }

    @Test
    void shouldRejectUsdWithoutExchangeRate() {
        assertThatThrownBy(() -> engine.calculate(
                new BigDecimal("100.00"), 1, ReceivableType.DUPLICATA_MERCANTIL,
                Currency.USD, null
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("taxa BRL/USD");
    }

    @Test
    void shouldRejectNonPositiveTerm() {
        assertThatThrownBy(() -> engine.calculate(
                new BigDecimal("100.00"), 0, ReceivableType.DUPLICATA_MERCANTIL,
                Currency.BRL, null
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("prazo");
    }
}
