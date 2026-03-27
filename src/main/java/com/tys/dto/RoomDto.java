package com.tys.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomDto {
    private Long id;

    private Integer number;

    private Boolean loaded;

    private Integer capacity;

    private Boolean seaView;

    private Integer floor;

    private Long companyId;

    private String companyName;

    private List<ReservationDto> reservations;

    //private Long reservationId;
}