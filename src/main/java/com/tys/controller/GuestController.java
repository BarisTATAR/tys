package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import com.tys.service.ExcelImportService;
import com.tys.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest")
@CrossOrigin(origins = "http://localhost:3000")
public class GuestController {

    private final GuestService guestService;

    @Autowired
    private ExcelImportService excelImportService;
    @PostMapping("/create")
    public ResponseEntity<Void> createGuest(@RequestBody CreateGuestRequest request) {
        guestService.createGuest(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteGuest(@RequestBody DeleteGuestRequest request) {
        guestService.deleteGuest(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateGuest(@RequestBody UpdateGuestRequest request) {
        guestService.updateGuest(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{identityNumber}")
    public ResponseEntity<GuestDto> getGuestByIdentityNumber(@PathVariable("identityNumber") String identityNumber) {
        return ResponseEntity.ok(guestService.getGuestByIdentityNumber(identityNumber));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GuestDto>> getAllGuest() {
        return ResponseEntity.ok(guestService.getAllGuest());
    }

    @GetMapping("sms/all")
    public ResponseEntity<List<String>> getAllGuestForSMS() {
        return ResponseEntity.ok(guestService.getAllGuestForSMS());
    }

    @GetMapping("/year/all/{year}")
    public ResponseEntity<List<String>> getAllGuestInYear(@PathVariable("year") int year) {
        return ResponseEntity.ok(guestService.getAllGuestInYear(year));
    }

    @GetMapping("/day/all/{day}")
    public ResponseEntity<List<String>> getAllGuestForDay(@PathVariable("day") int day) {
        return ResponseEntity.ok(guestService.getAllGuestForDay(day));
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importGuestsFromExcel(file);
            return ResponseEntity.ok("Excel başarıyla içeri aktarıldı!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }
}