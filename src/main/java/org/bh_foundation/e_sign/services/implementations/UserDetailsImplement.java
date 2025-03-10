package org.bh_foundation.e_sign.services.implementations;

import org.bh_foundation.e_sign.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImplement implements UserDetailsService {
    
    private final UserRepository userRepository;

    public UserDetailsImplement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
