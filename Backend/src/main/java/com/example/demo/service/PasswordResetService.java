package com.example.demo.service;

import com.example.demo.dto.ForgotPasswordRequestDTO;
import com.example.demo.dto.ResetPasswordRequestDTO;

public interface PasswordResetService {
    void requestReset(ForgotPasswordRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
}
