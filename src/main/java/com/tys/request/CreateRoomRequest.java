package com.tys.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {

    private Integer number;
    private Boolean loaded;
    private Integer capacity;
    private Boolean seaView;
}
