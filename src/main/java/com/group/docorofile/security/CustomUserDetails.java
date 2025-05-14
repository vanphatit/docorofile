package com.group.docorofile.security;

import com.group.docorofile.entities.AdminEntity;
import com.group.docorofile.entities.MemberEntity;
import com.group.docorofile.entities.ModeratorEntity;
import com.group.docorofile.entities.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user instanceof AdminEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user instanceof ModeratorEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        } else if (user instanceof MemberEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isActive();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public UserEntity getUser() {
        return user;
    }
}