package com.tys.dto;

import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private Integer number;
    private Boolean loaded;
    private Integer capacity;
    private Boolean seaView;

    private Long companyId;
    private String companyName;

    //private Long reservationId;
}