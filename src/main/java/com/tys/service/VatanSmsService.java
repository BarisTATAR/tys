package com.tys.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tys.request.CreateSMSRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VatanSmsService {
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    private final String smsUrl;
    private final String apiId;
    private final String apiKey;
    private final String sender;

    public VatanSmsService(
            @Value("${sms.api-url:https://api.vatansms.net/api/v1/1toN}") String smsUrl,
            @Value("${sms.api-id:}") String apiId,
            @Value("${sms.api-key:}") String apiKey,
            @Value("${sms.sender:}") String sender) {
        this.smsUrl = smsUrl;
        this.apiId = apiId;
        this.apiKey = apiKey;
        this.sender = sender;
    }

    public void sendSms(CreateSMSRequest request) {
        if (apiId == null || apiId.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS servis bilgileri tanımlı değil.");
        }
        try {
            URL url = new URL(smsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Sağlayıcı yanıt vermezse istek iş parçacığı süresiz beklemesin
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> params = new HashMap<>();
            params.put("api_id", apiId);
            params.put("api_key", apiKey);
            params.put("sender", sender == null || sender.isBlank() ? "SMS TEST" : sender);
            params.put("message_type", "normal");
            params.put("message", request.getMessage());
            params.put("message_content_type", "bilgi");
            params.put("phones", request.getPhoneList());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(params);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("SMS sağlayıcısı isteği reddetti: HTTP {}", status);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "SMS gönderilemedi: sağlayıcı isteği kabul etmedi (HTTP " + status + ").");
            }

            InputStream is = conn.getErrorStream();
            if (is != null) {
                is.close();
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("SMS sağlayıcısına ulaşılamadı: {}", e.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "SMS gönderilemedi: sağlayıcıya ulaşılamadı.");
        }
    }
}
