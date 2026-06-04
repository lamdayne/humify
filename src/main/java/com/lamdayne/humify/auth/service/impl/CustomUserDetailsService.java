package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserHasRoleRepository userHasRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByEmail(username);

        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;

        List<String> permissions = userHasRoleRepository.findPermissionNameByUserIdAndCompanyId(user.getId(), companyId);

        return UserPrincipal.builder()
                .id(user.getId())
                .companyId(companyId)
                .email(user.getEmail())
                .password(user.getPassword())
                .active(user.getActive())
                .authorities(permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
