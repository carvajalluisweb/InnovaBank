package com.Innova.bank.auth.service;

import com.Innova.bank.auth.dto.RegisterCustomerRequest;
import com.Innova.bank.auth.dto.RegisterStaffRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrationService {

    void registerCustomer(RegisterCustomerRequest request, HttpServletRequest httpRequest);

    void registerStaff(RegisterStaffRequest request, HttpServletRequest httpRequest);
}