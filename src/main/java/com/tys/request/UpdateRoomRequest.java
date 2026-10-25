package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {
    private Long id;
    private Integer number;
    private Boolean loaded;
    private Integer capacity;
    private Boolean seaView;
    private Integer floor;
}
