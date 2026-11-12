package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.PricingResponse;
import com.srm.creditengine.application.dto.ApiDtos.PricingSimulationRequest;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.domain.pricing.PricingQuote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PricingApplicationService {

    private final PricingEngine pricingEngine;
    private final ExchangeRateService exchangeRateService;

    public PricingApplicationService(PricingEngine pricingEngine, ExchangeRateService exchangeRateService) {
        this.pricingEngine = pricingEngine;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional(readOnly = true)
    public PricingResponse simulate(PricingSimulationRequest request) {
        var exchangeRate = request.paymentCurrency() == Currency.USD
                ? exchangeRateService.findEffectiveUsdRate(Instant.now())
                : null;
        var quote = pricingEngine.calculate(
                request.faceValue(), request.termMonths(), request.type(), request.paymentCurrency(),
                exchangeRate == null ? null : exchangeRate.getRate()
        );
        return toResponse(quote);
    }

    static PricingResponse toResponse(PricingQuote quote) {
        return new PricingResponse(
                quote.receivableType(), quote.faceValueBrl(), quote.presentValueBrl(), quote.discountBrl(),
                quote.finalAmount(), quote.paymentCurrency(), quote.exchangeRate()
        );
    }
}
