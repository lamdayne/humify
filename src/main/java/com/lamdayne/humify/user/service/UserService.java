package com.lamdayne.humify.user.service;

import com.lamdayne.humify.user.entity.User;

public interface UserService {

    boolean existsByEmail(String email);

    void save(User user);

    User findByEmail(String email);

}
