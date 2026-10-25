package com.tys.request;

import com.tys.client.MusteriKimlikNoCikisTalep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbsGuestCheckOutRequest {
    private MusteriKimlikNoCikisTalep musteri;
}
