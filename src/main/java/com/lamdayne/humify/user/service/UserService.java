package com.lamdayne.humify.user.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.user.dto.request.ChangePasswordRequest;
import com.lamdayne.humify.user.dto.request.ChangeRoleRequest;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.entity.User;

import java.util.Optional;

public interface UserService {

    boolean existsByEmail(String email);

    void save(User user);

    User findByEmail(String email);

    UserResponse create(CreateUserRequest request);

    PageResponse<UserResponse> findAll(int page, int size, String... sorts);

    void changeRole(Long id, ChangeRoleRequest request);

    UserResponse findById(Long id);

    void changePassword(Long id, ChangePasswordRequest request, UserPrincipal userPrincipal);

    void resetPassword(Long id, String newPassword);

    Optional<User> findByEmailAndCompanyId(String email, Long companyId);

    User createUser(String email, Company company);

    User getUserById(Long id);

}
