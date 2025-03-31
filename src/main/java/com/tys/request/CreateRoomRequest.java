package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {
    private Long companyId;
    private Integer number;
    private Boolean loaded;
    private Integer capacity;
    private Boolean seaView;
}
