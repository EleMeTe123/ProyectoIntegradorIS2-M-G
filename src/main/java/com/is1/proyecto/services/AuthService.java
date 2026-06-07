package com.is1.proyecto.services;

import com.is1.proyecto.dto.LoginRequest;
import com.is1.proyecto.models.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    public User authenticate(LoginRequest request) {
        User user = User.findFirst("userName = ?", request.getUsername());
        if (user == null) return null;

        String storedHash = user.getString("password");
        if (BCrypt.checkpw(request.getPassword(), storedHash)) {
            return user;
        }
        return null;
    }

    public boolean isAdmin(User user) {
        String rol = user.getString("rol");
        return "ADMIN".equals(rol);
    }

    public boolean isProfessor(User user) {
        String rol = user.getString("rol");
        return "PROFESSOR".equals(rol);
    }
}
