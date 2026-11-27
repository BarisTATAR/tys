package com.tys.web;

import com.tys.exception.CompanyBusinessException;
import com.tys.exception.DuplicateCompanyTssCodeException;
import com.tys.exception.DuplicateCompanyUsernameException;
import com.tys.exception.RoomCountBelowRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateCompanyUsernameException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUsername(DuplicateCompanyUsernameException ex) {
        return conflictResponse(ex);
    }

    @ExceptionHandler(DuplicateCompanyTssCodeException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateTssCode(DuplicateCompanyTssCodeException ex) {
        return conflictResponse(ex);
    }

    @ExceptionHandler(RoomCountBelowRegisteredException.class)
    public ResponseEntity<Map<String, String>> handleRoomCountBelowRegistered(RoomCountBelowRegisteredException ex) {
        return badRequestResponse(ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        String message = ex.getReason() == null || ex.getReason().isBlank()
                ? "İstek tamamlanamadı."
                : ex.getReason();
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Girilen değer geçersiz."
                : ex.getMessage();
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.badRequest().body(error);
    }

    /** Okunamayan istek gövdesi (bozuk JSON, yanlış biçimli tarih/sayı): iç ayrıntı dönmeden anlamlı 400. */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class).warn("Okunamayan istek: {}", ex.getMostSpecificCause().getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "İstek okunamadı; girilen alanların biçimini kontrol edin.");
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Yabancı anahtar / benzersizlik ihlali: kayıt başka kayıtlarla ilişkili. SQL ve kısıt adları istemciye dönmez.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class).warn("Veri bütünlüğü ihlali: {}", ex.getMostSpecificCause().getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("message", "Kayıt başka kayıtlarla ilişkili olduğu için işlem tamamlanamadı.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    private static ResponseEntity<Map<String, String>> conflictResponse(CompanyBusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex));
    }

    private static ResponseEntity<Map<String, String>> badRequestResponse(CompanyBusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex));
    }

    private static Map<String, String> errorBody(CompanyBusinessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        error.put("error", ex.getErrorCode());
        return error;
    }
}
