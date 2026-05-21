package com.safomarva.tour.config;

import com.safomarva.tour.bot.AdminTelegramBot;
import com.safomarva.tour.bot.LeadTelegramBot;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import jakarta.annotation.PostConstruct;

@Configuration
public class BotConfig {

    private final AdminTelegramBot adminTelegramBot;
    private final LeadTelegramBot leadTelegramBot;

    public BotConfig(AdminTelegramBot adminTelegramBot, LeadTelegramBot leadTelegramBot) {
        this.adminTelegramBot = adminTelegramBot;
        this.leadTelegramBot = leadTelegramBot;
    }

    @PostConstruct
    public void registerBot() {
        try {
            System.out.println("⏳ Registering Telegram Bot explicitly...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(adminTelegramBot);
            System.out.println("🚀 [SUCCESS] Telegram Bot '@" + adminTelegramBot.getBotUsername() + "' registered successfully and is now active!");
            
            botsApi.registerBot(leadTelegramBot);
            System.out.println("🚀 [SUCCESS] Telegram Bot '@" + leadTelegramBot.getBotUsername() + "' registered successfully and is now active!");
        } catch (TelegramApiException e) {
            System.err.println("❌ [ERROR] Failed to register Telegram Bot: " + e.getMessage());
        }
    }
}
