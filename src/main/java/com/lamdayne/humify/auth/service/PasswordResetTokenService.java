package com.lamdayne.humify.auth.service;

public interface PasswordResetTokenService {

    void setPasswordNewAccount(String email, Long userId, String name);

}
