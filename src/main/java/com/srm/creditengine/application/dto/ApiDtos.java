package com.srm.creditengine.application.dto;

import com.srm.creditengine.domain.model.DomainEnums.Currency;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableStatus;
import com.srm.creditengine.domain.model.DomainEnums.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record CreateReceivableRequest(
            @NotBlank @Size(max = 160) String assignor,
            @NotNull @DecimalMin(value = "0.01") BigDecimal faceValue,
            @NotNull @Min(1) @Max(600) Integer termMonths,
            LocalDate maturityDate,
            @NotNull ReceivableType type
    ) {
    }

    public record ReceivableResponse(
            UUID id,
            String assignor,
            BigDecimal faceValue,
            Integer termMonths,
            LocalDate maturityDate,
            ReceivableType type,
            Currency originalCurrency,
            ReceivableStatus status,
            Instant createdAt
    ) {
    }

    public record ExchangeRateRequest(
            @NotNull Currency baseCurrency,
            @NotNull Currency quoteCurrency,
            @NotNull @DecimalMin(value = "0.0000000001") BigDecimal rate,
            @NotNull Instant validFrom
    ) {
    }

    public record ExchangeRateResponse(
            UUID id,
            Currency baseCurrency,
            Currency quoteCurrency,
            BigDecimal rate,
            Instant validFrom,
            Instant createdAt
    ) {
    }

    public record PricingSimulationRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal faceValue,
            @NotNull @Min(1) @Max(600) Integer termMonths,
            @NotNull ReceivableType type,
            @NotNull Currency paymentCurrency
    ) {
    }

    public record PricingResponse(
            ReceivableType receivableType,
            BigDecimal faceValueBrl,
            BigDecimal presentValueBrl,
            BigDecimal discountBrl,
            BigDecimal finalAmount,
            Currency paymentCurrency,
            BigDecimal exchangeRate
    ) {
    }

    public record SettlementRequest(
            @NotNull UUID receivableId,
            @NotNull Currency paymentCurrency
    ) {
    }

    public record SettlementResponse(
            UUID id,
            UUID receivableId,
            String idempotencyKey,
            BigDecimal faceValueBrl,
            BigDecimal presentValueBrl,
            BigDecimal discountBrl,
            BigDecimal finalAmount,
            Currency paymentCurrency,
            BigDecimal exchangeRate,
            Instant settledAt
    ) {
    }

    public record SettlementStatementItem(
            UUID id,
            UUID receivableId,
            String assignor,
            BigDecimal faceValueBrl,
            BigDecimal presentValueBrl,
            BigDecimal discountBrl,
            BigDecimal finalAmount,
            Currency paymentCurrency,
            BigDecimal exchangeRate,
            Instant settledAt
    ) {
    }
}
