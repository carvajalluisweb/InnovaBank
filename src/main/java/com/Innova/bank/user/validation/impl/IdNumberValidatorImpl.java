package com.Innova.bank.user.validation.impl;

import com.Innova.bank.common.exception.ExceptionFactory;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.validation.IdNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdNumberValidatorImpl implements IdNumberValidator {

    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;
    private final ExceptionFactory exceptionFactory;

    @Override
    public void validateCustomer(String idNumber) {

        if (userCustomerRepository.existsByIdNumber(idNumber)) {
            throw exceptionFactory.badRequest( "La cédula " + idNumber + " ya se encuentra registrada");
        }
    }

    @Override
    public void validateStaff(String idNumber) {

        if (userStaffRepository.existsByIdNumber(idNumber)) {
            throw exceptionFactory.badRequest("El personal con cédula " + idNumber + " ya se encuentra registrado");
        }
    }
}