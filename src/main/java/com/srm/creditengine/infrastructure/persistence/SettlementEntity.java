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
@Table(name = "settlements")
public class SettlementEntity {

    @Id
    private UUID id;

    @Column(name = "receivable_id", nullable = false, unique = true)
    private UUID receivableId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 160)
    private String idempotencyKey;

    @Column(name = "face_value_brl", nullable = false, precision = 19, scale = 6)
    private BigDecimal faceValueBrl;

    @Column(name = "present_value_brl", nullable = false, precision = 19, scale = 6)
    private BigDecimal presentValueBrl;

    @Column(name = "discount_brl", nullable = false, precision = 19, scale = 6)
    private BigDecimal discountBrl;

    @Column(name = "final_amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false, length = 3)
    private Currency paymentCurrency;

    @Column(name = "exchange_rate_id")
    private UUID exchangeRateId;

    @Column(name = "exchange_rate_value", precision = 19, scale = 10)
    private BigDecimal exchangeRateValue;

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SettlementEntity() {
    }

    public SettlementEntity(UUID receivableId, String idempotencyKey, BigDecimal faceValueBrl,
                            BigDecimal presentValueBrl, BigDecimal discountBrl, BigDecimal finalAmount,
                            Currency paymentCurrency, UUID exchangeRateId, BigDecimal exchangeRateValue,
                            Instant settledAt) {
        this.id = UUID.randomUUID();
        this.receivableId = receivableId;
        this.idempotencyKey = idempotencyKey;
        this.faceValueBrl = faceValueBrl;
        this.presentValueBrl = presentValueBrl;
        this.discountBrl = discountBrl;
        this.finalAmount = finalAmount;
        this.paymentCurrency = paymentCurrency;
        this.exchangeRateId = exchangeRateId;
        this.exchangeRateValue = exchangeRateValue;
        this.settledAt = settledAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getReceivableId() { return receivableId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BigDecimal getFaceValueBrl() { return faceValueBrl; }
    public BigDecimal getPresentValueBrl() { return presentValueBrl; }
    public BigDecimal getDiscountBrl() { return discountBrl; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public Currency getPaymentCurrency() { return paymentCurrency; }
    public UUID getExchangeRateId() { return exchangeRateId; }
    public BigDecimal getExchangeRateValue() { return exchangeRateValue; }
    public Instant getSettledAt() { return settledAt; }
    public Instant getCreatedAt() { return createdAt; }
}
