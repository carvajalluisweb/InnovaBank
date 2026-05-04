package com.Innova.bank.user.service;

import com.Innova.bank.auth.dto.ActualSessionResponse;
import com.Innova.bank.user.dto.UpdateMyProfileRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface UserProfileService {

    ActualSessionResponse getMyProfile();

    ActualSessionResponse updateMyProfile(UpdateMyProfileRequest request, HttpServletRequest httpRequest);
}