package com.safomarva.tour;

import com.safomarva.tour.model.PackageEntity;
import com.safomarva.tour.repository.PackageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
public class SafoMarvaTourApplication {

    public static void main(String[] eloquenceArgs) {
        SpringApplication.run(SafoMarvaTourApplication.class, eloquenceArgs);
    }

    private static final Set<String> ACTIVE_PACKAGE_KEYS = Set.of("standard", "comfort", "lux", "special_14day", "lux_jumeirah");
    private static final Set<String> RETIRED_PACKAGE_KEYS = Set.of("lux_premium");

    @Bean
    public CommandLineRunner syncPackages(PackageRepository packageRepository) {
        return args -> {
            System.out.println("📦 Paketlar bazasi sayt bilan sinxronlanmoqda...");

            upsertPackage(packageRepository, "standard", "Standart Paket", "1150",
                    "10 kechalik standart paket. Ramada Zad Al Tayser (Haramga ~1 km). Viza va transport xizmatlari.");
            upsertPackage(packageRepository, "comfort", "Komfort Paket", "1300",
                    "10 kechalik komfort paket. AL EBAA mehmonxonasi (Haramga ~500 m). Viza, transport va qulay xizmatlar.");
            upsertPackage(packageRepository, "lux", "LUX Paket", "1550",
                    "10 kechalik lyuks paket. ANJUM mehmonxonasi (Haramga ~150 m). Premium xizmat va viza.");
            upsertPackage(packageRepository, "special_14day", "14 Kunlik Paket", "1390",
                    "14 kunlik maxsus paket. Madina: Mehrob Toiba (7 kecha), Makka: Al Ebaa (7 kecha). To'liq xizmat va hadiyalar.");
            upsertPackage(packageRepository, "lux_jumeirah", "LUX Jumeirah Paket", "1690",
                    "10 kunlik LUX Jumeirah premium. Madina: Waqf As Safi (3 kecha), Makka: Jumeirah Jabal Omar (6 kecha). Iyun-Iyul maxsus.");

            packageRepository.findAll().stream()
                    .filter(pkg -> RETIRED_PACKAGE_KEYS.contains(pkg.getKeyName()))
                    .forEach(pkg -> {
                        packageRepository.delete(pkg);
                        System.out.println("🗑 Eski paket o'chirildi: " + pkg.getKeyName() + " (" + pkg.getDisplayName() + ")");
                    });

            System.out.println("✅ Paketlar sinxronlandi. Faol paketlar: " + ACTIVE_PACKAGE_KEYS);
        };
    }

    private void upsertPackage(
            PackageRepository packageRepository,
            String keyName,
            String displayName,
            String price,
            String description) {
        PackageEntity pkg = packageRepository.findByKeyName(keyName)
                .orElseGet(() -> new PackageEntity(keyName, displayName, price, description));

        pkg.setDisplayName(displayName);
        pkg.setPrice(price);
        pkg.setDescription(description);
        packageRepository.save(pkg);
    }
}
