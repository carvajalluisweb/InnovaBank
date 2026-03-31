package com.Innova.bank.user.controller;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.common.response.ApiResponse;
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ActualSessionResponse>> getMyProfile() {
        ActualSessionResponse response = userService.getMyProfile();

        return ResponseEntity.ok(
                ApiResponse.<ActualSessionResponse>builder()
                        .success(true)
                        .message("Perfil obtenido correctamente")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ActualSessionResponse>> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        ActualSessionResponse response = userService.updateMyProfile(request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<ActualSessionResponse>builder()
                        .success(true)
                        .message("Perfil actualizado correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .success(true)
                        .message("Usuarios obtenidos correctamente")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Usuario obtenido correctamente")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        UserResponse response = userService.updateUserRole(id, request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Rol actualizado correctamente")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        UserResponse response = userService.updateUserStatus(id, request, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Estado actualizado correctamente")
                        .data(response)
                        .build()
        );
    }
}