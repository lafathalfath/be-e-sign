package org.bh_foundation.e_sign.seeder;

import java.time.LocalDateTime;

import org.bh_foundation.e_sign.models.Role;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserSeeder {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User seed(String name, String email, String password, Role role) {
        User admin = new User();
        admin.setUsername(name);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setVerifiedAt(LocalDateTime.now());
        userRepository.save(admin);
        System.out.println("--- SEEDER: User Seeding Successfully !! ------------------------------");
        return admin;
    }

}
