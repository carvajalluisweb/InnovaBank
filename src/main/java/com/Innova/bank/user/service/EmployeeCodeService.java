package com.Innova.bank.user.service;

import com.Innova.bank.enums.Position;

public interface EmployeeCodeService {

    String generate(Position position, Long id);
}