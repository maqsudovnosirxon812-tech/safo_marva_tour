package com.safomarva.tour.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safomarva.tour.model.LeadEntity;
import com.safomarva.tour.model.PackageEntity;
import com.safomarva.tour.repository.LeadRepository;
import com.safomarva.tour.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminTelegramBot extends TelegramLongPollingBot {

    private final PackageRepository packageRepository;
    private final LeadRepository leadRepository;
    private final String botToken;
    private final String botUsername;
    private final String adminsStr;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Keyboards
    private ReplyKeyboardMarkup mainMenuKeyboard;
    private ReplyKeyboardMarkup cancelKeyboard;

    private static class UserState {
        String step;
        String keyName;
        String displayName;
        String price;
        String description;
        Long pkgId;
        String pkgName;
        String pkgKey;
    }

    public AdminTelegramBot(
            PackageRepository packageRepository,
            LeadRepository leadRepository,
            @Value("${bot.token}") String botToken,
            @Value("${bot.username}") String botUsername,
            @Value("${bot.admins}") String adminsStr) {
        super(botToken);
        this.packageRepository = packageRepository;
        this.leadRepository = leadRepository;
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.adminsStr = adminsStr;

        initializeKeyboards();
    }

    private void initializeKeyboards() {
        // Main menu
        mainMenuKeyboard = new ReplyKeyboardMarkup();
        mainMenuKeyboard.setResizeKeyboard(true);
        mainMenuKeyboard.setOneTimeKeyboard(false);
        List<KeyboardRow> mainRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📦 Paketlar");
        row1.add("👥 Murojaatlar");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("➕ Yangi Paket");
        row2.add("📊 Statistika");
        mainRows.add(row1);
        mainRows.add(row2);
        mainMenuKeyboard.setKeyboard(mainRows);

        // Cancel menu
        cancelKeyboard = new ReplyKeyboardMarkup();
        cancelKeyboard.setResizeKeyboard(true);
        cancelKeyboard.setOneTimeKeyboard(false);
        List<KeyboardRow> cancelRows = new ArrayList<>();
        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add("❌ Bekor qilish");
        cancelRows.add(cancelRow);
        cancelKeyboard.setKeyboard(cancelRows);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleIncomingMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleIncomingMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText().trim() : null;

        // Simple security clearance check
        if (!isAuthorized(chatId)) {
            sendTextMessage(chatId, "⚠️ Kechirasiz, siz adminlik huquqiga ega emassiz!");
            return;
        }

        UserState state = userStates.getOrDefault(chatId, new UserState());

        // Cancel command
        if ("❌ Bekor qilish".equals(text)) {
            userStates.remove(chatId);
            sendCustomKeyboardMessage(chatId, "🏠 Bosh menyuga qaytildi. Amal bekor qilindi.", mainMenuKeyboard);
            return;
        }

        // Start command
        if ("/start".equals(text)) {
            userStates.remove(chatId);
            String welcome = "👋 <b>Assalomu alaykum, Safo Marva Tour Admin Tizimiga Xush Kelibsiz!</b>\n\n" +
                    "Ushbu bot yordamida siz veb-saytingizdagi ziyorat paketlarini boshqara olasiz:\n\n" +
                    "📦 <b>Paketlar:</b> Narxlarni va tavsiflarni tahrirlash yoki o'chirish\n" +
                    "➕ <b>Yangi Paket:</b> Saytga yangi ziyorat paketlarini qo'shish\n" +
                    "📊 <b>Statistika:</b> Loyihangizdagi umumiy paketlar va murojaatlar soni\n\n" +
                    "👇 Boshlash uchun quyidagi menyudan foydalaning:";
            sendCustomKeyboardMessage(chatId, welcome, mainMenuKeyboard);
            return;
        }

        // 1. STATE MACHINE - NOT IN ANY STATE (Main Keyboard Routing)
        if (state.step == null) {
            if ("📦 Paketlar".equals(text)) {
                sendPackagesCatalog(chatId);
            } else if ("➕ Yangi Paket".equals(text)) {
                UserState newState = new UserState();
                newState.step = "WAITING_FOR_KEY_NAME";
                userStates.put(chatId, newState);

                String msg = "📝 <b>Yangi Paket Yaratish (1/4)</b>\n\n" +
                        "Paket uchun inglizcha kalit so'z kiriting.\n" +
                        "<i>(Faqat kichik harflar va pastki chiziq, masalan: comfort_plus, premium_15):</i>";
                sendCustomKeyboardMessage(chatId, msg, cancelKeyboard);
            } else if ("👥 Murojaatlar".equals(text)) {
                sendLeadsList(chatId);
            } else if ("📊 Statistika".equals(text)) {
                sendStatistics(chatId);
            }
            return;
        }

        // 2. STATE MACHINE - WAITING_FOR_KEY_NAME
        if ("WAITING_FOR_KEY_NAME".equals(state.step)) {
            if (text == null) return;
            String keyName = text.toLowerCase().replaceAll("[^a-z0-9_]", "");
            if (keyName.isEmpty()) {
                sendTextMessage(chatId, "⚠️ Xatolik! Kalit so'zda faqat inglizcha harflar, raqamlar va pastki chiziq bo'lishi kerak. Qayta urinib ko'ring:");
                return;
            }

            Optional<PackageEntity> existing = packageRepository.findByKeyName(keyName);
            if (existing.isPresent()) {
                sendTextMessage(chatId, "⚠️ Bunday kalit nomli paket allaqachon mavjud. Iltimos boshqacha kiriting:");
                return;
            }

            state.keyName = keyName;
            state.step = "WAITING_FOR_DISPLAY_NAME";
            sendTextMessage(chatId, "🏷 <b>Paket nomini kiriting (2/4)</b>\n<i>(masalan: Komfort Plus Paket):</i>");
            return;
        }

        // 3. STATE MACHINE - WAITING_FOR_DISPLAY_NAME
        if ("WAITING_FOR_DISPLAY_NAME".equals(state.step)) {
            if (text == null) return;
            if (text.length() < 3) {
                sendTextMessage(chatId, "⚠️ Paket nomi kamida 3 ta harfdan iborat bo'lishi kerak. Qayta kiriting:");
                return;
            }

            state.displayName = text;
            state.step = "WAITING_FOR_PRICE";
            sendTextMessage(chatId, "💵 <b>Boshlang'ich narxni faqat raqamlarda kiriting (3/4)</b>\n<i>(masalan: 1350):</i>");
            return;
        }

        // 4. STATE MACHINE - WAITING_FOR_PRICE
        if ("WAITING_FOR_PRICE".equals(state.step)) {
            if (text == null) return;
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                sendTextMessage(chatId, "⚠️ Narxni to'g'ri formatda kiriting (faqat musbat sonlar):");
                return;
            }

            state.price = digits;
            state.step = "WAITING_FOR_DESCRIPTION";
            sendTextMessage(chatId, "📝 <b>Paket tavsifini kiriting (4/4)</b>\n<i>(masalan: Qulay transport, 5 yulduzli mehmonxona, maxsus viza):</i>");
            return;
        }

        // 5. STATE MACHINE - WAITING_FOR_DESCRIPTION
        if ("WAITING_FOR_DESCRIPTION".equals(state.step)) {
            if (text == null) return;
            state.description = text;

            sendTextMessage(chatId, "⏳ Paket ma'lumotlar bazasiga yozilmoqda...");

            try {
                PackageEntity entity = new PackageEntity(state.keyName, state.displayName, state.price, state.description);
                packageRepository.save(entity);

                String msg = "🎉 <b>Muvaffaqiyatli Qo'shildi!</b> \n\n" +
                        "📦 <b>Paket:</b> " + state.displayName + "\n" +
                        "💵 <b>Narxi:</b> $" + state.price + "\n" +
                        "📝 <b>Tavsif:</b> " + state.description + "\n\n" +
                        "Yangi paket darhol veb-saytda aks etadi.";

                sendCustomKeyboardMessage(chatId, msg, mainMenuKeyboard);
            } catch (Exception e) {
                sendCustomKeyboardMessage(chatId, "❌ Xatolik yuz berdi: " + e.getMessage(), mainMenuKeyboard);
            }

            userStates.remove(chatId);
            return;
        }

        // 6. STATE MACHINE - WAITING_FOR_NEW_NAME
        if ("WAITING_FOR_NEW_NAME".equals(state.step)) {
            if (text == null) return;
            if (text.length() < 3) {
                sendTextMessage(chatId, "⚠️ Paket nomi kamida 3 ta harfdan iborat bo'lishi kerak. Qayta kiriting:");
                return;
            }

            sendTextMessage(chatId, "⏳ Nomi yangilanmoqda...");

            try {
                PackageEntity pkg = packageRepository.findById(state.pkgId).orElseThrow();
                String oldName = pkg.getDisplayName();
                pkg.setDisplayName(text);
                packageRepository.save(pkg);

                sendCustomKeyboardMessage(chatId, "✅ Paket nomi muvaffaqiyatli <b>" + oldName + "</b> dan <b>" + text + "</b> ga o'zgartirildi! Saytda yangilandi.", mainMenuKeyboard);
            } catch (Exception e) {
                sendCustomKeyboardMessage(chatId, "❌ Xatolik yuz berdi: " + e.getMessage(), mainMenuKeyboard);
            }

            userStates.remove(chatId);
            return;
        }

        // 7. STATE MACHINE - WAITING_FOR_NEW_PRICE
        if ("WAITING_FOR_NEW_PRICE".equals(state.step)) {
            if (text == null) return;
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                sendTextMessage(chatId, "⚠️ Narxni to'g'ri formatda kiriting (faqat musbat sonlar):");
                return;
            }

            sendTextMessage(chatId, "⏳ Narx yangilanmoqda...");

            try {
                PackageEntity pkg = packageRepository.findById(state.pkgId).orElseThrow();
                pkg.setPrice(digits);
                packageRepository.save(pkg);

                sendCustomKeyboardMessage(chatId, "✅ <b>" + state.pkgName + "</b> narxi muvaffaqiyatli <b>$" + digits + "</b> ga o'zgartirildi!", mainMenuKeyboard);
            } catch (Exception e) {
                sendCustomKeyboardMessage(chatId, "❌ Xatolik yuz berdi: " + e.getMessage(), mainMenuKeyboard);
            }

            userStates.remove(chatId);
            return;
        }

        // 8. STATE MACHINE - WAITING_FOR_NEW_DESC
        if ("WAITING_FOR_NEW_DESC".equals(state.step)) {
            if (text == null) return;

            sendTextMessage(chatId, "⏳ Tavsif yangilanmoqda...");

            try {
                PackageEntity pkg = packageRepository.findById(state.pkgId).orElseThrow();
                pkg.setDescription(text);
                packageRepository.save(pkg);

                sendCustomKeyboardMessage(chatId, "✅ <b>" + state.pkgName + "</b> tavsifi muvaffaqiyatli yangilandi!", mainMenuKeyboard);
            } catch (Exception e) {
                sendCustomKeyboardMessage(chatId, "❌ Xatolik yuz berdi: " + e.getMessage(), mainMenuKeyboard);
            }

            userStates.remove(chatId);
            return;
        }

        // 9. STATE MACHINE - WAITING_FOR_MEDIA
        if ("WAITING_FOR_MEDIA".equals(state.step)) {
            String fileId = null;
            boolean isVideo = false;
            String ext = "jpg";

            if (message.hasPhoto() && !message.getPhoto().isEmpty()) {
                List<PhotoSize> photos = message.getPhoto();
                fileId = photos.get(photos.size() - 1).getFileId();
                ext = "jpg";
            } else if (message.hasVideo()) {
                fileId = message.getVideo().getFileId();
                isVideo = true;
                ext = "mp4";
            } else if (message.hasDocument()) {
                fileId = message.getDocument().getFileId();
                String mime = message.getDocument().getMimeType();
                if (mime != null && mime.startsWith("video/")) {
                    isVideo = true;
                    ext = "mp4";
                } else {
                    ext = "jpg";
                }
            }

            if (fileId == null) {
                sendTextMessage(chatId, "⚠️ Iltimos, faqat rasm yoki video fayl yuboring:");
                return;
            }

            sendTextMessage(chatId, "⏳ Fayl serverga yuklab olinmoqda, iltimos kuting...");

            try {
                GetFile getFileMethod = new GetFile();
                getFileMethod.setFileId(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);

                String originalExt = "";
                String pathOnTelegram = file.getFilePath();
                if (pathOnTelegram != null && pathOnTelegram.contains(".")) {
                    originalExt = pathOnTelegram.substring(pathOnTelegram.lastIndexOf(".") + 1);
                }
                String finalExt = !originalExt.isEmpty() ? originalExt : ext;

                String newFilename = "custom_" + state.pkgKey + "_" + System.currentTimeMillis() + "." + finalExt;
                File destinationFolder = new File("src/main/resources/static/galereya");
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }
                File targetFile = new File(destinationFolder, newFilename);

                // Download using longpolling bot's built-in file downloader
                File tempDownloaded = downloadFile(file);
                Files.copy(tempDownloaded.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String webRelativePath = "galereya/" + newFilename;
                if (isVideo) {
                    updateLocalMedia(state.pkgKey, "video_url", webRelativePath);
                } else {
                    updateLocalMedia(state.pkgKey, "image_url", webRelativePath);
                }

                sendCustomKeyboardMessage(chatId, "✅ <b>" + state.pkgName + "</b> uchun yangi media fayl muvaffaqiyatli yuklandi va saytda yangilandi!", mainMenuKeyboard);
            } catch (Exception e) {
                System.err.println("❌ Download error: " + e.getMessage());
                sendCustomKeyboardMessage(chatId, "❌ Faylni yuklashda xatolik yuz berdi: " + e.getMessage(), mainMenuKeyboard);
            }

            userStates.remove(chatId);
        }
    }

    private void handleCallbackQuery(org.telegram.telegrambots.meta.api.objects.CallbackQuery query) {
        Long chatId = query.getMessage().getChatId();
        Integer messageId = query.getMessage().getMessageId();
        String data = query.getData();

        // 1. Back to packages catalog
        if ("back_to_catalog".equals(data)) {
            deleteMessage(chatId, messageId);
            sendPackagesCatalog(chatId);
            return;
        }

        // 2. Open specific package details
        if (data.startsWith("view_pkg_")) {
            Long pkgId = Long.parseLong(data.replace("view_pkg_", ""));
            deleteMessage(chatId, messageId);
            sendPackageDetails(chatId, pkgId);
            return;
        }

        // 3. Edit Name Query
        if (data.startsWith("edit_name_")) {
            Long pkgId = Long.parseLong(data.replace("edit_name_", ""));
            PackageEntity pkg = packageRepository.findById(pkgId).orElse(null);
            if (pkg == null) {
                sendTextMessage(chatId, "⚠️ Paket topilmadi.");
                return;
            }

            UserState state = new UserState();
            state.step = "WAITING_FOR_NEW_NAME";
            state.pkgId = pkgId;
            state.pkgName = pkg.getDisplayName();
            userStates.put(chatId, state);

            deleteMessage(chatId, messageId);
            sendCustomKeyboardMessage(chatId, "🏷 <b>Nomini o'zgartirish: " + pkg.getDisplayName() + "</b>\n" +
                    "Hozirgi nomi: <b>" + pkg.getDisplayName() + "</b>\n\n" +
                    "Yangi nomini yuboring:", cancelKeyboard);
            return;
        }

        // 4. Edit Media Query
        if (data.startsWith("edit_media_")) {
            Long pkgId = Long.parseLong(data.replace("edit_media_", ""));
            PackageEntity pkg = packageRepository.findById(pkgId).orElse(null);
            if (pkg == null) {
                sendTextMessage(chatId, "⚠️ Paket topilmadi.");
                return;
            }

            UserState state = new UserState();
            state.step = "WAITING_FOR_MEDIA";
            state.pkgId = pkgId;
            state.pkgName = pkg.getDisplayName();
            state.pkgKey = pkg.getKeyName();
            userStates.put(chatId, state);

            deleteMessage(chatId, messageId);
            sendCustomKeyboardMessage(chatId, "🖼 <b>Rasm yoki Video yuklash: " + pkg.getDisplayName() + "</b>\n\n" +
                    "Ushbu paket uchun veb-saytda ko'rsatiladigan yangi Rasm yoki Video faylini yuboring.\n" +
                    "<i>(Faylni botga rasm yoki video shaklida jo'nating):</i>", cancelKeyboard);
            return;
        }

        // 5. Edit Price Query
        if (data.startsWith("edit_price_")) {
            Long pkgId = Long.parseLong(data.replace("edit_price_", ""));
            PackageEntity pkg = packageRepository.findById(pkgId).orElse(null);
            if (pkg == null) {
                sendTextMessage(chatId, "⚠️ Paket topilmadi.");
                return;
            }

            UserState state = new UserState();
            state.step = "WAITING_FOR_NEW_PRICE";
            state.pkgId = pkgId;
            state.pkgName = pkg.getDisplayName();
            userStates.put(chatId, state);

            deleteMessage(chatId, messageId);
            sendCustomKeyboardMessage(chatId, "💵 <b>Narxni o'zgartirish: " + pkg.getDisplayName() + "</b>\n" +
                    "Hozirgi narx: <b>$" + pkg.getPrice() + "</b>\n\n" +
                    "Yangi narxni faqat raqamlarda yuboring:", cancelKeyboard);
            return;
        }

        // 6. Edit Description Query
        if (data.startsWith("edit_desc_")) {
            Long pkgId = Long.parseLong(data.replace("edit_desc_", ""));
            PackageEntity pkg = packageRepository.findById(pkgId).orElse(null);
            if (pkg == null) {
                sendTextMessage(chatId, "⚠️ Paket topilmadi.");
                return;
            }

            UserState state = new UserState();
            state.step = "WAITING_FOR_NEW_DESC";
            state.pkgId = pkgId;
            state.pkgName = pkg.getDisplayName();
            userStates.put(chatId, state);

            deleteMessage(chatId, messageId);
            sendCustomKeyboardMessage(chatId, "📝 <b>Tavsifni o'zgartirish: " + pkg.getDisplayName() + "</b>\n" +
                    "Hozirgi tavsif: <i>" + (pkg.getDescription() != null ? pkg.getDescription() : "Kiritilmagan") + "</i>\n\n" +
                    "Yangi tavsifni yuboring:", cancelKeyboard);
            return;
        }

        // 7. Delete Package Confirmation Query
        if (data.startsWith("delete_pkg_")) {
            Long pkgId = Long.parseLong(data.replace("delete_pkg_", ""));
            PackageEntity pkg = packageRepository.findById(pkgId).orElse(null);
            if (pkg == null) {
                sendTextMessage(chatId, "⚠️ Paket topilmadi.");
                return;
            }

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("✅ Ha, o'chirish", "confirm_del_pkg_" + pkgId));
            row1.add(createInlineButton("❌ Yo'q, bekor qilish", "view_pkg_" + pkgId));
            rows.add(row1);
            inlineKeyboard.setKeyboard(rows);

            deleteMessage(chatId, messageId);
            sendInlineKeyboardMessage(chatId, "🚨 <b>DIQQAT!</b> \n\nRostdan ham <b>" + pkg.getDisplayName() + "</b> paketini butunlay o'chirib tashlamoqchimisiz?\n" +
                    "Bu amal saytdan ham yo'qoladi va ortga qaytarib bo'lmaydi.", inlineKeyboard);
            return;
        }

        // 8. Confirm Delete Package
        if (data.startsWith("confirm_del_pkg_")) {
            Long pkgId = Long.parseLong(data.replace("confirm_del_pkg_", ""));
            deleteMessage(chatId, messageId);

            try {
                packageRepository.deleteById(pkgId);
                sendCustomKeyboardMessage(chatId, "✅ Paket muvaffaqiyatli o'chirildi va saytdan olindi.", mainMenuKeyboard);
            } catch (Exception e) {
                sendCustomKeyboardMessage(chatId, "❌ O'chirishda xatolik: " + e.getMessage(), mainMenuKeyboard);
            }
            return;
        }

        // 9. View Lead Details
        if (data.startsWith("view_lead_")) {
            Long leadId = Long.parseLong(data.replace("view_lead_", ""));
            deleteMessage(chatId, messageId);
            sendLeadDetails(chatId, leadId);
            return;
        }

        // 10. Delete Lead Confirmation
        if (data.startsWith("del_lead_")) {
            Long leadId = Long.parseLong(data.replace("del_lead_", ""));
            LeadEntity lead = leadRepository.findById(leadId).orElse(null);
            if (lead == null) {
                sendTextMessage(chatId, "⚠️ Murojaat topilmadi.");
                return;
            }

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("✅ Ha, o'chirish", "confirm_del_lead_" + leadId));
            row1.add(createInlineButton("❌ Yo'q, orqaga", "view_lead_" + leadId));
            rows.add(row1);
            inlineKeyboard.setKeyboard(rows);

            deleteMessage(chatId, messageId);
            sendInlineKeyboardMessage(chatId, "🗑 <b>Murojaatni O'chirish</b> \n\nRostdan ham <b>" + lead.getName() + "</b> tomonidan qoldirilgan murojaatni o'chirib tashlamoqchimisiz?", inlineKeyboard);
            return;
        }

        // 11. Confirm Delete Lead
        if (data.startsWith("confirm_del_lead_")) {
            Long leadId = Long.parseLong(data.replace("confirm_del_lead_", ""));
            deleteMessage(chatId, messageId);

            try {
                leadRepository.deleteById(leadId);
                sendTextMessage(chatId, "✅ Murojaat ro'yxatdan muvaffaqiyatli o'chirildi!");
                sendLeadsList(chatId);
            } catch (Exception e) {
                sendTextMessage(chatId, "❌ Xatolik: " + e.getMessage());
            }
            return;
        }

        // 12. Back to Leads List
        if ("back_to_leads".equals(data)) {
            deleteMessage(chatId, messageId);
            sendLeadsList(chatId);
        }
    }

    // --- HELPER WRAPPERS ---

    private void sendPackagesCatalog(Long chatId) {
        sendTextMessage(chatId, "⏳ Paketlar ro'yxati olinmoqda...");

        try {
            List<PackageEntity> packages = packageRepository.findAll();
            if (packages.isEmpty()) {
                sendTextMessage(chatId, "📦 Hozircha hech qanday paket mavjud emas.");
                return;
            }

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (PackageEntity pkg : packages) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(createInlineButton("🔹 " + pkg.getDisplayName() + " ($" + pkg.getPrice() + ")", "view_pkg_" + pkg.getId()));
                rows.add(row);
            }
            inlineKeyboard.setKeyboard(rows);

            sendInlineKeyboardMessage(chatId, "📦 <b>Safo Marva Tour Paketlar Katalogi</b>\n\n" +
                    "Batafsil ko'rish, narxini o'zgartirish yoki tahrirlash uchun kerakli paketni tanlang:", inlineKeyboard);
        } catch (Exception e) {
            sendTextMessage(chatId, "❌ Xatolik yuz berdi: " + e.getMessage());
        }
    }

    private void sendPackageDetails(Long chatId, Long pkgId) {
        try {
            PackageEntity pkg = packageRepository.findById(pkgId).orElseThrow();

            String desc = pkg.getDescription() != null ? pkg.getDescription() : "Kiritilmagan";
            String msg = "📦 <b>Paket Tafsilotlari:</b>\n\n" +
                    "🔹 <b>Nomi:</b> " + pkg.getDisplayName() + "\n" +
                    "🔑 <b>Kalit so'z:</b> <code>" + pkg.getKeyName() + "</code>\n" +
                    "💵 <b>Narx:</b> $" + pkg.getPrice() + "\n" +
                    "📝 <b>Tavsif:</b> <i>" + desc + "</i>\n\n" +
                    "<b>Quyidagi amallardan birini tanlang:</b>";

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("🏷 Nomini tahrirlash", "edit_name_" + pkg.getId()));
            row1.add(createInlineButton("🖼 Rasm/Video yuklash", "edit_media_" + pkg.getId()));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("💵 Narxni o'zgartirish", "edit_price_" + pkg.getId()));
            row2.add(createInlineButton("📝 Tavsifni tahrirlash", "edit_desc_" + pkg.getId()));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(createInlineButton("❌ Paketni o'chirish", "delete_pkg_" + pkg.getId()));

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            row4.add(createInlineButton("🔙 Orqaga", "back_to_catalog"));

            rows.add(row1);
            rows.add(row2);
            rows.add(row3);
            rows.add(row4);
            inlineKeyboard.setKeyboard(rows);

            sendInlineKeyboardMessage(chatId, msg, inlineKeyboard);
        } catch (Exception e) {
            sendTextMessage(chatId, "❌ Paket ma'lumotlarini olib bo'lmadi.");
        }
    }

    private void sendLeadsList(Long chatId) {
        sendTextMessage(chatId, "⏳ Kelgan murojaatlar olinmoqda...");

        try {
            List<LeadEntity> leads = leadRepository.findFirst10ByOrderByIdDesc();
            if (leads.isEmpty()) {
                sendTextMessage(chatId, "👥 Hozircha saytdan hech qanday murojaat kelmagan.");
                return;
            }

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (int i = 0; i < leads.size(); i++) {
                LeadEntity lead = leads.get(i);
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(createInlineButton((i + 1) + ". " + lead.getName() + " (" + lead.getPhone() + ")", "view_lead_" + lead.getId()));
                rows.add(row);
            }
            inlineKeyboard.setKeyboard(rows);

            sendInlineKeyboardMessage(chatId, "👥 <b>Oxirgi 10 ta kelgan Murojaatlar</b> \n\n" +
                    "Mijozning to'liq ma'lumotlarini ko'rish yoki uni ro'yxatdan o'chirish uchun ustiga bosing:", inlineKeyboard);
        } catch (Exception e) {
            sendTextMessage(chatId, "❌ Murojaatlarni olishda xatolik: " + e.getMessage());
        }
    }

    private void sendLeadDetails(Long chatId, Long leadId) {
        try {
            LeadEntity lead = leadRepository.findById(leadId).orElseThrow();
            String cleanPhone = lead.getPhone().replaceAll("\\s", "");

            String room = lead.getRoom() != null ? lead.getRoom() : "Kiritilmagan";
            String source = lead.getSource() != null ? lead.getSource() : "Veb-sayt";
            String operator = lead.getOperator() != null ? lead.getOperator() : "Erkak";
            String payment = lead.getPaymentMethod() != null ? lead.getPaymentMethod() : "Naqd pul";
            int persons = lead.getPersons() != null ? lead.getPersons() : 1;

            String message = "👤 <b>Mijoz Ma'lumotlari:</b>\n\n" +
                    "👤 <b>Ismi:</b> " + lead.getName() + "\n" +
                    "📞 <b>Telefon:</b> <code>" + lead.getPhone() + "</code>\n" +
                    "📦 <b>Tanlangan paket:</b> " + lead.getPackageSelected() + "\n" +
                    "👥 <b>Ziyoratchilar soni:</b> " + persons + " kishi\n" +
                    "🛏 <b>Xona turi:</b> " + room + "\n" +
                    "🎧 <b>Operator jinsi:</b> " + operator + "\n" +
                    "💳 <b>To'lov turi:</b> " + payment + "\n" +
                    "🌐 <b>Manba:</b> " + source + "\n\n" +
                    "📞 <b>Mijoz bilan bog'lanish:</b> Telegramda raqam ustiga bosib qo'ng'iroq qilishingiz yoki yozishingiz mumkin.";

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton telBtn = createInlineButton("📞 Qo'ng'iroq qilish (Direct)", "direct_call_lead");
            telBtn.setUrl("tel:" + cleanPhone);
            row1.add(telBtn);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("🗑 Murojaatni o'chirish", "del_lead_" + lead.getId()));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(createInlineButton("🔙 Ro'yxatga qaytish", "back_to_leads"));

            rows.add(row1);
            rows.add(row2);
            rows.add(row3);
            inlineKeyboard.setKeyboard(rows);

            sendInlineKeyboardMessage(chatId, message, inlineKeyboard);
        } catch (Exception e) {
            sendTextMessage(chatId, "❌ Murojaat tafsilotlarini olib bo'lmadi.");
        }
    }

    private void sendStatistics(Long chatId) {
        sendTextMessage(chatId, "⏳ Statistika hisoblanmoqda...");

        try {
            long pkgCount = packageRepository.count();
            long leadCount = leadRepository.count();

            String message = "📊 <b>Safo Marva Tour Umumiy Statistikasi:</b>\n\n" +
                    "📦 <b>Faol Ziyorat Paketlari:</b> <code>" + pkgCount + "</code> ta\n" +
                    "👥 <b>Kelib tushgan jami so'rovlar (Leads):</b> <code>" + leadCount + "</code> ta\n\n" +
                    "💡 <i>Maslahat: Murojaatlar ro'yxatini ko'rish va ularga javob qaytarish uchun 'Murojaatlar' tugmasidan foydalaning.</i>";

            sendCustomKeyboardMessage(chatId, message, mainMenuKeyboard);
        } catch (Exception e) {
            sendCustomKeyboardMessage(chatId, "❌ Statistikani olishda xatolik: " + e.getMessage(), mainMenuKeyboard);
        }
    }

    // --- BASE SEND WRAPPERS ---

    private void sendTextMessage(Long chatId, String htmlText) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(htmlText);
            message.setParseMode("HTML");
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ SendMessage error: " + e.getMessage());
        }
    }

    private void sendCustomKeyboardMessage(Long chatId, String htmlText, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(htmlText);
            message.setParseMode("HTML");
            message.setReplyMarkup(keyboard);
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ SendMessage keyboard error: " + e.getMessage());
        }
    }

    private void sendInlineKeyboardMessage(Long chatId, String htmlText, InlineKeyboardMarkup inlineKeyboard) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(htmlText);
            message.setParseMode("HTML");
            message.setReplyMarkup(inlineKeyboard);
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ SendMessage inline error: " + e.getMessage());
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(String.valueOf(chatId));
            deleteMessage.setMessageId(messageId);
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.err.println("❌ DeleteMessage error: " + e.getMessage());
        }
    }

    private InlineKeyboardButton createInlineButton(String label, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(label);
        btn.setCallbackData(callbackData);
        return btn;
    }

    private boolean isAuthorized(Long chatId) {
        if (adminsStr == null || adminsStr.trim().isEmpty()) {
            return false;
        }
        String[] admins = adminsStr.split(",");
        String chatStr = String.valueOf(chatId);
        for (String admin : admins) {
            if (admin.trim().equals(chatStr)) {
                return true;
            }
        }
        return false;
    }

    // --- JSON WRITER FOR PACKAGES_MEDIA.JSON ---

    @SuppressWarnings("unchecked")
    private synchronized void updateLocalMedia(String key, String mediaType, String value) {
        File file = new File("packages_media.json");
        Map<String, Map<String, String>> mediaMap = new HashMap<>();

        if (file.exists()) {
            try {
                mediaMap = objectMapper.readValue(file, new TypeReference<Map<String, Map<String, String>>>() {});
            } catch (IOException e) {
                System.err.println("❌ Error reading media file inside bot: " + e.getMessage());
            }
        }

        if (!mediaMap.containsKey(key)) {
            mediaMap.put(key, new HashMap<>());
        }

        Map<String, String> keyMedia = mediaMap.get(key);
        keyMedia.put(mediaType, value);

        // Exclusive lock
        if ("image_url".equals(mediaType)) {
            keyMedia.put("video_url", "");
        } else if ("video_url".equals(mediaType)) {
            keyMedia.put("image_url", "");
        }

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, mediaMap);
        } catch (IOException e) {
            System.err.println("❌ Error writing media file inside bot: " + e.getMessage());
        }
    }
}
