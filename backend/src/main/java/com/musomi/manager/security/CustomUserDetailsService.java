package com.musomi.manager.security;

import com.musomi.manager.entity.User;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findBySchoolIdAndUsername(1L, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Account is inactive");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
