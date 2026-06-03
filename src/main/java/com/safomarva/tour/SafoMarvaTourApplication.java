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

    private static final Set<String> ACTIVE_PACKAGE_KEYS = Set.of("standard", "comfort", "special_14day", "anjum_lux");
    private static final Set<String> RETIRED_PACKAGE_KEYS = Set.of("lux", "lux_premium", "lux_jumeirah");

    @Bean
    public CommandLineRunner syncPackages(PackageRepository packageRepository) {
        return args -> {
            System.out.println("📦 Paketlar bazasi sayt bilan sinxronlanmoqda...");

            upsertPackage(packageRepository, "standard", "Standard Plus (13 kunlik)", "1250",
                    "Июн , Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Standard Plus\" 13 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️✈️✈️✈️✈️\n\nКетиш саналари:\n📆04.07.2026 (05:20 - 10:40)\n📆11.07.2026 \n📆18.07.2026 \n📆25.07.2026 \n📆01.08.2026 \n📆08.08.2026 \n📆15.08.2026 \n📆22.08.2026 \n📆29.08.2026  \nХафтанинг ҳар Шанба кунлари\n\n✈️ Кетиш: Тошкент – Жидда\n✈️ Қайтиш: Мадина - Тошкент\n\n🏨 Жиддада: Shinam hotel\n🏨 Маккада: Ramada Zad al Tayser hotel\n🏨 Мадинада: Mehrob Toiba\n\n🏨 Жидда : 1 - кеча\n🏨 Маккада: 8 - кеча \n🏨 Мадинада: 3- кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1250$\n🛏 3 кишилик - 1350$\n🛏 2 кишилик - 1450$ \n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n🍽 2 маҳал таом — Мадинада \n🍽 3 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "comfort", "Comfort Plus (10 kunlik)", "1350",
                    "Июн , Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Comfort Plus\" 10 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️✈️✈️✈️✈️\n\nКетиш саналари:\n📆25.06.2026 (11:00 - 16:20)\n📆02.07.2026 \n📆09.07.2026 \n📆16.07.2026 \n📆23.07.2026 \n📆30.07.2026 \n📆06.08.2026 \n📆13.08.2026 \n📆20.08.2026 \n📆27.08.2026 \nХафтанинг ҳар Пайшанба кунлари\n\n✈️ Кетиш: Тошкент – Мадина\n✈️ Қайтиш: Жидда - Тошкент\n\n1 - Жума Мадинада\n1- Жума Маккада\n\n🏨 Мадинада: Nusk al Hijra 4️⃣⭐️\n🏨 Маккада: Al Ebaa hotel 4️⃣⭐️\n\n🏨 Мадинада: 3 - кеча \n🏨 Маккада: 6- кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1350$\n🛏 3 кишилик - 1450$\n🛏 2 кишилик - 1550$ \n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n🍽 2 маҳал таом — Мадинада \n🍽 2 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "special_14day", "Comfort Plus (14 kunlik)", "1500",
                    "7 kecha Madina (Mehrob Toiba 4★) va 7 kecha Makka (Al Ebaa 4★), haftaning har payshanba kunlari uchish. Toshkent-Madina-Toshkent reysi.");
            upsertPackage(packageRepository, "anjum_lux", "ANJUM LUX Hoji Aka (10 kunlik)", "1590",
                    "3 kecha Madina (Waqf As Safi 5★) va 6 kecha Makka (Anjum Makkah 5★). Ketish: Toshkent-Madina, qaytish: Jidda-Toshkent.");

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
