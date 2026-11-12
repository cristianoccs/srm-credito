package com.srm.creditengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.srm.creditengine.config.PricingProperties;

@SpringBootApplication
@EnableConfigurationProperties(PricingProperties.class)
public class CreditEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditEngineApplication.class, args);
    }
}
