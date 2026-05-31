package com.safomarva.tour.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.CacheControl;
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
        }

        settings.putIfAbsent("meta_pixel_id", "");
        settings.putIfAbsent("hero_subtitle", "Muborak Safarga Taklif Etamiz");
        settings.putIfAbsent("hero_title", "Yangi Mavsum — Iyun-Iyul-Avgust!");
        settings.putIfAbsent("hero_desc", "SAFO MARVA TOUR bilan — litsenziyalangan, xavfsiz va xotirjam ziyorat!");
        settings.putIfAbsent("offer_title", "Comfort Plus — 14 Kunlik Umra Paketi");
        settings.putIfAbsent("offer_desc", "Iyun, Iyul va Avgust oylari uchun maxsus. Toshkentdan to'g'ridan-to'g'ri reyslar va barcha qulayliklar mujassam.");
        settings.putIfAbsent("offer_makka_days", "7");
        settings.putIfAbsent("offer_madina_days", "7");
        settings.putIfAbsent("offer_price_4", "1500");
        settings.putIfAbsent("offer_price_3", "1600");
        settings.putIfAbsent("offer_price_2", "1750");

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(settings);
    }
}