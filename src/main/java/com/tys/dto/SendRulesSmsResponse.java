package com.tys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendRulesSmsResponse {
    private String message;
    private String approvalUrl;
    private String token;
    private Long reservationId;
}
