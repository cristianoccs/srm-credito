package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.SettlementResponse;
import com.srm.creditengine.application.dto.ApiDtos.SettlementRequest;
import com.srm.creditengine.domain.exception.BusinessRuleException;
import com.srm.creditengine.domain.exception.IdempotencyConflictException;
import com.srm.creditengine.domain.exception.ResourceNotFoundException;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableStatus;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateRepository;
import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableRepository;
import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.math.RoundingMode;

@Service
public class SettlementService {

    private final ReceivableRepository receivableRepository;
    private final SettlementRepository settlementRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final PricingEngine pricingEngine;

    public SettlementService(
            ReceivableRepository receivableRepository,
            SettlementRepository settlementRepository,
            ExchangeRateRepository exchangeRateRepository,
            PricingEngine pricingEngine
    ) {
        this.receivableRepository = receivableRepository;
        this.settlementRepository = settlementRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.pricingEngine = pricingEngine;
    }

    @Transactional
    public SettlementOutcome settle(SettlementRequest request, String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);

        var existing = settlementRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            ensureSameRequest(existing.get(), request);
            return new SettlementOutcome(toResponse(existing.get()), true);
        }

        ReceivableEntity receivable = receivableRepository.findByIdForUpdate(request.receivableId())
                .orElseThrow(() -> new ResourceNotFoundException("Recebível não encontrado: " + request.receivableId()));

        existing = settlementRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            ensureSameRequest(existing.get(), request);
            return new SettlementOutcome(toResponse(existing.get()), true);
        }

        if (receivable.getStatus() == ReceivableStatus.SETTLED) {
            throw new BusinessRuleException("RECEIVABLE_ALREADY_SETTLED", "O recebível já foi liquidado");
        }

        Instant settledAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ExchangeRateEntity exchangeRate = request.paymentCurrency() == Currency.USD
                ? exchangeRateRepository.findEffectiveRate(Currency.BRL, Currency.USD, settledAt)
                    .orElseThrow(() -> new BusinessRuleException("MISSING_EXCHANGE_RATE", "Não existe taxa BRL/USD vigente para a liquidação"))
                : null;

        var quote = pricingEngine.calculate(
                receivable.getFaceValue(), receivable.getTermMonths(), receivable.getType(),
                request.paymentCurrency(), exchangeRate == null ? null : exchangeRate.getRate()
        );

        var settlement = new SettlementEntity(
                receivable.getId(), idempotencyKey, quote.faceValueBrl(), quote.presentValueBrl(),
                quote.discountBrl(), quote.finalAmount(), quote.paymentCurrency(),
                exchangeRate == null ? null : exchangeRate.getId(),
                exchangeRate == null ? null : exchangeRate.getRate(), settledAt
        );

        receivable.settle();
        settlementRepository.save(settlement);
        receivableRepository.save(receivable);

        return new SettlementOutcome(toResponse(settlement), false);
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 160) {
            throw new BusinessRuleException("INVALID_IDEMPOTENCY_KEY", "O header Idempotency-Key é obrigatório e deve ter até 160 caracteres");
        }
    }

    private void ensureSameRequest(SettlementEntity existing, SettlementRequest request) {
        if (!existing.getReceivableId().equals(request.receivableId())
                || existing.getPaymentCurrency() != request.paymentCurrency()) {
            throw new IdempotencyConflictException("A chave de idempotência já foi usada com outra requisição");
        }
    }

    private SettlementResponse toResponse(SettlementEntity entity) {
        return new SettlementResponse(
                entity.getId(), entity.getReceivableId(), entity.getIdempotencyKey(), money(entity.getFaceValueBrl()),
                money(entity.getPresentValueBrl()), money(entity.getDiscountBrl()), money(entity.getFinalAmount()),
                entity.getPaymentCurrency(), rate(entity.getExchangeRateValue()), entity.getSettledAt()
        );
    }

    private java.math.BigDecimal money(java.math.BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private java.math.BigDecimal rate(java.math.BigDecimal value) {
        return value == null ? null : value.setScale(10, RoundingMode.HALF_EVEN);
    }

    public record SettlementOutcome(SettlementResponse response, boolean replayed) {
    }
}
