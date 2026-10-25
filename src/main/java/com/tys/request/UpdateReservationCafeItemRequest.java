package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationCafeItemRequest {
    /** reservation_cafe_item.id — varsa buna göre güncellenir */
    private Long id;
    private Long reservationId;
    private Long cafeId;
    /** Yeni adet. 0 veya null → satır silinir */
    private Integer count;
}
