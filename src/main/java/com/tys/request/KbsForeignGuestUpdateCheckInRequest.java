package com.tys.request;

import com.tys.client.MusteriKimlikNoGuncelleTalep;
import com.tys.client.MusteriYabanciGuncelleTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsForeignGuestUpdateCheckInRequest {
    private MusteriYabanciGuncelleTalep musteri;
}
