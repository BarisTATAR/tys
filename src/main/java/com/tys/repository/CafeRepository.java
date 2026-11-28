package com.tys.repository;

import com.tys.model.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long> {

    List<Cafe> findAllByCompanyId(Long companyId);

    /** Menü: yalnız aktif (menüden kaldırılmamış) ürünler. */
    List<Cafe> findAllByCompanyIdAndActiveTrue(Long companyId);

    long countByCompanyId(Long companyId);
}
