package com.tys.controller;

import com.tys.request.KbsGuestCheckInRequest;
import com.tys.request.KbsGuestCheckOutRequest;
import com.tys.request.KbsGuestUpdateCheckInRequest;
import com.tys.request.KbsParameterRequest;
import com.tys.response.*;
import com.tys.service.KbsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kbs")
public class KbsController {

    private final KbsService kbsService;

    @GetMapping("/guestList")
    public ResponseEntity<List<KbsGuestResponse>> getKbsGuestList() {
        return ResponseEntity.ok(kbsService.kbsGuestList());
    }

    @GetMapping("/parameterList")
    public ResponseEntity<KbsParameterResponse> getKbsParameterList(@ModelAttribute KbsParameterRequest request) {
        return ResponseEntity.ok(kbsService.kbsParameterList(request));
    }

    @PostMapping("/checkInGuest")
    public ResponseEntity<KbsGuestCheckInResponse> checkInGuest(@ModelAttribute KbsGuestCheckInRequest request) {
        return ResponseEntity.ok(kbsService.checkInGuest(request));

    }

    @PostMapping("/checkOutGuest")
    public ResponseEntity<KbsGuestCheckOutResponse> checkOutGuest(@ModelAttribute KbsGuestCheckOutRequest request) {
        return ResponseEntity.ok(kbsService.checkOutGuest(request));
    }

    @PostMapping("/updateCheckInGuest")
    public ResponseEntity<KbsGuestUpdateCheckInResponse> updateCheckInGuest(@ModelAttribute KbsGuestUpdateCheckInRequest request) {
        return ResponseEntity.ok(kbsService.updateCheckInGuest(request));
    }
}
