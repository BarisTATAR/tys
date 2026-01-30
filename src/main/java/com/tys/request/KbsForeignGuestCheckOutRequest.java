package com.tys.request;

import com.tys.client.MusteriKimlikNoCikisTalep;
import com.tys.client.MusteriYabanciCikisTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbsForeignGuestCheckOutRequest {
    private MusteriYabanciCikisTalep musteri;
}
