package com.tys.repository;
import com.tys.model.RulesFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RulesFileRepository extends JpaRepository<RulesFile, Long> {
    Optional<RulesFile> findByCompanyId(Long companyId);

}
