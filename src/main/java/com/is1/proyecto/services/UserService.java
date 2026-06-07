package com.is1.proyecto.services;

import com.is1.proyecto.dto.CreateUserRequest;
import com.is1.proyecto.models.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.set("userName", request.getUserName());
        user.set("password", BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.set("rol", request.getRol());

        if (request.getFirstName() != null) user.set("firstName", request.getFirstName());
        if (request.getLastName() != null) user.set("lastName", request.getLastName());
        if (request.getEmail() != null) user.set("email", request.getEmail());
        if (request.getDni() != null) user.set("dni", request.getDni());
        if (request.getAddress() != null) user.set("address", request.getAddress());
        if (request.getPhoneNumber() != null) user.set("phoneNumber", request.getPhoneNumber());

        user.saveIt();
        return user;
    }

    public User findByUsername(String username) {
        return User.findFirst("userName = ?", username);
    }

    public User findById(int id) {
        return User.findById(id);
    }

    public boolean usernameExists(String username) {
        return User.findFirst("userName = ?", username) != null;
    }

    public boolean emailExists(String email) {
        return User.findFirst("email = ?", email) != null;
    }

    public boolean dniExists(int dni) {
        return User.findFirst("dni = ?", dni) != null;
    }

}
