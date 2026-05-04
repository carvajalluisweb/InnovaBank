package com.Innova.bank.auth.service.impl;

import com.Innova.bank.auth.dto.RegisterCustomerRequest;
import com.Innova.bank.auth.dto.RegisterStaffRequest;
import com.Innova.bank.auth.service.RegistrationService;
import com.Innova.bank.common.audit.AuditFacade;
import com.Innova.bank.common.audit.AuditMessageFactory;
import com.Innova.bank.common.exception.BadRequestException;
import com.Innova.bank.common.security.AuthorizationService;
import com.Innova.bank.common.security.CurrentUserService;
import com.Innova.bank.enums.*;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.entity.UserCustomer;
import com.Innova.bank.user.entity.UserStaff;
import com.Innova.bank.user.repository.UserCustomerRepository;
import com.Innova.bank.user.repository.UserRepository;
import com.Innova.bank.user.repository.UserStaffRepository;
import com.Innova.bank.user.service.EmployeeCodeService;
import com.Innova.bank.user.validation.IdNumberValidator;
import com.Innova.bank.user.validation.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final UserStaffRepository userStaffRepository;

    private final PasswordEncoder passwordEncoder;

    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final AuditFacade auditFacade;

    private final ValidationService validationService;
    private final IdNumberValidator idNumberValidator;
    private final EmployeeCodeService employeeCodeService;
    private final AuditMessageFactory auditMessageFactory;

    @Override
    public void registerCustomer(RegisterCustomerRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {

            authorizationService.requireOperatorOrAdmin(authenticatedUser);


            validationService.validatePasswords(request.getPassword(),request.getConfirmPassword());

            validateEmailNotExists(request.getEmail());

            idNumberValidator.validateCustomer(request.getIdNumber());

            String encodedPassword = passwordEncoder.encode(request.getPassword());

            User user = User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role(Rol.ROLE_USER)
                    .status(UserStatus.ACTIVE)
                    .failedAttempts(0)
                    .build();

            User savedUser = userRepository.save(user);

            UserCustomer customer = UserCustomer.builder()
                            .idNumber(request.getIdNumber())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .phoneNumber(request.getPhoneNumber())
                            .age(request.getAge())
                            .gender(request.getGender())
                            .user(savedUser)
                            .build();

            userCustomerRepository.save(customer);

            auditFacade.success(authenticatedUser.getId(),authenticatedUser.getEmail(), AuditAction.REGISTER_CUSTOMER,
                    auditMessageFactory.customerRegistered(request.getEmail()), httpRequest );

        } catch (RuntimeException exception) {

            auditFacade.failed(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.REGISTER_CUSTOMER,
                        auditMessageFactory.customerRegisterFailed(request.getEmail(),exception), httpRequest);
            throw exception;
        }
    }

    @Override
    public void registerStaff(RegisterStaffRequest request, HttpServletRequest httpRequest) {

        User authenticatedUser = currentUserService.getCurrentUser();

        try {
            authorizationService.requireAdmin(authenticatedUser);

            validationService.validatePasswords(request.getPassword(), request.getConfirmPassword());

            validateEmailNotExists(request.getEmail());

            validationService.validateInternalRole(request.getRole());

            validationService.validateRolePosition(request.getRole(), request.getPosition());

            idNumberValidator.validateStaff(request.getIdNumber());

            String encodedPassword = passwordEncoder.encode(request.getPassword());

            User user = User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role(request.getRole())
                    .failedAttempts(0)
                    .status(UserStatus.ACTIVE)
                    .build();

            User savedUser = userRepository.save(user);

            UserStaff staff = UserStaff.builder()
                    .idNumber(request.getIdNumber())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .age(request.getAge())
                    .gender(request.getGender())
                    .position(request.getPosition().name())
                    .department(Department.OPERATIONS.name())
                    .user(savedUser)
                    .build();

            UserStaff savedStaff = userStaffRepository.save(staff);

            savedStaff.setEmployeeCode(employeeCodeService.generate(request.getPosition(), savedStaff.getId()));

            userStaffRepository.save(savedStaff);

            auditFacade.success(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.REGISTER_STAFF,
                    auditMessageFactory.staffRegistered(request.getEmail()), httpRequest);

        }catch (Exception exception){
            auditFacade.failed(authenticatedUser.getId(), authenticatedUser.getEmail(), AuditAction.REGISTER_STAFF,
                    auditMessageFactory.staffRegisterFailed(exception), httpRequest);

            throw exception;
        }


    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(
                    "El correo ya está registrado"
            );
        }
    }
}