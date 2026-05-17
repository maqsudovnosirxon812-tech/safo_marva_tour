package com.safomarva.tour.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safomarva.tour.model.PackageEntity;
import com.safomarva.tour.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class PackageController {

    private final PackageRepository packageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MEDIA_FILE_PATH = "packages_media.json";

    @Value("${admin.api-key}")
    private String adminApiKey;

    public PackageController(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    // 1. Public API: Fetch all packages
    @GetMapping("/api/packages")
    public ResponseEntity<List<PackageEntity>> getPublicPackages() {
        return ResponseEntity.ok(packageRepository.findAll());
    }

    // 2. Admin API: Fetch all packages (Requires validation or simple lookup)
    @GetMapping("/admin/packages")
    public ResponseEntity<List<PackageEntity>> getAdminPackages() {
        return ResponseEntity.ok(packageRepository.findAll());
    }

    // 3. Admin API: Edit specific package details
    @PostMapping("/admin/package/{id}")
    public ResponseEntity<?> updatePackage(
            @PathVariable Long id,
            @RequestHeader(value = "x-admin-key", required = false) String headerKey,
            @RequestBody Map<String, String> updates) {

        // Validate secret admin API key header
        if (headerKey == null || !headerKey.equals(adminApiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden: Invalid Admin Key"));
        }

        Optional<PackageEntity> packageOptional = packageRepository.findById(id);
        if (packageOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Package not found"));
        }

        PackageEntity pkg = packageOptional.get();

        if (updates.containsKey("displayName")) {
            pkg.setDisplayName(updates.get("displayName"));
        }
        if (updates.containsKey("price")) {
            pkg.setPrice(updates.get("price"));
        }
        if (updates.containsKey("description")) {
            pkg.setDescription(updates.get("description"));
        }

        PackageEntity saved = packageRepository.save(pkg);
        return ResponseEntity.ok(saved);
    }

    // 4. Public API: Fetch Custom package media mapping
    @GetMapping("/api/packages/media")
    public ResponseEntity<Map<String, Map<String, String>>> getPackagesMedia() {
        return ResponseEntity.ok(readMediaFile());
    }

    // 5. Public API: Update Custom package media mapping (used by bot/admin portal)
    @PostMapping("/api/packages/media/{key}")
    public ResponseEntity<?> updatePackageMedia(
            @PathVariable String key,
            @RequestBody Map<String, String> mediaData) {

        Map<String, Map<String, String>> mediaMap = readMediaFile();
        if (!mediaMap.containsKey(key)) {
            mediaMap.put(key, new HashMap<>());
        }

        Map<String, String> keyMedia = mediaMap.get(key);
        if (mediaData.containsKey("image_url")) {
            String imageUrl = mediaData.get("image_url");
            keyMedia.put("image_url", imageUrl);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                keyMedia.put("video_url", ""); // Exclusive choice
            }
        }
        if (mediaData.containsKey("video_url")) {
            String videoUrl = mediaData.get("video_url");
            keyMedia.put("video_url", videoUrl);
            if (videoUrl != null && !videoUrl.isEmpty()) {
                keyMedia.put("image_url", ""); // Exclusive choice
            }
        }

        writeMediaFile(mediaMap);
        return ResponseEntity.ok(Map.of("success", true, "media", keyMedia));
    }

    // Helper: Read media from JSON file safely
    @SuppressWarnings("unchecked")
    private synchronized Map<String, Map<String, String>> readMediaFile() {
        File file = new File(MEDIA_FILE_PATH);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (IOException e) {
            System.err.println("❌ Error reading media file: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Helper: Write media into JSON file safely
    private synchronized void writeMediaFile(Map<String, Map<String, String>> mediaMap) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(MEDIA_FILE_PATH), mediaMap);
        } catch (IOException e) {
            System.err.println("❌ Error writing media file: " + e.getMessage());
        }
    }
}
