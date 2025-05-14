package com.tys.request;

import com.tys.client.MusteriKimlikNoCikisTalep;
import com.tys.client.MusteriYabanciCikisTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsForeignGuestCheckOutRequest {
    private MusteriYabanciCikisTalep musteri;
}
