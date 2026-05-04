package com.Innova.bank.user.validation;

public interface IdNumberValidator {

    void validateCustomer(String idNumber);

    void validateStaff(String idNumber);
}