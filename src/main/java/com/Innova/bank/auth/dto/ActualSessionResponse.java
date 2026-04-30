package com.Innova.bank.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActualSessionResponse {

    private Long userId;

    private String email;

    private String role;

    private String status;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String idNumber;

    private String gender;

    private Integer age;

    private String position;

    private String department;
}