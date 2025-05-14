package com.tys.request;

import com.tys.client.MusteriKimlikNoGirisTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsGuestCheckInRequest {
    private MusteriKimlikNoGirisTalep musteri;

}
