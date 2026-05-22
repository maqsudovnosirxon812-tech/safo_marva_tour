package com.safomarva.tour.controller;

import com.safomarva.tour.model.PackageEntity;
import com.safomarva.tour.repository.PackageRepository;
import com.safomarva.tour.service.PackageMediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class PackageController {

    private final PackageRepository packageRepository;
    private final PackageMediaService packageMediaService;

    @Value("${admin.api-key}")
    private String adminApiKey;

    public PackageController(PackageRepository packageRepository, PackageMediaService packageMediaService) {
        this.packageRepository = packageRepository;
        this.packageMediaService = packageMediaService;
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
    public ResponseEntity<Map<String, Map<String, Object>>> getPackagesMedia() {
        return ResponseEntity.ok(packageMediaService.readAll());
    }

    // 5. Public API: Update Custom package media mapping (used by bot/admin portal)
    @PostMapping("/api/packages/media/{key}")
    public ResponseEntity<?> updatePackageMedia(
            @PathVariable String key,
            @RequestBody Map<String, String> mediaData) {

        Map<String, Object> updated;
        if (mediaData.containsKey("video_url") && mediaData.get("video_url") != null && !mediaData.get("video_url").isEmpty()) {
            updated = packageMediaService.appendVideo(key, mediaData.get("video_url"));
        } else if (mediaData.containsKey("image_url") && mediaData.get("image_url") != null && !mediaData.get("image_url").isEmpty()) {
            updated = packageMediaService.appendImage(key, mediaData.get("image_url"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "image_url or video_url required"));
        }
        return ResponseEntity.ok(Map.of("success", true, "media", updated));
    }
}
