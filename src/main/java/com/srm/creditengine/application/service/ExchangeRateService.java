package com.srm.creditengine.application.service;

import com.srm.creditengine.application.dto.ApiDtos.ExchangeRateRequest;
import com.srm.creditengine.application.dto.ApiDtos.ExchangeRateResponse;
import com.srm.creditengine.domain.exception.BusinessRuleException;
import com.srm.creditengine.domain.exception.MissingExchangeRateException;
import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository repository;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExchangeRateResponse create(ExchangeRateRequest request) {
        validatePair(request.baseCurrency(), request.quoteCurrency());
        var entity = new ExchangeRateEntity(
                request.baseCurrency(), request.quoteCurrency(),
                request.rate().setScale(10, RoundingMode.HALF_EVEN), request.validFrom()
        );
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExchangeRateEntity findEffectiveUsdRate(Instant at) {
        return repository.findEffectiveRate(Currency.BRL, Currency.USD, at)
                .orElseThrow(() -> new MissingExchangeRateException(
                        "Nenhuma taxa BRL/USD vigente foi encontrada para " + at));
    }

    private void validatePair(Currency base, Currency quote) {
        if (base != Currency.BRL || quote != Currency.USD) {
            throw new BusinessRuleException("UNSUPPORTED_CURRENCY_PAIR", "A primeira versão suporta somente o par BRL/USD");
        }
    }

    private ExchangeRateResponse toResponse(ExchangeRateEntity entity) {
        return new ExchangeRateResponse(
                entity.getId(), entity.getBaseCurrency(), entity.getQuoteCurrency(), entity.getRate(),
                entity.getValidFrom(), entity.getCreatedAt()
        );
    }
}
