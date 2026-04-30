package com.Innova.bank.user.entity;

import com.Innova.bank.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_number", nullable = false, unique = true, length = 10)
    private String idNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
