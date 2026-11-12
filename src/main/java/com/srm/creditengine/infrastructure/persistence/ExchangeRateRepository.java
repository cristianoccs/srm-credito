package com.srm.creditengine.infrastructure.persistence;

import com.srm.creditengine.domain.model.DomainEnums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    @Query("select r from ExchangeRateEntity r " +
           "where r.baseCurrency = :baseCurrency and r.quoteCurrency = :quoteCurrency " +
           "and r.validFrom <= :at order by r.validFrom desc")
    Optional<ExchangeRateEntity> findEffectiveRate(
            @Param("baseCurrency") Currency baseCurrency,
            @Param("quoteCurrency") Currency quoteCurrency,
            @Param("at") Instant at
    );
}
