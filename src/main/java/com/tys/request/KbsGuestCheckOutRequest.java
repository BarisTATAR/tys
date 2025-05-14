package com.tys.request;

import com.tys.client.MusteriKimlikNoCikisTalep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsGuestCheckOutRequest {
    private MusteriKimlikNoCikisTalep musteri;
}
