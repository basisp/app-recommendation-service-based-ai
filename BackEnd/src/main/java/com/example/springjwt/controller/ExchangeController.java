package com.example.springjwt.controller;

import com.example.springjwt.dto.AppDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExchangeController {

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeApps(@RequestBody(required = false) List<AppDTO> apps) {
        List<Map<String, Object>> processedApps = new ArrayList<>();

        if (apps != null) {
            for (AppDTO app : apps) {
                Map<String, Object> appMap = new HashMap<>();
                appMap.put("timestamp", app.getTimestamp());
                appMap.put("package_name", app.getPackageName());
                appMap.put("category", app.getCategory());
                processedApps.add(appMap);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Received apps successfully");
        response.put("count", apps != null ? apps.size() : 0);
        response.put("apps", processedApps);

        return ResponseEntity.ok(response);
    }
}