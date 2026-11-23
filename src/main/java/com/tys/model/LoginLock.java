package com.tys.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "login_lock")
public class LoginLock {

    @Id
    @Column(name = "username", length = 120, nullable = false)
    private String username;

    @Column(name = "failures", nullable = false)
    private int failures;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
}
