package com.srm.creditengine.infrastructure.persistence;

import com.srm.creditengine.domain.model.DomainEnums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRateEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 3)
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false, length = 3)
    private Currency quoteCurrency;

    @Column(nullable = false, precision = 19, scale = 10)
    private BigDecimal rate;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ExchangeRateEntity() {
    }

    public ExchangeRateEntity(Currency baseCurrency, Currency quoteCurrency, BigDecimal rate, Instant validFrom) {
        this.id = UUID.randomUUID();
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.rate = rate;
        this.validFrom = validFrom;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Currency getBaseCurrency() { return baseCurrency; }
    public Currency getQuoteCurrency() { return quoteCurrency; }
    public BigDecimal getRate() { return rate; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getCreatedAt() { return createdAt; }
}
