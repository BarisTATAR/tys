package com.tys.response;

import com.tys.client.Sonuc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbsForeignGuestUpdateCheckInResponse {
    private Sonuc sonuc;

}
