package safo_marva_tour;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.adminId}")
    private String adminId;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            
            String roomInfo = "14 Kunlik Paket".equals(request.getSelectedPackage()) ? 
                    "\n🛏 Xona turi: " + request.getRoom() : "";

            String message = String.format(
                "🌟 <b>YANGI MUROJAAT!</b> 🌟\n\n" +
                "👤 <b>Mijoz:</b> %s\n" +
                "📞 <b>Telefon:</b> <code>%s</code>\n" +
                "📦 <b>Tanlangan paket:</b> %s%s\n\n" +
                "📅 <b>Vaqt:</b> %s\n" +
                "🌐 <b>Manba:</b> Safo Marva Tour (Veb-sayt)\n\n" +
                "📞 <b>Qo'ng'iroq uchun:</b> <a href=\"tel:%s\">%s</a>",
                request.getName(), 
                request.getPhone(), 
                request.getSelectedPackage(), 
                roomInfo, 
                now, 
                request.getPhone().replaceAll("\\s", ""), 
                request.getPhone()
            );

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            
            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", adminId);
            body.put("text", message);
            body.put("parse_mode", "HTML");

            restTemplate.postForEntity(url, body, String.class);

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
