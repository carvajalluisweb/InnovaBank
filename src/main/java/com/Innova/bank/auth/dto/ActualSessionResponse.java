package com.Innova.bank.auth.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualSessionResponse {
    private Long id;
    private String idNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private String gender;
    private String role;
}
