package com.tys.repository;

import com.tys.model.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long> {

    List<Cafe> findAllByCompanyId(Long companyId);
}
