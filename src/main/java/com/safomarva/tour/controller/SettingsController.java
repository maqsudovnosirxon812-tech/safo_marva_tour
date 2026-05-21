package com.safomarva.tour.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<Map<String, String>> getSettings() {
        File file = new File("settings.json");
        Map<String, String> settings = new HashMap<>();
        if (file.exists()) {
            try {
                settings = objectMapper.readValue(file, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            settings.put("hero_subtitle", "Muborak Safarga Taklif Etamiz");
            settings.put("hero_title", "Yangi Mavsum — Iyun-Iyul Oylaridan!");
            settings.put("hero_desc", "SAFO MARVA TOUR bilan — litsenziyalangan, xavfsiz va xotirjam ziyorat!");
            settings.put("offer_title", "14 Kunlik \"Al Ebaa\" Komfort Paketi");
            settings.put("offer_desc", "Iyun va Iyul oylari uchun maxsus. Toshkentdan to'g'ridan-to'g'ri reyslar va barcha qulayliklar mujassam.");
            settings.put("offer_makka_days", "7");
            settings.put("offer_madina_days", "7");
            settings.put("offer_price_4", "1390");
            settings.put("offer_price_3", "1490");
            settings.put("offer_price_2", "1650");
        }
        return ResponseEntity.ok(settings);
    }
}
