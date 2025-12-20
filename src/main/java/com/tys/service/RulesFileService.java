package com.tys.service;
import com.tys.dto.RulesFileDto;
import com.tys.mapper.RulesFileMapper;
import com.tys.model.RulesFile;
import com.tys.repository.RulesFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class RulesFileService {
    private final RulesFileRepository rulesFileRepository;
    private final RulesFileMapper rulesFileMapper;

    public RulesFileService(RulesFileRepository repository, RulesFileMapper mapper) {
        this.rulesFileRepository = repository;
        this.rulesFileMapper = mapper;
    }

    // DOCX Upload
    public RulesFileDto uploadDocx(Long companyId, MultipartFile file) throws IOException {
        RulesFile entity = rulesFileRepository.findByCompanyId(companyId)
                .orElse(new RulesFile());

        entity.setCompanyId(companyId);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileData(file.getBytes());
        entity.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        entity.setCreatedAt(LocalDateTime.now());

        rulesFileRepository.save(entity);

        return rulesFileMapper.toDto(entity);
    }

    // DOCX Download
    public RulesFileDto downloadDocx(Long companyId) {
        RulesFile entity = rulesFileRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("Dosya bulunamadı"));

        return rulesFileMapper.toDto(entity);
    }
}
