package com.tys.controller;

import com.tys.request.CreateSMSRequest;
import com.tys.service.VatanSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SMSController {
    private final VatanSmsService smsService;

    @PostMapping("/send-sms")
    public ResponseEntity<Void> sendSms(@RequestBody CreateSMSRequest request) {
        smsService.sendSms(request);
        return ResponseEntity.ok().build();
    }
}
