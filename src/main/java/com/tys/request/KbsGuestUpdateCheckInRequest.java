package com.tys.request;

import com.tys.client.MusteriKimlikNoGuncelleTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsGuestUpdateCheckInRequest {
    private MusteriKimlikNoGuncelleTalep musteri;
}
