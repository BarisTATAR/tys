package com.tys.repository;

import com.tys.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUsername(String username);

    Optional<Company> findByName(String name);

    @Query("select c.name from Company c")
    List<String> findAllCompanyNames();
}
