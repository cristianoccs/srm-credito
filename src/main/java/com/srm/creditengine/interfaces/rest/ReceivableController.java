package com.srm.creditengine.interfaces.rest;

import com.srm.creditengine.application.dto.ApiDtos.CreateReceivableRequest;
import com.srm.creditengine.application.dto.ApiDtos.ReceivableResponse;
import com.srm.creditengine.application.service.ReceivableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
public class ReceivableController {

    private final ReceivableService service;

    public ReceivableController(ReceivableService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReceivableResponse> create(@Valid @RequestBody CreateReceivableRequest request) {
        var response = service.create(request);
        return ResponseEntity.created(URI.create("/api/receivables/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ReceivableResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }
}
