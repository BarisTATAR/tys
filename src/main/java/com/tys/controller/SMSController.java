package com.tys.controller;

import com.tys.request.CreateSMSRequest;
import com.tys.security.CurrentActor;
import com.tys.service.GuestService;
import com.tys.service.VatanSmsService;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SMSController {
    private final VatanSmsService smsService;
    private final GuestService guestService;
    private final CurrentActor currentActor;

    @PostMapping("/send-sms")
    public ResponseEntity<?> sendSms(@RequestBody CreateSMSRequest request) {
        // Doğrulama hataları 400; sağlayıcı yapılandırması/erişimi 503/502 (ApiExceptionHandler)
        if (request == null) {
            throw new IllegalArgumentException("SMS bilgileri gereklidir.");
        }
        Long companyId = currentActor.requireCompanyId();
        guestService.assertPhonesBelongToCompany(request.getPhoneList(), companyId);
        InputRules.plainText(request.getSenderName(), "Gönderen", 30, false);
        InputRules.plainText(request.getMessage(), "Mesaj", InputRules.MAX_SMS, true);
        smsService.sendSms(request);
        return ResponseEntity.ok().build();
    }
}
