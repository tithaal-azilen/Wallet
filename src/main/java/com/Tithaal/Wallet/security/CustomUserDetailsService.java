package com.Tithaal.Wallet.security;

import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        if (user.getStatus() == com.Tithaal.Wallet.entity.UserStatus.INACTIVE) {
            throw new org.springframework.security.authentication.DisabledException("User account is inactive");
        }

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities
                    .add(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()));
        }

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities);
    }
}
