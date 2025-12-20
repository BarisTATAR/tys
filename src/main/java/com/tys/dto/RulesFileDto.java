package com.tys.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RulesFileDto {
    private Long id;
    private Long companyId; // Her apart için sadece 1 dosya
    private String fileName;
    private byte[] fileData;
    private String contentType;
    private LocalDateTime createdAt;
}
