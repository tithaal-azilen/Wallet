package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;

public interface OrganizationRegistrationService {
    String registerOrganizationAndAdmin(OrganizationRegistrationDto registrationDto);
}
