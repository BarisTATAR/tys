package com.tys.request;

import com.tys.client.MusteriKimlikNoGirisTalep;
import com.tys.client.MusteriYabanciGirisTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbsForeignGuestCheckInRequest {
    private MusteriYabanciGirisTalep musteri;
}
