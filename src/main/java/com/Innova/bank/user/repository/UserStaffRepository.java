package com.Innova.bank.user.repository;

import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStaffRepository extends JpaRepository<UserStaff, Long> {

    boolean existsByIdNumber(String idNumber);
    Optional<UserStaff> findByIdNumber(String idNumber);
    Optional<UserStaff> findByEmployeeCode(String employeeCode);
    Optional<UserStaff> findByUser(User user);
}
