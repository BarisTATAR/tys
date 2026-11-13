package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private Long companyId;
    private String companyName;
    private Integer number;
    private Boolean loaded;
    private Integer capacity;
    private Boolean seaView;
    private Integer floor;
}
