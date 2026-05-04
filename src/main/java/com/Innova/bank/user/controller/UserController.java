package com.Innova.bank.user.controller;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.response.ApiResponse;
import com.Innova.bank.common.response.ResponseFactory;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;
import com.Innova.bank.user.dto.UserResponse;
import com.Innova.bank.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResponseFactory responseFactory;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ActualSessionResponse>> getMyProfile() {
        ActualSessionResponse response = userService.getMyProfile();

        return responseFactory.ok("Perfil obtenido correctamente", response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ActualSessionResponse>> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request, HttpServletRequest httpRequest) {
        ActualSessionResponse response = userService.updateMyProfile(request, httpRequest);

        return responseFactory.ok("Perfil actualizado correctamente", response);
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllStaff() {

        List<UserResponse> response = userService.getAllStaff();

        return responseFactory.ok("Personal obtenido correctamente", response);
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllCustomers() {

        List<UserResponse> response = userService.getAllCustomers();

        return responseFactory.ok("Clientes obtenidos correctamente", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);

        return responseFactory.ok("Usuario obtenido correctamente", response);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request, HttpServletRequest httpRequest) {
        UserResponse response = userService.updateUserRole(id, request, httpRequest);

        return responseFactory.ok("Rol actualizado correctamente", response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request, HttpServletRequest httpRequest) {
        UserResponse response = userService.updateUserStatus(id, request, httpRequest);

        return responseFactory.ok("Estado actualizado correctamente", response);
    }
}