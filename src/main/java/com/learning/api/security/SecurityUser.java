package com.learning.api.security;

import com.learning.api.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = switch (user.getRole()) {
            case 1 -> "ROLE_STUDENT";
            case 2 -> "ROLE_TEACHER";
            case 3 -> "ROLE_ADMIN";
            default -> null;
        };
        return roleName != null ? List.of(new SimpleGrantedAuthority(roleName)) : List.of();
    }
}
