package com.Innova.bank.user.validation;

import com.Innova.bank.enums.Position;
import com.Innova.bank.enums.Rol;
import com.Innova.bank.user.entity.User;
import com.Innova.bank.user.dto.UpdateUserRoleRequest;
import com.Innova.bank.user.dto.UpdateUserStatusRequest;

public interface ValidationService {

    void validatePasswords(String password, String confirmPassword);

    void validateInternalRole(Rol role);

    void validateRolePosition(Rol role, Position position);

    void validateRoleChange(User targetUser, UpdateUserRoleRequest request);

    void validateStatusChange(User targetUser, UpdateUserStatusRequest request);
}