package com.safomarva.tour;

import com.safomarva.tour.model.PackageEntity;
import com.safomarva.tour.repository.PackageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SafoMarvaTourApplication {

    public static void main(String[] eloquenceArgs) {
        SpringApplication.run(SafoMarvaTourApplication.class, eloquenceArgs);
    }

    @Bean
    public CommandLineRunner databaseSeeder(PackageRepository packageRepository) {
        return args -> {
            if (packageRepository.count() == 0) {
                System.out.println("🌱 Database is empty! Seeding default packages into PostgreSQL...");
                
                packageRepository.save(new PackageEntity(
                    "standard", 
                    "Standart Paket", 
                    "1200", 
                    "14 kunlik standart paket, 3 yulduzli mehmonxona, viza va transport xizmatlari."
                ));
                
                packageRepository.save(new PackageEntity(
                    "comfort", 
                    "Komfort Paket", 
                    "1450", 
                    "14 kunlik komfort paket, 4 yulduzli mehmonxona, viza, qulay transport va ekskursiyalar."
                ));
                
                packageRepository.save(new PackageEntity(
                    "lux", 
                    "LUX Paket", 
                    "1850", 
                    "14 kunlik lyuks paket, 5 yulduzli eng yaqin mehmonxonalar, premium xizmat ko'rsatish va viza."
                ));
                
                packageRepository.save(new PackageEntity(
                    "lux_premium", 
                    "LUX Premium Paket", 
                    "2200", 
                    "14 kunlik eng yuqori darajadagi lyuks premium paket, Al-Haram va Nabaviy masjidlariga eng yaqin masofa, VIP xizmatlar."
                ));
                
                packageRepository.save(new PackageEntity(
                    "special_14day", 
                    "14-Kunlik Paket", 
                    "1350", 
                    "14-kunlik maxsus taklif ziyorat paketi. Barcha asosiy viza, sug'urta, transport va mehmonxona xarajatlarini o'z ichiga oladi."
                ));
                
                System.out.println("✅ Seeding packages completed successfully!");
            } else {
                System.out.println("📦 Packages already exist in database, bypassing seeder.");
            }
        };
    }
}
