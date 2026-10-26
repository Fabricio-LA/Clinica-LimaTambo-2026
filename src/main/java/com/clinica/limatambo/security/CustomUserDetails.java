package com.clinica.limatambo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private String nombreReal;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String nombreReal) {
        super(username, password, authorities);
        this.nombreReal = nombreReal;
    }

    public String getNombreReal() {
        return nombreReal;
    }

    public void setNombreReal(String nombreReal) {
        this.nombreReal = nombreReal;
    }
}
