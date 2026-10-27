package com.example.multitenant.security;

import com.example.multitenant.entity.User;
import com.example.multitenant.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.contains(":")) {
            String[] parts = username.split(":", 2);
            String email = parts[0];
            String tenantSlug = parts[1];
            User user = userRepository.findByEmailAndTenantSlug(email, tenantSlug)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + email + " and tenant: " + tenantSlug));
            return UserPrincipal.create(user);
        }

        List<User> users = userRepository.findAllByEmailWithTenant(username);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        if (users.size() > 1) {
            throw new UsernameNotFoundException(
                    "Multiple accounts found with email: " + username + ". Please specify tenant slug.");
        }
        return UserPrincipal.create(users.get(0));
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long id) {
        User user = userRepository.findByIdWithTenant(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
