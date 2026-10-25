package com.tys.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tys.request.CreateSMSRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VatanSmsService {
    private static final String SMS_URL = "https://api.vatansms.net/api/v1/1toN";
    private static final String API_ID = "ba7889c926c44bcaefe19a94";
    private static final String API_KEY = "402231631e9de08d2debc7c8";


    public void sendSms(CreateSMSRequest request) {
        try {
           URL url = new URL(SMS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> params = new HashMap<>();
            params.put("api_id", API_ID);
            params.put("api_key", API_KEY);
            //params.put("sender", request.getSenderName());
            params.put("sender", "SMS TEST");
            params.put("message_type", "normal");
            params.put("message", request.getMessage());
            params.put("message_content_type", "bilgi");
            params.put("phones", request.getPhoneList());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(params);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            System.out.println("Response Code: " + conn.getResponseCode());
            System.out.println("Response Message: " + conn.getResponseMessage());

            InputStream is = conn.getErrorStream();
            if (is != null) {
                String error = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("ERROR BODY: " + error);
            }

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("SMS gönderilemedi: " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("SMS servis hatası", e);
        }
    }
}
