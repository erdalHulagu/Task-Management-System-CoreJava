package com.erdal.repository;

import com.erdal.model.Login;
import com.erdal.model.User;

public class LoginRepository {

    private final UserRepository userRepo = new UserRepository();

    /**
     * Email ve şifre kontrolü
     * Başarılı olursa User objesini döner
     */
    public User authenticate(Login login) {
        if (login == null) return null;
        return userRepo.login(login.getEmail(), login.getPassword());
    }
}
