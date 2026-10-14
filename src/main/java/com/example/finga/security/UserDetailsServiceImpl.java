package com.example.finga.security;

import com.example.finga.user.User;
import com.example.finga.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {

        User user = userRepository.findByUsername(login)
                .orElseGet(() ->
                        userRepository.findByEmail(login)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException(
                                                "Username oder E-Mail nicht gefunden"
                                        )
                                )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}