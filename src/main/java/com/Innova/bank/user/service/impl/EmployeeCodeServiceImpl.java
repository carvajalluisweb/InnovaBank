package com.Innova.bank.user.service.impl;

import com.Innova.bank.enums.Position;
import com.Innova.bank.user.service.EmployeeCodeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeCodeServiceImpl implements EmployeeCodeService {

    @Override
    public String generate(Position position, Long id) {

        String prefix = switch (position) {
            case GENERAL_MANAGER -> "GM";
            case TELLER -> "TEL";
            case CUSTOMER_SERVICE -> "CSR";
        };

        return prefix + "-" + String.format("%04d", id);
    }
}