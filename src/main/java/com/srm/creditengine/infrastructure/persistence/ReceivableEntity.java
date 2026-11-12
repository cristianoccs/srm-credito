package com.srm.creditengine.infrastructure.persistence;

import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableStatus;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receivables")
public class ReceivableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String assignor;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal faceValue;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReceivableType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "original_currency", nullable = false, length = 3)
    private Currency originalCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReceivableStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReceivableEntity() {
    }

    public ReceivableEntity(String assignor, BigDecimal faceValue, Integer termMonths,
                            LocalDate maturityDate, ReceivableType type) {
        this.id = UUID.randomUUID();
        this.assignor = assignor;
        this.faceValue = faceValue;
        this.termMonths = termMonths;
        this.maturityDate = maturityDate;
        this.type = type;
        this.originalCurrency = Currency.BRL;
        this.status = ReceivableStatus.OPEN;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getAssignor() { return assignor; }
    public BigDecimal getFaceValue() { return faceValue; }
    public Integer getTermMonths() { return termMonths; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public ReceivableType getType() { return type; }
    public Currency getOriginalCurrency() { return originalCurrency; }
    public ReceivableStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void settle() {
        if (status == ReceivableStatus.SETTLED) {
            throw new IllegalStateException("Receivable is already settled");
        }
        this.status = ReceivableStatus.SETTLED;
    }
}
