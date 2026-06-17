package com.lamdayne.humify.user.service.impl;

import com.lamdayne.humify.auth.enums.SystemRole;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.user.dto.request.ChangePasswordRequest;
import com.lamdayne.humify.user.dto.request.ChangeRoleRequest;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.mapper.UserMapper;
import com.lamdayne.humify.user.repository.UserRepository;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleAccessService roleAccessService;
    private final CompanyAccessService companyAccessService;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EMAIL_EXISTED);
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCompany(companyAccessService.getReferenceById(userPrincipal.getCompanyId()));
        user = userRepository.save(user);
        roleAccessService.assignRoles(user, request.getRoleIds());
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> findAll(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<User> users = userRepository.findAll(pageable);

        List<UserResponse> userResponses = users.getContent().stream()
                .filter(u -> u.getCompany() != null)
                .map(userMapper::toResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .items(userResponses)
                .build();
    }

    @Override
    @Transactional
    public void changeRole(Long id, ChangeRoleRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        roleAccessService.assignRoles(user, request.getRoleIds());
    }

    @Override
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getCompany() == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, UserPrincipal userPrincipal) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Set<String> roleNames = roleAccessService.findAllRoleNames(userPrincipal);
        if (!userPrincipal.getId().equals(user.getId())
                && !roleNames.contains(SystemRole.COMPANY_ADMIN.name())
                && !roleNames.contains(SystemRole.HR_MANAGER.name())
        ) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.USER_PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
