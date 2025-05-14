package com.tys.controller;

import com.tys.request.*;
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

    @GetMapping("/foreignGuestList")
    public ResponseEntity<List<KbsForeignGuestResponse>> getKbsForeignGuestList() {
        return ResponseEntity.ok(kbsService.kbsForeignGuestList());
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

    @PostMapping("/checkInForeignGuest")
    public ResponseEntity<KbsForeignGuestCheckInResponse> checkInGuest(@ModelAttribute KbsForeignGuestCheckInRequest request) {
        return ResponseEntity.ok(kbsService.checkInForeignGuest(request));

    }

    @PostMapping("/checkOutForeignGuest")
    public ResponseEntity<KbsForeignGuestCheckOutResponse> checkOutGuest(@ModelAttribute KbsForeignGuestCheckOutRequest request) {
        return ResponseEntity.ok(kbsService.checkOutForeignGuest(request));
    }

    @PostMapping("/updateForeignCheckInGuest")
    public ResponseEntity<KbsForeignGuestUpdateCheckInResponse> updateCheckInGuest(@ModelAttribute KbsForeignGuestUpdateCheckInRequest request) {
        return ResponseEntity.ok(kbsService.updateForeignCheckInGuest(request));
    }

}
