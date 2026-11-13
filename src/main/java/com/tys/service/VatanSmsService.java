package com.tys.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tys.request.CreateSMSRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class VatanSmsService {
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
            throw new IllegalStateException("SMS servis bilgileri tanımlı değil.");
        }
        try {
            URL url = new URL(smsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("SMS gönderilemedi.");
            }

            InputStream is = conn.getErrorStream();
            if (is != null) {
                is.close();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("SMS servis hatası");
        }
    }
}
