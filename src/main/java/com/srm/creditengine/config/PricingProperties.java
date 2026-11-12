package com.srm.creditengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ConfigurationProperties(prefix = "credit-engine.pricing")
public record PricingProperties(BigDecimal baseRate, int scale, RoundingMode roundingMode) {
}
