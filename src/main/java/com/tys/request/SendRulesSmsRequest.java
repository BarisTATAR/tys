package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendRulesSmsRequest {
    /** İlişkilendirilecek rezervasyon (zorunlu) */
    private Long reservationId;
    /** Alıcı telefon (boşsa rezervasyondaki iletişim misafirinden alınır) */
    private String phoneNumber;
    /** Opsiyonel misafir adı (boşsa iletişim misafirinden alınır) */
    private String guestName;
    /** Boşsa sistem varsayılan onay mesajını üretir */
    private String message;
}
