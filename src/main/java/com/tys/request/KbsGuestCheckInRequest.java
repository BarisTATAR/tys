package com.tys.request;

import com.tys.client.MusteriKimlikNoGirisTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbsGuestCheckInRequest {
    private MusteriKimlikNoGirisTalep musteri;
}
