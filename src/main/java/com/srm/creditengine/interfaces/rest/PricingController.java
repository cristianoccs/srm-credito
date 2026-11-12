package com.srm.creditengine.interfaces.rest;

import com.srm.creditengine.application.dto.ApiDtos.PricingResponse;
import com.srm.creditengine.application.dto.ApiDtos.PricingSimulationRequest;
import com.srm.creditengine.application.service.PricingApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingApplicationService service;

    public PricingController(PricingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/simulations")
    public PricingResponse simulate(@Valid @RequestBody PricingSimulationRequest request) {
        return service.simulate(request);
    }
}
