package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCafeRequest {
    private Long id;
    private Long companyId;
    private String name;
    private double price;
}
