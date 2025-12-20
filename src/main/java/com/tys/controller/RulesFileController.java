package com.tys.controller;
import com.tys.dto.RulesFileDto;
import com.tys.service.RulesFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rules-file")
@CrossOrigin(origins = "http://localhost:3000")
public class RulesFileController {
    private final RulesFileService rulesFileService;

    public RulesFileController(RulesFileService service) {
        this.rulesFileService = service;
    }

    @PostMapping("/{companyId}/rules/docx")
    public ResponseEntity<String> upload(@PathVariable Long companyId,
                                         @RequestParam MultipartFile file) throws Exception {
        rulesFileService.uploadDocx(companyId, file);
        return ResponseEntity.ok("DOCX dosyası yüklendi");
    }

    @GetMapping("/{companyId}/rules/docx")
    public ResponseEntity<byte[]> download(@PathVariable Long companyId) {
        RulesFileDto file = rulesFileService.downloadDocx(companyId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getFileData());
    }
}
