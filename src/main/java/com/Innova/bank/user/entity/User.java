package com.Innova.bank.user.entity;

import com.Innova.bank.enums.Gender;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
import jakarta.persistence.*;
import jdk.jshell.Snippet;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_number" , nullable = false, length = 10)
    private  String idNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

}
