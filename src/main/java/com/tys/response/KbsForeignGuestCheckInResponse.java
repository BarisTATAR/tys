package com.tys.response;

import com.tys.client.MusteriKimlikNoGirisSonuc;
import com.tys.client.MusteriYabanciGirisSonuc;
import com.tys.client.MusteriYabanciGirisTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbsForeignGuestCheckInResponse {
    private MusteriYabanciGirisSonuc musteri;
}
