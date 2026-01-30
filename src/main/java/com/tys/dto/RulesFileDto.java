package com.tys.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RulesFileDto {
    private Long id;
    private Long companyId;
    private String rulesText;
    private String fileName;
    private String contentType;
    private LocalDateTime createdAt;
}
