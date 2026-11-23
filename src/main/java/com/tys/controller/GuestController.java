package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import com.tys.security.CurrentActor;
import com.tys.service.ExcelImportService;
import com.tys.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest")
public class GuestController {

    private static final Logger log = LoggerFactory.getLogger(GuestController.class);

    private final GuestService guestService;
    private final CurrentActor currentActor;

    @Autowired
    private ExcelImportService excelImportService;

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createGuest(@RequestBody CreateGuestRequest request) {
        Long companyId = getCurrentCompanyIdFromAuth();
        guestService.createGuest(request, companyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteGuest(@RequestBody DeleteGuestRequest request) {
        guestService.deleteGuest(request, getCurrentCompanyIdFromAuth());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateGuest(@RequestBody UpdateGuestRequest request) {
        guestService.updateGuest(request, getCurrentCompanyIdFromAuth());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{identityNumber}")
    public ResponseEntity<GuestDto> getGuestByIdentityNumber(@PathVariable("identityNumber") String identityNumber) {
        return ResponseEntity.ok(guestService.getGuestByIdentityNumber(identityNumber, getCurrentCompanyIdFromAuth()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GuestDto>> getAllGuest() {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(guestService.getAllGuest(companyId));
    }

    @GetMapping("sms/all")
    public ResponseEntity<List<String>> getAllGuestForSMS() {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(guestService.getAllGuestForSMS(companyId));
    }

    @GetMapping("/year/all/{year}")
    public ResponseEntity<List<String>> getAllGuestInYear(@PathVariable("year") int year) {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(guestService.getAllGuestInYear(year, companyId));
    }

    @GetMapping("/day/all/{day}")
    public ResponseEntity<List<String>> getAllGuestForDay(@PathVariable("day") int day) {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(guestService.getAllGuestForDay(day, companyId));
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();

            excelImportService.importGuestsFromExcel(file, companyId);
            return ResponseEntity.ok("Excel başarıyla içeri aktarıldı!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Hata: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excel import failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Excel dosyası işlenemedi.");
        }
    }
}