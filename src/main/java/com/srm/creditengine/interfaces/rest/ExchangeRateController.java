package com.srm.creditengine.interfaces.rest;

import com.srm.creditengine.application.dto.ApiDtos.ExchangeRateRequest;
import com.srm.creditengine.application.dto.ApiDtos.ExchangeRateResponse;
import com.srm.creditengine.application.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService service;

    public ExchangeRateController(ExchangeRateService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody ExchangeRateRequest request) {
        var response = service.create(request);
        return ResponseEntity.created(URI.create("/api/exchange-rates/" + response.id())).body(response);
    }
}
