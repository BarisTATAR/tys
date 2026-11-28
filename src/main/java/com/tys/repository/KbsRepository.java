package com.tys.repository;

import com.tys.model.Kbs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KbsRepository extends JpaRepository<Kbs, Long> {
    Optional<Kbs> findByCompany_Id(Long companyId);
}
