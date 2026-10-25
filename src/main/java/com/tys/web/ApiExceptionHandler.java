package com.tys.web;

import com.tys.exception.CompanyBusinessException;
import com.tys.exception.DuplicateCompanyTssCodeException;
import com.tys.exception.DuplicateCompanyUsernameException;
import com.tys.exception.RoomCountBelowRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
