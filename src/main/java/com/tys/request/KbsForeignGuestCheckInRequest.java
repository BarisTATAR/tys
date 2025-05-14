package com.tys.request;

import com.tys.client.MusteriKimlikNoGirisTalep;
import com.tys.client.MusteriYabanciGirisTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsForeignGuestCheckInRequest {
    private MusteriYabanciGirisTalep musteri;

}
