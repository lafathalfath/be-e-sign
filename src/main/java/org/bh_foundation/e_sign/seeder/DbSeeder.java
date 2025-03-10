package org.bh_foundation.e_sign.seeder;

import org.bh_foundation.e_sign.models.Role;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DbSeeder {
    
    private final UserSeeder usersSeeder;

    private final UserRepository userRepository;

    public DbSeeder(
        UserSeeder usersSeeder,
        UserRepository userRepository
    ) {
        this.usersSeeder = usersSeeder;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void run() {
        if (userRepository.count() == 0) {
            usersSeeder.seed("Admin", "admin@gmail.com", "password", Role.ADMIN);
            usersSeeder.seed("User", "user@gmail.com", "password", Role.USER);
        }
    }

}
