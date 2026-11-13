package com.tys.controller;

import com.tys.dto.RulesApprovalPageDto;
import com.tys.service.RulesSmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Misafirlerin giriş yapmadan kuralları görüp onayladığı herkese açık API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/rules")
public class PublicRulesController {

    private static final Logger log = LoggerFactory.getLogger(PublicRulesController.class);

    private final RulesSmsService rulesSmsService;

    @GetMapping("/{token}")
    public ResponseEntity<?> getRulesPage(@PathVariable String token) {
        try {
            return ResponseEntity.ok(rulesSmsService.getApprovalPage(token));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Rules page could not be opened", e);
            return badRequest("Geçersiz veya süresi dolmuş onay linki.");
        }
    }

    @PostMapping("/{token}/approve")
    public ResponseEntity<?> approveRules(@PathVariable String token) {
        try {
            RulesApprovalPageDto dto = rulesSmsService.approve(token);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Rules approval could not be saved", e);
            return badRequest("Onay kaydedilemedi.");
        }
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        error.put("error", "INVALID_TOKEN");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
