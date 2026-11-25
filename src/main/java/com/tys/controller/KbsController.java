package com.tys.controller;

import com.tys.request.KbsParameterRequest;
import com.tys.response.KbsForeignGuestResponse;
import com.tys.response.KbsGuestResponse;
import com.tys.response.KbsParameterResponse;
import com.tys.security.CurrentActor;
import com.tys.service.KbsCredentialService;
import com.tys.service.KbsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Yalnız oturumdaki işletmenin kendi KBS tesis bilgileriyle salt okunur sorgular.
 * Misafir giriş/çıkış bildirimi rezervasyon akışında (check-in / check-out) yapılır;
 * keyfi TCKN ile doğrudan KBS'ye yazan uçlar bilinçli olarak sunulmaz.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kbs")
public class KbsController {

    private final KbsService kbsService;
    private final KbsCredentialService kbsCredentialService;
    private final CurrentActor currentActor;

    @GetMapping("/guestList")
    public ResponseEntity<List<KbsGuestResponse>> getKbsGuestList() {
        return ResponseEntity.ok(kbsService.kbsGuestList(credentials()));
    }

    @GetMapping("/foreignGuestList")
    public ResponseEntity<List<KbsForeignGuestResponse>> getKbsForeignGuestList() {
        return ResponseEntity.ok(kbsService.kbsForeignGuestList(credentials()));
    }

    @GetMapping("/parameterList")
    public ResponseEntity<KbsParameterResponse> getKbsParameterList(@ModelAttribute KbsParameterRequest request) {
        return ResponseEntity.ok(kbsService.kbsParameterList(credentials(), request));
    }

    private com.tys.service.KbsCredentials credentials() {
        Long companyId = currentActor.requireCompanyId();
        return kbsCredentialService.find(companyId)
                .orElseThrow(() -> new IllegalArgumentException(KbsCredentialService.NOT_CONFIGURED));
    }
}
