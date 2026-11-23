package com.tys.response;

import com.tys.client.MusteriKimlikNoGirisSonuc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbsGuestCheckInResponse {
    private MusteriKimlikNoGirisSonuc musteri;
}
