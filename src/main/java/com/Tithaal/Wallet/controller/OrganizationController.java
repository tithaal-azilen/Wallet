package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Control", description = "Endpoints for managing organizations and viewing their transactions")
public class OrganizationController {

    private final com.Tithaal.Wallet.service.OrganizationRegistrationService organizationRegistrationService;

    @Operation(summary = "Register an Organization and Admin User")
    @PostMapping
    public ResponseEntity<String> registerOrganization(
            @Valid @RequestBody OrganizationRegistrationDto registrationDto) {
        String orgCode = organizationRegistrationService.registerOrganizationAndAdmin(registrationDto);
        return new ResponseEntity<>("Organization and Admin created successfully! Organization Code: " + orgCode,
                HttpStatus.CREATED);
    }

}
