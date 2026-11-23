package com.tys.repository;

import com.tys.model.LoginLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLockRepository extends JpaRepository<LoginLock, String> {
}
