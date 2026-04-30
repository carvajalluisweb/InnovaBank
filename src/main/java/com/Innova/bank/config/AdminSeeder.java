package com.Innova.bank.config;

import com.Innova.bank.enums.Department;
import com.Innova.bank.enums.Gender;
import com.Innova.bank.enums.Position;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.enums.UserStatus;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserStaff;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserStaffRepository userStaffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail("admin@innovabank.com")) {
            return;
        }

        User adminUser = User.builder()
                .email("admin@innovabank.com")
                .password(passwordEncoder.encode("123456"))
                .role(Rol.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(adminUser);

        UserStaff adminStaff = UserStaff.builder()
                .idNumber("1718587486")
                .firstName("Luis")
                .lastName("Carvajal")
                .phoneNumber("0995911716")
                .age(30)
                .gender(Gender.MALE)
                .employeeCode("EMP-0001")
                .position(Position.GENERAL_MANAGER.name())
                .department(Department.GENERAL.name())
                .user(savedUser)
                .build();

        userStaffRepository.save(adminStaff);

        System.out.println(
                "ADMIN CREADO: admin@innovabank.com / 123456"
        );
    }
}