/**
 * Safo Marva Tour - Admin Telegram Bot
 * Token: 8843389342:AAFcyDSoofSqojZZWW2IugS4dL3mL-PWtq4
 */

// Avtomatik o'rnatish bloki
try {
    require('node-telegram-bot-api');
    require('@supabase/supabase-js');
    require('dotenv');
} catch (e) {
    console.log("Kerakli kutubxonalar topilmadi. O'rnatilmoqda...");
    const { execSync } = require('child_process');
    execSync('npm install node-telegram-bot-api @supabase/supabase-js dotenv', { stdio: 'inherit' });
    console.log("Kutubxonalar muvaffaqiyatli o'rnatildi! Bot ishga tushmoqda...");
}

const TelegramBot = require('node-telegram-bot-api');
const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

// .env faylini yuklash va tekshirish
require('dotenv').config();

const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || '8843389342:AAFcyDSoofSqojZZWW2IugS4dL3mL-PWtq4';
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://hddpzctigsjqljtooogq.supabase.co';
const SUPABASE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!SUPABASE_KEY) {
    console.warn("\n⚠️  DIQQAT: .env faylida SUPABASE_SERVICE_ROLE_KEY topilmadi!");
    console.warn("Bot bazaga yozish huquqiga ega bo'lmasligi mumkin.");
    console.warn("Iltimos, Supabase dashboarddan secret 'service_role' kalitini oling.\n");
}

// Supabase mijozini yaratish
const supabase = createClient(SUPABASE_URL, SUPABASE_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.OfwTe9X1_hGzdYVrV2hF_DmFwNBVjjVkQLcAmV_uA84');

// Botni polling rejimi bilan ishga tushirish
const bot = new TelegramBot(BOT_TOKEN, { polling: true });

console.log("🟢 Safo Marva Tour Admin Bot ishga tushdi!");

// Foydalanuvchilar holatini saqlash (State Management)
const userStates = {};

// Asosiy menyu klaviaturasi
const mainMenuKeyboard = {
    reply_markup: {
        keyboard: [
            [{ text: '📦 Paketlar Ro\'yxati' }, { text: '➕ Yangi Paket Qo\'shish' }],
            [{ text: '✏️ Narxni O\'zgartirish' }, { text: '❌ Paketni O\'chirish' }]
        ],
        resize_keyboard: true
    }
};

// Bekor qilish tugmasi
const cancelKeyboard = {
    reply_markup: {
        keyboard: [[{ text: '❌ Bekor qilish' }]],
        resize_keyboard: true
    }
};

// /start buyrug'i
bot.onText(/\/start/, (msg) => {
    const chatId = msg.chat.id;
    userStates[chatId] = {}; // holatni tozalash
    
    bot.sendMessage(chatId, 
        `Assalomu alaykum, *Safo Marva Tour* admin botiga xush kelibsiz!\n\n` +
        `Ushbu bot yordamida veb-saytdagi ziyorat paketlarini boshqarishingiz mumkin.\n\n` +
        `📌 *Asosiy imkoniyatlar:* \n` +
        `• Paketlar narxini istalgan vaqtda o'zgartirish\n` +
        `• Yangi paketlar qo'shish va ularni saytda chiqarish\n` +
        `• Keraksiz paketlarni o'chirish`, 
        { parse_mode: 'Markdown', ...mainMenuKeyboard }
    );
});

// Xabarlarni eshitish va holatlarni tekshirish
bot.on('message', async (msg) => {
    const chatId = msg.chat.id;
    const text = msg.text;

    if (!text) return;

    // Bekor qilish komandasi
    if (text === '❌ Bekor qilish') {
        userStates[chatId] = {};
        return bot.sendMessage(chatId, 'Amal bekor qilindi.', mainMenuKeyboard);
    }

    const state = userStates[chatId]?.step;

    // Asosiy tugmalar logikasi
    if (!state) {
        switch (text) {
            case '📦 Paketlar Ro\'yxati':
                return listPackages(chatId);
            case '➕ Yangi Paket Qo\'shish':
                userStates[chatId] = { step: 'WAITING_FOR_KEY_NAME' };
                return bot.sendMessage(chatId, 
                    `📝 *Yangi paket qo'shish (1/4)*\n\n` +
                    `Paket uchun inglizcha kalit so'z kiriting.\n` +
                    `*(Faqat kichik inglizcha harflar va pastki chiziq, masalan: comfort_plus, premium_15):*`, 
                    { parse_mode: 'Markdown', ...cancelKeyboard }
                );
            case '✏️ Narxni O\'zgartirish':
                return showPriceChangeList(chatId);
            case '❌ Paketni O\'chirish':
                return showDeleteList(chatId);
        }
    }

    // Holatlar zanjiri (State Machine)
    if (state === 'WAITING_FOR_KEY_NAME') {
        const keyName = text.trim().toLowerCase().replace(/[^a-z0-9_]/g, '');
        if (!keyName) {
            return bot.sendMessage(chatId, '⚠️ Xato! Kalit so\'zda faqat inglizcha harflar, raqamlar va pastki chiziq bo\'lishi kerak. Qaytadan kiriting:');
        }
        
        // Key unique ekanligini tekshirish
        const { data } = await supabase.from('packages').select('id').eq('key_name', keyName).single();
        if (data) {
            return bot.sendMessage(chatId, '⚠️ Ushbu kalit nomdagi paket allaqachon mavjud. Boshqa nom kiriting:');
        }

        userStates[chatId].key_name = keyName;
        userStates[chatId].step = 'WAITING_FOR_DISPLAY_NAME';
        return bot.sendMessage(chatId, '✏️ *Paket nomini kiriting (2/4)*\n*(masalan: Komfort Plus Paket):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_DISPLAY_NAME') {
        const displayName = text.trim();
        if (displayName.length < 3) {
            return bot.sendMessage(chatId, '⚠️ Paket nomi kamida 3 ta harfdan iborat bo\'lishi kerak. Qaytadan kiriting:');
        }

        userStates[chatId].display_name = displayName;
        userStates[chatId].step = 'WAITING_FOR_PRICE';
        return bot.sendMessage(chatId, '💵 *Boshlang\'ich narxni faqat raqamlarda kiriting (3/4)*\n*(masalan: 1250):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_PRICE') {
        const price = parseInt(text.trim().replace(/[^0-9]/g, ''), 10);
        if (isNaN(price) || price <= 0) {
            return bot.sendMessage(chatId, '⚠️ Narxni to\'g\'ri formatda kiriting (faqat musbat raqamlar):');
        }

        userStates[chatId].price = price.toString();
        userStates[chatId].step = 'WAITING_FOR_DESCRIPTION';
        return bot.sendMessage(chatId, '📝 *Paket tavsifini kiriting (4/4)*\n*(masalan: Qulay mehmonxona, sifatli transport va mazali taomlar):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_DESCRIPTION') {
        const description = text.trim();
        const pkgData = userStates[chatId];
        
        bot.sendMessage(chatId, '⏳ Paket ma\'lumotlar bazasiga saqlanmoqda...');

        const { data, error } = await supabase.from('packages').insert([{
            key_name: pkgData.key_name,
            display_name: pkgData.display_name,
            price: pkgData.price,
            description: description
        }]).select();

        if (error) {
            console.error(error);
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${error.message}\n\nAgarda RLS xatosi bo'lsa, .env faylida 'SUPABASE_SERVICE_ROLE_KEY' o'rnatilganligini tekshiring.`, mainMenuKeyboard);
        } else {
            bot.sendMessage(chatId, 
                `✅ *Muvaffaqiyatli qo'shildi!* \n\n` +
                `📦 *Paket:* ${pkgData.display_name}\n` +
                `💵 *Narxi:* $${pkgData.price}\n` +
                `📝 *Tavsif:* ${description}\n\n` +
                `Siz qo'shgan paket darhol veb-saytda aks etadi.`, 
                { parse_mode: 'Markdown', ...mainMenuKeyboard }
            );
        }
        userStates[chatId] = {};
    }

    if (state === 'WAITING_FOR_NEW_PRICE') {
        const price = parseInt(text.trim().replace(/[^0-9]/g, ''), 10);
        if (isNaN(price) || price <= 0) {
            return bot.sendMessage(chatId, '⚠️ Narxni to\'g\'ri formatda kiriting (faqat musbat raqamlar):');
        }

        const pkgId = userStates[chatId].pkgId;
        const pkgName = userStates[chatId].pkgName;

        bot.sendMessage(chatId, '⏳ Narx yangilanmoqda...');

        const { error } = await supabase.from('packages').update({ price: price.toString() }).eq('id', pkgId);

        if (error) {
            console.error(error);
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${error.message}`, mainMenuKeyboard);
        } else {
            bot.sendMessage(chatId, `✅ *${pkgName}* narxi muvaffaqiyatli *$${price}* ga o'zgartirildi! Saytda yangilandi.`, { parse_mode: 'Markdown', ...mainMenuKeyboard });
        }
        userStates[chatId] = {};
    }
});

// Inline klaviatura hodisalarini eshitish
bot.on('callback_query', async (query) => {
    const chatId = query.message.chat.id;
    const data = query.data;

    bot.answerCallbackQuery(query.id);

    if (data.startsWith('edit_price_')) {
        const pkgId = parseInt(data.replace('edit_price_', ''), 10);
        
        // Paket ma'lumotlarini olish
        const { data: pkg, error } = await supabase.from('packages').select('*').eq('id', pkgId).single();
        if (error || !pkg) {
            return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        }

        userStates[chatId] = { step: 'WAITING_FOR_NEW_PRICE', pkgId: pkgId, pkgName: pkg.display_name };
        
        bot.sendMessage(chatId, 
            `✏️ *Narxni o'zgartirish: ${pkg.display_name}*\n` +
            `Hozirgi narxi: *$${pkg.price}*\n\n` +
            `Yangi narxni faqat raqamlarda kiriting:`, 
            { parse_mode: 'Markdown', ...cancelKeyboard }
        );
    }

    if (data.startsWith('delete_pkg_')) {
        const pkgId = parseInt(data.replace('delete_pkg_', ''), 10);

        // Paket ma'lumotlarini olish
        const { data: pkg } = await supabase.from('packages').select('*').eq('id', pkgId).single();
        if (!pkg) {
            return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        }

        const keyboard = {
            reply_markup: {
                inline_keyboard: [
                    [
                        { text: '✅ Ha, o\'chirish', callback_data: `confirm_del_${pkgId}` },
                        { text: '❌ Yo\'q, bekor qilish', callback_data: 'cancel_action' }
                    ]
                ]
            }
        };

        bot.sendMessage(chatId, `⚠️ Rostdan ham *${pkg.display_name}* paketini o'chirib tashlamoqchimisiz? Bu amalni orqaga qaytarib bo'lmaydi.`, { parse_mode: 'Markdown', ...keyboard });
    }

    if (data.startsWith('confirm_del_')) {
        const pkgId = parseInt(data.replace('confirm_del_', ''), 10);

        bot.sendMessage(chatId, '⏳ Paket o\'chirilmoqda...');
        const { error } = await supabase.from('packages').delete().eq('id', pkgId);

        if (error) {
            console.error(error);
            bot.sendMessage(chatId, `❌ O'chirishda xatolik: ${error.message}`);
        } else {
            bot.sendMessage(chatId, '✅ Paket muvaffaqiyatli o\'chirildi va saytdan yo\'qoldi!');
        }
    }

    if (data === 'cancel_action') {
        bot.sendMessage(chatId, 'Amal bekor qilindi.', mainMenuKeyboard);
    }
});

// Barcha paketlarni ko'rish funksiyasi
async function listPackages(chatId) {
    bot.sendMessage(chatId, '⏳ Paketlar yuklanmoqda...');
    
    const { data: packages, error } = await supabase.from('packages').select('*').order('id', { ascending: true });

    if (error) {
        return bot.sendMessage(chatId, `❌ Ma'lumotlarni olishda xatolik: ${error.message}`);
    }

    if (!packages || packages.length === 0) {
        return bot.sendMessage(chatId, '📦 Hozircha hech qanday paket mavjud emas.');
    }

    let message = `📦 *Mavjud Paketlar Ro'yxati:*\n\n`;
    packages.forEach(pkg => {
        message += `🔹 *${pkg.display_name}* (\`${pkg.key_name}\`)\n`;
        message += `💵 Narx: *$${pkg.price}*\n`;
        message += `📝 Tavsif: _${pkg.description || 'Kiritilmagan'}_\n`;
        message += `-------------------------\n\n`;
    });

    bot.sendMessage(chatId, message, { parse_mode: 'Markdown', ...mainMenuKeyboard });
}

// Narxni o'zgartirish ro'yxatini ko'rsatish
async function showPriceChangeList(chatId) {
    const { data: packages, error } = await supabase.from('packages').select('*').order('id', { ascending: true });

    if (error || !packages || packages.length === 0) {
        return bot.sendMessage(chatId, '✏️ Narxini o\'zgartirish uchun paketlar topilmadi.');
    }

    const inlineKeyboard = packages.map(pkg => {
        return [{ text: `${pkg.display_name} ($${pkg.price})`, callback_data: `edit_price_${pkg.id}` }];
    });

    bot.sendMessage(chatId, '✏️ Narxini o\'zgartirmoqchi bo\'lgan paketingizni tanlang:', {
        reply_markup: { inline_keyboard: inlineKeyboard }
    });
}

// O'chirish ro'yxatini ko'rsatish
async function showDeleteList(chatId) {
    const { data: packages, error } = await supabase.from('packages').select('*').order('id', { ascending: true });

    if (error || !packages || packages.length === 0) {
        return bot.sendMessage(chatId, '❌ O\'chirish uchun paketlar topilmadi.');
    }

    const inlineKeyboard = packages.map(pkg => {
        return [{ text: `❌ ${pkg.display_name} ($${pkg.price})`, callback_data: `delete_pkg_${pkg.id}` }];
    });

    bot.sendMessage(chatId, '❌ O\'chirmoqchi bo\'lgan paketingizni tanlang (Diqqat qiling!):', {
        reply_markup: { inline_keyboard: inlineKeyboard }
    });
}
