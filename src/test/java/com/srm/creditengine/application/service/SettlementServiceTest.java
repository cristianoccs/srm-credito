package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.SettlementRequest;
import com.srm.creditengine.config.PricingProperties;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import com.srm.creditengine.domain.pricing.MerchantInvoicePricingStrategy;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.domain.pricing.PreDatedCheckPricingStrategy;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateRepository;
import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableRepository;
import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SettlementServiceTest {

    @Test
    void shouldReturnTheOriginalSettlementWhenTheSameIdempotencyKeyIsRetried() {
        var receivableRepository = mock(ReceivableRepository.class);
        var settlementRepository = mock(SettlementRepository.class);
        var exchangeRateRepository = mock(ExchangeRateRepository.class);
        var engine = new PricingEngine(
                List.of(new MerchantInvoicePricingStrategy(), new PreDatedCheckPricingStrategy()),
                new PricingProperties(new BigDecimal("0.0100"), 2, RoundingMode.HALF_EVEN)
        );
        var service = new SettlementService(receivableRepository, settlementRepository, exchangeRateRepository, engine);
        var receivable = new ReceivableEntity(
                "Cedente A", new BigDecimal("100000.00"), 3, null, ReceivableType.DUPLICATA_MERCANTIL
        );
        var request = new SettlementRequest(receivable.getId(), Currency.BRL);
        var persisted = new SettlementEntity(
                receivable.getId(), "retry-001", new BigDecimal("100000.00"),
                new BigDecimal("92859.94"), new BigDecimal("7140.06"), new BigDecimal("92859.94"),
                Currency.BRL, null, null, java.time.Instant.parse("2026-01-01T10:00:00Z")
        );

        var storedSettlement = new AtomicReference<SettlementEntity>();
        when(settlementRepository.findByIdempotencyKey("retry-001"))
                .thenAnswer(invocation -> Optional.ofNullable(storedSettlement.get()));
        when(receivableRepository.findByIdForUpdate(receivable.getId())).thenReturn(Optional.of(receivable));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(invocation -> {
            SettlementEntity saved = invocation.getArgument(0);
            storedSettlement.set(saved);
            return saved;
        });

        var first = service.settle(request, "retry-001");
        var second = service.settle(request, "retry-001");

        assertThat(first.replayed()).isFalse();
        assertThat(second.replayed()).isTrue();
        assertThat(second.response().id()).isEqualTo(first.response().id());
        verify(settlementRepository, times(1)).save(any(SettlementEntity.class));
        verify(receivableRepository, times(1)).findByIdForUpdate(receivable.getId());
    }

    @Test
    void shouldUseConflictWhenAKeyIsReusedForAnotherRequest() {
        var receivableRepository = mock(ReceivableRepository.class);
        var settlementRepository = mock(SettlementRepository.class);
        var exchangeRateRepository = mock(ExchangeRateRepository.class);
        var engine = new PricingEngine(
                List.of(new MerchantInvoicePricingStrategy(), new PreDatedCheckPricingStrategy()),
                new PricingProperties(new BigDecimal("0.0100"), 2, RoundingMode.HALF_EVEN)
        );
        var service = new SettlementService(receivableRepository, settlementRepository, exchangeRateRepository, engine);
        var storedReceivableId = UUID.randomUUID();
        var persisted = new SettlementEntity(
                storedReceivableId, "retry-002", new BigDecimal("100.00"),
                new BigDecimal("92.86"), new BigDecimal("7.14"), new BigDecimal("92.86"),
                Currency.BRL, null, null, java.time.Instant.now()
        );
        when(settlementRepository.findByIdempotencyKey("retry-002")).thenReturn(Optional.of(persisted));

        var differentRequest = new SettlementRequest(UUID.randomUUID(), Currency.USD);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.settle(differentRequest, "retry-002"))
                .isInstanceOf(com.srm.creditengine.domain.exception.IdempotencyConflictException.class);
        verifyNoInteractions(receivableRepository);
    }
}
