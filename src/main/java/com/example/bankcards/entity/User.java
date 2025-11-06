package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private Role role;
    private Set<Role> roles;

    public User(String username, String password, String email, Role role) {

        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }
}
