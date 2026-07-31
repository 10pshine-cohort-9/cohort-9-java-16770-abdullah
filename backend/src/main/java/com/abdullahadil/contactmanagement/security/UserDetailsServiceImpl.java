package com.abdullahadil.contactmanagement.security;

import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Looks up the authenticated principal by user id (the JWT subject).
 * Login itself (matching an email/phone + password) happens in
 * AuthService, not here, since a user can log in with either identifier.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UsernameNotFoundException("No user with id " + userId));
        return new UserPrincipal(user);
    }
}
