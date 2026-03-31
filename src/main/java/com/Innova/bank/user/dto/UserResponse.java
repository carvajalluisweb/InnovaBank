package com.Innova.bank.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String idNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private String gender;
    private String role;
    private String status;
}