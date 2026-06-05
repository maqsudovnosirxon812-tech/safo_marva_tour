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

    private static final Set<String> ACTIVE_PACKAGE_KEYS = Set.of("standard", "comfort", "comfort_14", "comfort_plus_14", "anjum_lux");
    private static final Set<String> RETIRED_PACKAGE_KEYS = Set.of("lux", "lux_premium", "lux_jumeirah", "special_14day");

    @Bean
    public CommandLineRunner syncPackages(PackageRepository packageRepository) {
        return args -> {
            System.out.println("📦 Paketlar bazasi sayt bilan sinxronlanmoqda...");

            upsertPackage(packageRepository, "standard", "Standard Plus (13 kunlik)", "1250",
                    "Июн , Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Standard Plus\" 13 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️✈️✈️✈️✈️\n\nКетиш саналари:\n📆04.07.2026 (05:20 - 10:40)\n📆11.07.2026 \n📆18.07.2026 \n📆25.07.2026 \n📆01.08.2026 \n📆08.08.2026 \n📆15.08.2026 \n📆22.08.2026 \n📆29.08.2026  \nХафтанинг ҳар Шанба кунлари\n\n✈️ Кетиш: Тошкент – Жидда\n✈️ Қайтиш: Мадина - Тошкент\n\n🏨 Жиддада: Shinam hotel\n🏨 Маккада: Ramada Zad al Tayser hotel\n🏨 Мадинада: Mehrob Toiba\n\n🏨 Жидда : 1 - кеча\n🏨 Маккада: 8 - кеча \n🏨 Мадинада: 3- кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1250$\n🛏 3 кишилик - 1350$\n🛏 2 кишилик - 1450$ \n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n🍽 2 маҳал таом — Мадинада \n🍽 3 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "comfort", "Comfort Plus (10 kunlik)", "1350",
                    "Июн , Июл ва Август ойилари учун SAFO MARVA TOUR нинг \"Comfort Plus\" 10 кунлик пакети\n\nSAFO MARVA TOUR билан — лицензияланган, хавфсиз, хотиржам\n\n✈️✈️✈️✈️✈️✈️✈️✈️✈️✈️\n\nКетиш саналари:\n📆25.06.2026 (11:00 - 16:20)\n📆02.07.2026 \n📆09.07.2026 \n📆16.07.2026 \n📆23.07.2026 \n📆30.07.2026 \n📆06.08.2026 \n📆13.08.2026 \n📆20.08.2026 \n📆27.08.2026 \nХафтанинг ҳар Пайшанба кунлари\n\n✈️ Кетиш: Тошкент – Мадина\n✈️ Қайтиш: Жидда - Тошкент\n\n1 - Жума Мадинада\n1- Жума Маккада\n\n🏨 Мадинада: Nusk al Hijra 4️⃣⭐️\n🏨 Маккада: Al Ebaa hotel 4️⃣⭐️\n\n🏨 Мадинада: 3 - кеча \n🏨 Маккада: 6- кеча\n\n‼️ Хона жойлашувлари бўйича:\n🛏 4 кишилик - 1350$\n🛏 3 кишилик - 1450$\n🛏 2 кишилик - 1550$ \n\n\n🎁 Пакетимиз ичига кирувчи хизматлар:\n🚄 Тезюрар поезд хизмати\n🍽 2 маҳал таом — Мадинада \n🍽 2 маҳал таом — Маккада \n🕌 Макка ва Мадинада экскурсиялар\n🕌 Равзага кириш имконияти\n📖 Илмли гуруҳ раҳбарлари хизматлари\n⚕️ Малакали шифокор хизматлари\n🐳 Қизил денгиз саёхати\n\n🎁 Ҳадиялар:\n🧊 Зам-Зам 5 л \n🧥 Нимча\n🎒 Сумка\n🪪 Бейжик\n👗 Абая (аёллар учун)\n\n👨‍💻 Мурожаат учун: \n📞 555177373\n✅ @Safomarva_admin");
            upsertPackage(packageRepository, "comfort_14", "Comfort (14 kunlik)", "1450",
                    "Iyun oyi uchun SAFO MARVA TOUR ning \"Comfort\" 14 kunlik paketi.\n\nSAFO MARVA TOUR bilan — litsenziyalangan, xavfsiz, xotirjam.\n\nKetish va qaytish sanasi: 18.06.2026 - 02.07.2026\n\nYo'nalish: Toshkent - Madina - Toshkent\n\n1 Juma Madinada va 1 Juma Makkada.\n\nMadina: Mehrob Toiba / Manazel al Safiyah 4★, 7 kecha.\nMakka: Ramada Zad al Tayser hotel 4★, 7 kecha.\n\nXona joylashuvlari:\n4 kishilik - 1450$\n3 kishilik - 1550$\n2 kishilik - 1700$\n\nPaket ichiga kiruvchi xizmatlar:\nTezyurar poyezd xizmati\n2 mahal taom Madinada\n3 mahal taom Makkada\nMakka va Madinada ekskursiyalar\nRavzaga kirish imkoniyati\nIlmli guruh rahbarlari xizmatlari\nMalakali shifokor xizmatlari\nQizil dengiz sayohati\n\nHadyalar: Zam-Zam 5 l, nimcha, sumka, beyjik, abaya (ayollar uchun).\n\nMurojaat uchun: 555177373");
            upsertPackage(packageRepository, "comfort_plus_14", "Comfort Plus (14 kunlik)", "1550",
                    "Iyun oyi uchun SAFO MARVA TOUR ning \"Comfort Plus\" 14 kunlik paketi.\n\nSAFO MARVA TOUR bilan — litsenziyalangan, xavfsiz, xotirjam.\n\nKetish va qaytish sanasi: 18.06.2026 - 02.07.2026\n\nYo'nalish: Toshkent - Madina - Toshkent\n\n1 Juma Madinada va 1 Juma Makkada.\n\nMadina: Mehrob Toiba / Manazel al Safiyah 4★, 7 kecha.\nMakka: Al Ebaa hotel 4★, 7 kecha.\n\nXona joylashuvlari:\n4 kishilik - 1550$\n3 kishilik - 1650$\n2 kishilik - 1800$\n\nPaket ichiga kiruvchi xizmatlar:\nTezyurar poyezd xizmati\n2 mahal taom Madinada\n2 mahal taom Makkada\nMakka va Madinada ekskursiyalar\nRavzaga kirish imkoniyati\nIlmli guruh rahbarlari xizmatlari\nMalakali shifokor xizmatlari\nQizil dengiz sayohati\n\nHadyalar: Zam-Zam 5 l, nimcha, sumka, beyjik, abaya (ayollar uchun).\n\nMurojaat uchun: 555177373");
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
