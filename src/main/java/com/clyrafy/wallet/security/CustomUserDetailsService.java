package com.clyrafy.wallet.security;

import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        Optional<User> userOpt = Optional.empty();

        // Try parsing as UUID first (used in JWT principal)
        try {
            UUID userId = UUID.fromString(input);
            userOpt = userRepository.findById(userId);
        } catch (IllegalArgumentException ignored) {
            // input is not a UUID, fallback to email/username/phone
        }

        // If not found by UUID, try email/username/phone
        User user = userOpt.orElseGet(() -> userRepository
                .findByEmailIgnoreCaseOrUserNameIgnoreCaseOrPhoneNumber(input, input, input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with: " + input)));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString()) // always use UUID as principal
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
