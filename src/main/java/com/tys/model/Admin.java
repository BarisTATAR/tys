package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true, nullable = false)      //işletme sahibine verilecek kullanıcı adı
    private String username;

    @Column(nullable = false)      //işletme sahibine verilecek şifre
    private String password;
}
