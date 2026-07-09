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

    private static final Set<String> ACTIVE_PACKAGE_KEYS = Set.of(
            "special_14day", "comfort_plus_10", "standard_13", "al_jabri_14",
            "anjum_lux");
    private static final Set<String> RETIRED_PACKAGE_KEYS = Set.of("lux", "lux_premium", "lux_jumeirah");

    @Bean
    public CommandLineRunner syncPackages(PackageRepository packageRepository) {
        return args -> {
            System.out.println("📦 Paketlar bazasi sayt bilan sinxronlanmoqda...");

            upsertPackage(packageRepository, "comfort_plus_10", "Comfort Plus (10 kunlik)", "1390",
                    "Iyun, Iyul va Avgust oylari uchun SAFO MARVA TOUR ning \"Comfort Plus\" 10 kunlik paketi\n\nSAFO MARVA TOUR bilan — litsenziyalangan, xavfsiz, xotirjam\n\nMadina: Nusk Al Hijra 4★, 3 kecha\nMakka: Al Ebaa hotel 4★, 6 kecha\n\nXona joylashuvlari:\n4 kishilik - 1390$\n3 kishilik - 1490$\n2 kishilik - 1590$\n\nPaket ichiga kiruvchi xizmatlar:\nAviachipta\nViza va sug'urta\nMehmonxona\nTransfer\nTezyurar poyezd\n2 mahal ovqat\nZiyoratlar\nGuruh rahbari\nShifokor xizmati\n\nMurojaat uchun:\n📞 +998 55 517 73 73\n📩 @Safomarva_admin");
            upsertPackage(packageRepository, "special_14day", "Comfort Plus (14 kunlik)", "1550",
                    "Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Comfort Plus\" 14 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️\n\n📆18.07.2026 - 01.08.2026\n\n✈️ Тошкент – Мадина - Тошкент\n\n1 - Жума Мадинада\n1- Жума Маккада\n\n🏨 Мадинада: Mehrob Toiba 4️⃣⭐️\n🏨 Маккада: Al Ebaa hotel 4️⃣⭐️\n\n🏨 Мадинада: 7 - кеча \n🏨 Маккада: 7 - кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1550$\n🛏 3 кишилик - 1650$\n🛏 2 кишилик - 1850$\n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n🍽 2 маҳал таом — Мадинада \n🍽 2 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Murojaat uchun:\n📞 +998 55 517 73 73\n📩 @Safomarva_admin");

            upsertPackage(packageRepository, "anjum_lux", "ANJUM LUX Hoji Aka (10 kunlik)", "1650",
                    "Август ва Сентябр ойи учун SAFO MARVA TOUR нинг \"ANJUM HOJI AKA\" 10 кунлик LUX пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️ Кетиш: Тошкент – Мадина\n✈️ Қайтиш: Жидда - Тошкент\n\n1 - Жума Мадинада\n1- Жума Маккада\n\n🏨 Мадинада: Waqf as Safi 5️⃣⭐️\n🏨 Маккада: ANJUM MAKKAH 5️⃣⭐️\n\n🏨 Мадинада: 4 - кеча \n🏨 Маккада: 5 - кеча\n\nКетиш саналари:\n📆13.08.2026 (11:00 - 16:20)\n\nХафтанинг ҳар Пайшанба кунлари\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1650$\n🛏 3 кишилик - 1750$\n🛏 2 кишилик - 1900$\n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n ▶️Мадина ▶️Макка томон\n🍽 2 маҳал таом — Мадинада \n🍽 2 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "standard_13", "Standard (13 kunlik)", "1100",
                    "Июл ойи учун SAFO MARVA TOUR нинг 13 кунлик \"Standard\" пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\nКетиш саналари:\n📆11.07.2026 (05:20 - 10:40)\n\n✈️ Кетиш: Тошкент – Жидда\n✈️ Қайтиш: Мадина - Тошкент\n\n\n🏨 Маккада: Jabri Makka 4️⃣⭐️\n🏨 Мадинада: Mehrob Toiba hotel 4️⃣⭐️\n\n🏨 Маккада: 9 - кеча \n🏨 Мадинада: 3 - кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1100$\n🛏 3 кишилик - 1200$\n🛏 2 кишилик - 1300$\n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🍽 3 маҳал таом — Маккада \n🍽 2 маҳал таом — Мадинада\n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "al_jabri_14", "Al Jabri Standart (14 kunlik)", "1250",
                    "Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Al Jabri Standart\" 14 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️\n\n📆18.07.2026 - 01.08.2026\n\n✈️ Тошкент – Мадина - Тошкент\n\n1 - Жума Мадинада\n1- Жума Маккада\n\n🏨 Мадинада: Mehrob Toiba 4️⃣⭐️\n🏨 Маккада: Al Jabri 4️⃣⭐️\n\n🏨 Мадинада: 7 - кеча \n🏨 Маккада: 7 - кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1250$\n🛏 3 кишилик - 1350$\n🛏 2 кишилик - 1450$\n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🍽 2 маҳал таом — Мадинада \n🍽 3 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373");

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
