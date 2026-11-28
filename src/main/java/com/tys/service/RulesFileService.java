package com.tys.service;
import com.tys.dto.RulesFileDto;
import com.tys.mapper.RulesFileMapper;
import com.tys.model.RulesFile;
import com.tys.repository.RulesFileRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RulesFileService {
    @Autowired
    private RulesFileRepository repository;

    @Autowired
    private RulesFileMapper mapper;

    @Transactional
    public RulesFileDto uploadWord(Long companyId, MultipartFile file) throws IOException {
        String fileName = file == null || file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        if (file == null || file.isEmpty() || !fileName.toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("Yalnız Word (.docx) dosyası yüklenebilir.");
        }
        // Word dosyasını text’e çevir
        StringBuilder text = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph para : doc.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }
        } catch (org.apache.poi.UnsupportedFileFormatException | org.apache.poi.EmptyFileException
                 | org.apache.poi.ooxml.POIXMLException ex) {
            throw new IllegalArgumentException("Dosya okunamadı; geçerli bir Word (.docx) dosyası yükleyin.");
        }
        if (text.toString().isBlank()) {
            throw new IllegalArgumentException("Word dosyasında kural metni bulunamadı.");
        }

        // DB kaydı
        RulesFile rulesFile = repository.findByCompanyId(companyId)
                .orElse(new RulesFile());
        rulesFile.setCompanyId(companyId);
        rulesFile.setFileData(file.getBytes());
        rulesFile.setRulesText(text.toString());
        rulesFile.setFileName(fileName.length() > 255 ? fileName.substring(0, 255) : fileName);
        rulesFile.setContentType(file.getContentType());
        rulesFile.setCreatedAt(LocalDateTime.now());

        RulesFile saved = repository.save(rulesFile);

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<RulesFileDto> getByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId)
                .map(mapper::toDto);
    }
}
