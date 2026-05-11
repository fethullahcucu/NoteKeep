package com.example.UserRegistration.Security;

import com.example.UserRegistration.Repository.UserRepository;
import com.example.UserRegistration.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            logger.info("Loading user by email: {}", email);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            logger.info("User found: {}, roles: {}", email, user.getRoles());

            Set<SimpleGrantedAuthority> authorities;
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                logger.info("No roles found, assigning ROLE_USER");
                authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
            } else {
                authorities = user.getRoles().stream()
                    .filter(role -> role != null)
                    .map(role -> {
                        logger.info("Adding role: {}", role.name());
                        return new SimpleGrantedAuthority(role.name());
                    })
                    .collect(Collectors.toSet());
                if (authorities.isEmpty()) {
                    authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
                }
            }

            logger.info("User {} loaded with authorities: {}", email, authorities);
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
            );
        } catch (Exception e) {
            logger.error("Error loading user {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
}
