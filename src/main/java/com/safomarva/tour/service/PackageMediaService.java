package com.safomarva.tour.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class PackageMediaService {

    private static final String MEDIA_FILE_PATH = "packages_media.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public synchronized Map<String, Map<String, Object>> readAll() {
        File file = new File(MEDIA_FILE_PATH);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try {
            Map<String, Map<String, Object>> raw = objectMapper.readValue(file,
                    new TypeReference<Map<String, Map<String, Object>>>() {});
            Map<String, Map<String, Object>> normalized = new LinkedHashMap<>();
            raw.forEach((key, value) -> normalized.put(key, normalize(value)));
            return normalized;
        } catch (IOException e) {
            System.err.println("Error reading packages_media.json: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public synchronized Map<String, Object> normalize(Map<String, Object> raw) {
        if (raw == null) raw = new HashMap<>();

        String imageUrl = str(raw.get("image_url"));
        String videoUrl = str(raw.get("video_url"));

        List<String> images = toStringList(raw.get("images"));
        List<String> videos = toStringList(raw.get("videos"));

        if (images.isEmpty() && !imageUrl.isEmpty()) {
            images = new ArrayList<>(List.of(imageUrl));
        }
        if (!imageUrl.isEmpty() && !images.contains(imageUrl)) {
            images.add(0, imageUrl);
        }
        if (imageUrl.isEmpty() && !images.isEmpty()) {
            imageUrl = images.get(0);
        }

        if (videos.isEmpty() && !videoUrl.isEmpty()) {
            videos = new ArrayList<>(List.of(videoUrl));
        }
        if (!videoUrl.isEmpty() && !videos.contains(videoUrl)) {
            videos.add(0, videoUrl);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("image_url", imageUrl);
        out.put("video_url", videoUrl);
        out.put("images", dedupe(images));
        out.put("videos", dedupe(videos));
        return out;
    }

    /** Yangi rasm qo'shadi — asosiy (image_url) o'zgarmaydi */
    public synchronized Map<String, Object> appendImage(String packageKey, String webRelativePath) {
        Map<String, Map<String, Object>> all = readAll();
        Map<String, Object> pkg = normalize(all.getOrDefault(packageKey, new HashMap<>()));

        String primary = str(pkg.get("image_url"));
        if (primary.isEmpty()) {
            pkg.put("image_url", webRelativePath);
        }

        @SuppressWarnings("unchecked")
        List<String> images = new ArrayList<>((List<String>) pkg.get("images"));
        if (!images.contains(webRelativePath)) {
            images.add(webRelativePath);
        }
        if (!primary.isEmpty() && images.contains(primary)) {
            images.remove(primary);
            images.add(0, primary);
        }
        pkg.put("images", images);

        all.put(packageKey, pkg);
        writeAll(all);
        return pkg;
    }

    /** Yangi video qo'shadi */
    public synchronized Map<String, Object> appendVideo(String packageKey, String webRelativePath) {
        Map<String, Map<String, Object>> all = readAll();
        Map<String, Object> pkg = normalize(all.getOrDefault(packageKey, new HashMap<>()));

        @SuppressWarnings("unchecked")
        List<String> videos = new ArrayList<>((List<String>) pkg.get("videos"));
        if (!videos.contains(webRelativePath)) {
            videos.add(webRelativePath);
        }
        pkg.put("videos", videos);
        if (str(pkg.get("video_url")).isEmpty()) {
            pkg.put("video_url", webRelativePath);
        }

        all.put(packageKey, pkg);
        writeAll(all);
        return pkg;
    }

    /** Ma'lum bir rasmini o'chiradi */
    public synchronized Map<String, Object> deleteImage(String packageKey, String webRelativePath) {
        Map<String, Map<String, Object>> all = readAll();
        Map<String, Object> pkg = normalize(all.getOrDefault(packageKey, new HashMap<>()));

        @SuppressWarnings("unchecked")
        List<String> images = new ArrayList<>((List<String>) pkg.get("images"));
        images.remove(webRelativePath);

        // Asosiy rasm o'chirilgan bo'lsa, boshqa rasmini asosiy qilib belgilaymiz
        String primary = str(pkg.get("image_url"));
        if (primary.equals(webRelativePath)) {
            pkg.put("image_url", images.isEmpty() ? "" : images.get(0));
        }

        pkg.put("images", images);
        all.put(packageKey, pkg);
        writeAll(all);
        return pkg;
    }

    /** Ma'lum bir videoni o'chiradi */
    public synchronized Map<String, Object> deleteVideo(String packageKey, String webRelativePath) {
        Map<String, Map<String, Object>> all = readAll();
        Map<String, Object> pkg = normalize(all.getOrDefault(packageKey, new HashMap<>()));

        @SuppressWarnings("unchecked")
        List<String> videos = new ArrayList<>((List<String>) pkg.get("videos"));
        videos.remove(webRelativePath);

        // Asosiy video o'chirilgan bo'lsa, boshqa videoni asosiy qilib belgilaymiz
        String primary = str(pkg.get("video_url"));
        if (primary.equals(webRelativePath)) {
            pkg.put("video_url", videos.isEmpty() ? "" : videos.get(0));
        }

        pkg.put("videos", videos);
        all.put(packageKey, pkg);
        writeAll(all);
        return pkg;
    }

    public File resolveGalleryDir() {
        File prod = new File("galereya");
        if (prod.exists() || prod.mkdirs()) {
            return prod;
        }
        File dev = new File("src/main/resources/static/galereya");
        if (!dev.exists()) {
            dev.mkdirs();
        }
        return dev;
    }

    private synchronized void writeAll(Map<String, Map<String, Object>> mediaMap) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(MEDIA_FILE_PATH), mediaMap);
        } catch (IOException e) {
            System.err.println("Error writing packages_media.json: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    String s = o.toString().trim();
                    if (!s.isEmpty()) out.add(s);
                }
            }
            return out;
        }
        return new ArrayList<>();
    }

    private List<String> dedupe(List<String> list) {
        LinkedHashSet<String> set = new LinkedHashSet<>(list);
        return new ArrayList<>(set);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
