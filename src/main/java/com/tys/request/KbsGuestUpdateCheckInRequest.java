package com.tys.request;

import com.tys.client.MusteriKimlikNoGuncelleTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbsGuestUpdateCheckInRequest {
    private MusteriKimlikNoGuncelleTalep musteri;
}
