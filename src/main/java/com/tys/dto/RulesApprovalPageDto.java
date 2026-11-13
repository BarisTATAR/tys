package com.tys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RulesApprovalPageDto {
    private Long reservationId;
    private String companyName;
    private String guestName;
    private String rulesText;
    private boolean approved;
    private LocalDateTime approvedAt;
}
