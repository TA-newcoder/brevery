package com.brevery.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilityService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getLocationByIp(String ip) {
        if (ip == null || ip.isEmpty() || "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            // For local testing, use a public IP or return a mock response
            ip = "14.248.83.31"; // Example IP in VN
        }
        
        try {
            String url = "http://ip-api.com/json/" + ip;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error fetching location for IP {}: {}", ip, e.getMessage());
        }
        return new HashMap<>();
    }

    public String shortenUrl(String longUrl) {
        try {
            String apiUrl = "https://cleanuri.com/api/v1/shorten";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("url", longUrl);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, request, JsonNode.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().path("result_url").asText();
            }
        } catch (Exception e) {
            log.error("Error shortening URL {}: {}", longUrl, e.getMessage());
        }
        return longUrl; // Fallback to original
    }
}
