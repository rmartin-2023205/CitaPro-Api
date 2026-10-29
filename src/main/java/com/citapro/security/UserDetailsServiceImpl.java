package com.citapro.security;

import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
            .map(ur -> new SimpleGrantedAuthority(ur.getRole().name()))
            .toList();

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            user.isActive(),
            true,
            true,
            true,
            authorities
        );
    }
}

