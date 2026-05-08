package com.Innova.bank.common.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private final Random random = new Random();

    public String generate() {

        return String.valueOf(1000000000L + Math.abs(random.nextLong()) % 9000000000L);
    }
}