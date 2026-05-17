/**
 * Safo Marva Tour - Premium Admin Telegram Bot
 * Fully integrated with Supabase and Express API.
 */

const TelegramBot = require('node-telegram-bot-api');
const fs = require('fs');
const path = require('path');
const db = require('./db');

const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || process.env.BOT_TOKEN || '8843389342:AAFcyDSoofSqojZZWW2IugS4dL3mL-PWtq4';

// Start Bot in Polling Mode
const bot = new TelegramBot(BOT_TOKEN, { polling: true });

console.log("🟢 Safo Marva Tour Premium Admin Bot started via Polling!");

// State Management for Multi-step forms
const userStates = {};

// Main Menu Keyboard
const mainMenuKeyboard = {
    reply_markup: {
        keyboard: [
            [{ text: '📦 Paketlar' }, { text: '📊 Statistika' }],
            [{ text: '➕ Yangi Paket' }]
        ],
        resize_keyboard: true
    }
};

// Cancel Keyboard
const cancelKeyboard = {
    reply_markup: {
        keyboard: [[{ text: '❌ Bekor qilish' }]],
        resize_keyboard: true
    }
};

// Welcome Command
bot.onText(/\/start/, (msg) => {
    const chatId = msg.chat.id;
    userStates[chatId] = {}; // Reset state
    
    bot.sendMessage(chatId, 
        `👋 *Assalomu alaykum, Safo Marva Tour Admin Tizimiga Xush Kelibsiz!*\n\n` +
        `Ushbu bot yordamida siz veb-saytingizdagi ziyorat paketlarini boshqara olasiz:\n\n` +
        `📦 *Paketlar:* Narxlarni va tavsiflarni tahrirlash yoki o'chirish\n` +
        `➕ *Yangi Paket:* Saytga yangi ziyorat paketlarini qo'shish\n` +
        `📊 *Statistika:* Loyihangizdagi umumiy paketlar va murojaatlar soni\n\n` +
        `👇 Boshlash uchun quyidagi menyudan foydalaning:`, 
        { parse_mode: 'Markdown', ...mainMenuKeyboard }
    );
});

// Message Listener & Router
bot.on('message', async (msg) => {
    const chatId = msg.chat.id;
    const text = msg.text;
    const state = userStates[chatId]?.step;

    // Rasm yoki video yuklanayotganda text bo'lmasligi mumkin, shuning uchun o'tkazib yuboramiz
    if (!text && state !== 'WAITING_FOR_MEDIA') return;

    // Handle Cancel Action
    if (text === '❌ Bekor qilish') {
        userStates[chatId] = {};
        return bot.sendMessage(chatId, '🏠 Bosh menyuga qaytildi. Amal bekor qilindi.', mainMenuKeyboard);
    }


    // Main buttons routing
    if (!state) {
        switch (text) {
            case '📦 Paketlar':
                return sendPackagesCatalog(chatId);
            case '➕ Yangi Paket':
                userStates[chatId] = { step: 'WAITING_FOR_KEY_NAME' };
                return bot.sendMessage(chatId, 
                    `📝 *Yangi Paket Yaratish (1/4)*\n\n` +
                    `Paket uchun inglizcha kalit so'z kiriting.\n` +
                    `*(Faqat kichik harflar va pastki chiziq, masalan: comfort_plus, premium_15):*`, 
                    { parse_mode: 'Markdown', ...cancelKeyboard }
                );
            case '📊 Statistika':
                return sendStatistics(chatId);
        }
    }

    // State machine for adding package
    if (state === 'WAITING_FOR_KEY_NAME') {
        const keyName = text.trim().toLowerCase().replace(/[^a-z0-9_]/g, '');
        if (!keyName) {
            return bot.sendMessage(chatId, '⚠️ Xatolik! Kalit so\'zda faqat inglizcha harflar, raqamlar va pastki chiziq bo\'lishi kerak. Qayta urinib ko\'ring:');
        }

        const { rows } = await db.query('SELECT id FROM packages WHERE key_name = $1', [keyName]);
        if (rows.length > 0) {
            return bot.sendMessage(chatId, '⚠️ Bunday kalit nomli paket allaqachon mavjud. Iltimos boshqacha kiriting:');
        }

        userStates[chatId].key_name = keyName;
        userStates[chatId].step = 'WAITING_FOR_DISPLAY_NAME';
        return bot.sendMessage(chatId, '🏷 *Paket nomini kiriting (2/4)*\n*(masalan: Komfort Plus Paket):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_DISPLAY_NAME') {
        const displayName = text.trim();
        if (displayName.length < 3) {
            return bot.sendMessage(chatId, '⚠️ Paket nomi kamida 3 ta harfdan iborat bo\'lishi kerak. Qayta kiriting:');
        }

        userStates[chatId].display_name = displayName;
        userStates[chatId].step = 'WAITING_FOR_PRICE';
        return bot.sendMessage(chatId, '💵 *Boshlang\'ich narxni faqat raqamlarda kiriting (3/4)*\n*(masalan: 1350):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_PRICE') {
        const price = parseInt(text.trim().replace(/[^0-9]/g, ''), 10);
        if (isNaN(price) || price <= 0) {
            return bot.sendMessage(chatId, '⚠️ Narxni to\'g\'ri formatda kiriting (faqat musbat sonlar):');
        }

        userStates[chatId].price = price.toString();
        userStates[chatId].step = 'WAITING_FOR_DESCRIPTION';
        return bot.sendMessage(chatId, '📝 *Paket tavsifini kiriting (4/4)*\n*(masalan: Qulay transport, 5 yulduzli mehmonxona, maxsus viza):*', { parse_mode: 'Markdown' });
    }

    if (state === 'WAITING_FOR_DESCRIPTION') {
        const description = text.trim();
        const pkgData = userStates[chatId];

        bot.sendMessage(chatId, '⏳ Paket ma\'lumotlar bazasiga yozilmoqda...');

        try {
            await db.query(
                'INSERT INTO packages (key_name, display_name, price, description) VALUES ($1, $2, $3, $4)',
                [pkgData.key_name, pkgData.display_name, pkgData.price, description]
            );
            bot.sendMessage(chatId, 
                `🎉 *Muvaffaqiyatli Qo'shildi!* \n\n` +
                `📦 *Paket:* ${pkgData.display_name}\n` +
                `💵 *Narxi:* $${pkgData.price}\n` +
                `📝 *Tavsif:* ${description}\n\n` +
                `Yangi paket darhol veb-saytda aks etadi.`, 
                { parse_mode: 'Markdown', ...mainMenuKeyboard }
            );
        } catch (err) {
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${err.message}`, mainMenuKeyboard);
        }
        userStates[chatId] = {};
    }

    // State machine for uploading media
    if (state === 'WAITING_FOR_MEDIA') {
        let fileId = null;
        let isVideo = false;
        let ext = 'jpg';

        if (msg.photo && msg.photo.length > 0) {
            fileId = msg.photo[msg.photo.length - 1].file_id;
            ext = 'jpg';
        } else if (msg.video) {
            fileId = msg.video.file_id;
            isVideo = true;
            ext = 'mp4';
        } else if (msg.document) {
            fileId = msg.document.file_id;
            const mime = msg.document.mime_type || '';
            if (mime.startsWith('video/')) {
                isVideo = true;
                ext = 'mp4';
            } else if (mime.startsWith('image/')) {
                ext = 'jpg';
            } else {
                ext = msg.document.file_name?.split('.').pop() || 'jpg';
            }
        }

        if (!fileId) {
            return bot.sendMessage(chatId, '⚠️ Iltimos, faqat rasm yoki video fayl yuboring:');
        }

        const pkgId = userStates[chatId].pkgId;
        const pkgName = userStates[chatId].pkgName;
        const pkgKey = userStates[chatId].pkgKey;

        bot.sendMessage(chatId, '⏳ Fayl serverga yuklab olinmoqda, iltimos kuting...');

        try {
            const fileDetails = await bot.getFile(fileId);
            const filePath = fileDetails.file_path;
            const fileExt = filePath.split('.').pop() || ext;

            const newFilename = `custom_${pkgKey}_${Date.now()}.${fileExt}`;
            const targetPath = path.join('/Users/macbookpro/Desktop/Safo_Marva_Tour/galereya', newFilename);
            const relativePath = `galereya/${newFilename}`;

            const downloadedPath = await bot.downloadFile(fileId, '/Users/macbookpro/Desktop/Safo_Marva_Tour/galereya');
            fs.renameSync(downloadedPath, targetPath);

            if (isVideo) {
                updateLocalMedia(pkgKey, { video_url: relativePath, image_url: "" });
            } else {
                updateLocalMedia(pkgKey, { image_url: relativePath, video_url: "" });
            }

            bot.sendMessage(chatId, `✅ *${pkgName}* uchun yangi media fayl muvaffaqiyatli yuklandi va saytda yangilandi!`, { parse_mode: 'Markdown', ...mainMenuKeyboard });
        } catch (err) {
            console.error("Error downloading file:", err);
            bot.sendMessage(chatId, `❌ Faylni yuklashda xatolik yuz berdi: ${err.message}`, mainMenuKeyboard);
        }

        userStates[chatId] = {};
        return;
    }

    // State machine for changing name
    if (state === 'WAITING_FOR_NEW_NAME') {
        const newName = text.trim();
        if (newName.length < 3) {
            return bot.sendMessage(chatId, '⚠️ Paket nomi kamida 3 ta harfdan iborat bo\'lishi kerak. Qayta kiriting:');
        }

        const pkgId = userStates[chatId].pkgId;
        const oldName = userStates[chatId].pkgName;

        bot.sendMessage(chatId, '⏳ Nomi yangilanmoqda...');
        try {
            await db.query('UPDATE packages SET display_name = $1 WHERE id = $2', [newName, pkgId]);
            bot.sendMessage(chatId, `✅ Paket nomi muvaffaqiyatli *${oldName}* dan *${newName}* ga o'zgartirildi! Saytda yangilandi.`, { parse_mode: 'Markdown', ...mainMenuKeyboard });
        } catch (err) {
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${err.message}`, mainMenuKeyboard);
        }
        userStates[chatId] = {};
    }

    // State machine for changing price
    if (state === 'WAITING_FOR_NEW_PRICE') {
        const price = parseInt(text.trim().replace(/[^0-9]/g, ''), 10);
        if (isNaN(price) || price <= 0) {
            return bot.sendMessage(chatId, '⚠️ Narxni to\'g\'ri formatda kiriting (faqat musbat sonlar):');
        }

        const pkgId = userStates[chatId].pkgId;
        const pkgName = userStates[chatId].pkgName;

        bot.sendMessage(chatId, '⏳ Narx yangilanmoqda...');
        try {
            await db.query('UPDATE packages SET price = $1 WHERE id = $2', [price.toString(), pkgId]);
            bot.sendMessage(chatId, `✅ *${pkgName}* narxi muvaffaqiyatli *$${price}* ga o'zgartirildi!`, { parse_mode: 'Markdown', ...mainMenuKeyboard });
        } catch (err) {
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${err.message}`, mainMenuKeyboard);
        }
        userStates[chatId] = {};
    }

    // State machine for changing description
    if (state === 'WAITING_FOR_NEW_DESC') {
        const description = text.trim();
        const pkgId = userStates[chatId].pkgId;
        const pkgName = userStates[chatId].pkgName;

        bot.sendMessage(chatId, '⏳ Tavsif yangilanmoqda...');
        try {
            await db.query('UPDATE packages SET description = $1 WHERE id = $2', [description, pkgId]);
            bot.sendMessage(chatId, `✅ *${pkgName}* tavsifi muvaffaqiyatli yangilandi!`, { parse_mode: 'Markdown', ...mainMenuKeyboard });
        } catch (err) {
            bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${err.message}`, mainMenuKeyboard);
        }
        userStates[chatId] = {};
    }
});

// Inline Button Actions (Callback Queries)
bot.on('callback_query', async (query) => {
    const chatId = query.message.chat.id;
    const messageId = query.message.message_id;
    const data = query.data;

    bot.answerCallbackQuery(query.id);

    // 1. Back to packages catalog
    if (data === 'back_to_catalog') {
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return sendPackagesCatalog(chatId);
    }

    // 2. Open specific package details
    if (data.startsWith('view_pkg_')) {
        const pkgId = parseInt(data.replace('view_pkg_', ''), 10);
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return sendPackageDetails(chatId, pkgId);
    }

    // Edit Media
    if (data.startsWith('edit_media_')) {
        const pkgId = parseInt(data.replace('edit_media_', ''), 10);
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        const pkg = rows[0];

        userStates[chatId] = { step: 'WAITING_FOR_MEDIA', pkgId, pkgName: pkg.display_name, pkgKey: pkg.key_name };
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `🖼 *Rasm yoki Video yuklash: ${pkg.display_name}*\n\n` +
            `Ushbu paket uchun veb-saytda ko'rsatiladigan yangi Rasm yoki Video faylini yuboring.\n` +
            `*(Faylni botga rasm (Photo) yoki video (Video) shaklida to'g'ridan-to'g'ri jo'nating):*`, 
            { parse_mode: 'Markdown', ...cancelKeyboard }
        );
    }

    // Edit Name
    if (data.startsWith('edit_name_')) {
        const pkgId = parseInt(data.replace('edit_name_', ''), 10);
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        const pkg = rows[0];

        userStates[chatId] = { step: 'WAITING_FOR_NEW_NAME', pkgId, pkgName: pkg.display_name };
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `🏷 *Nomini o'zgartirish: ${pkg.display_name}*\n` +
            `Hozirgi nomi: *${pkg.display_name}*\n\n` +
            `Yangi nomini yuboring:`, 
            { parse_mode: 'Markdown', ...cancelKeyboard }
        );
    }

    // 3. Edit Price
    if (data.startsWith('edit_price_')) {
        const pkgId = parseInt(data.replace('edit_price_', ''), 10);
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        const pkg = rows[0];

        userStates[chatId] = { step: 'WAITING_FOR_NEW_PRICE', pkgId, pkgName: pkg.display_name };
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `💵 *Narxni o'zgartirish: ${pkg.display_name}*\n` +
            `Hozirgi narx: *$${pkg.price}*\n\n` +
            `Yangi narxni raqamlarda kiriting:`, 
            { parse_mode: 'Markdown', ...cancelKeyboard }
        );
    }

    // 4. Edit Description
    if (data.startsWith('edit_desc_')) {
        const pkgId = parseInt(data.replace('edit_desc_', ''), 10);
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        const pkg = rows[0];

        userStates[chatId] = { step: 'WAITING_FOR_NEW_DESC', pkgId, pkgName: pkg.display_name };
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `📝 *Tavsifni o'zgartirish: ${pkg.display_name}*\n` +
            `Hozirgi tavsif: _${pkg.description || 'Kiritilmagan'}_\n\n` +
            `Yangi tavsifni yuboring:`, 
            { parse_mode: 'Markdown', ...cancelKeyboard }
        );
    }

    // 5. Delete package confirmation
    if (data.startsWith('delete_pkg_')) {
        const pkgId = parseInt(data.replace('delete_pkg_', ''), 10);
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Paket topilmadi.');
        const pkg = rows[0];

        const inlineKeyboard = {
            reply_markup: {
                inline_keyboard: [
                    [
                        { text: '✅ Ha, o‘chirish', callback_data: `confirm_del_pkg_${pkgId}` },
                        { text: '❌ Yo‘q, bekor qilish', callback_data: `view_pkg_${pkgId}` }
                    ]
                ]
            }
        };

        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `🚨 *DIQQAT!* \n\nRostdan ham *${pkg.display_name}* paketini butunlay o'chirib tashlamoqchimisiz?\n` +
            `Bu amal saytdan ham yo'qoladi va ortga qaytarib bo'lmaydi.`, 
            { parse_mode: 'Markdown', ...inlineKeyboard }
        );
    }

    // 6. Confirm Delete package
    if (data.startsWith('confirm_del_pkg_')) {
        const pkgId = parseInt(data.replace('confirm_del_pkg_', ''), 10);
        bot.deleteMessage(chatId, messageId).catch(() => {});

        try {
            await db.query('DELETE FROM packages WHERE id = $1', [pkgId]);
            bot.sendMessage(chatId, '✅ Paket muvaffaqiyatli o\'chirildi va saytdan olingan.', mainMenuKeyboard);
        } catch (err) {
            bot.sendMessage(chatId, `❌ O'chirishda xatolik: ${err.message}`, mainMenuKeyboard);
        }
    }

    // 7. View lead details
    if (data.startsWith('view_lead_')) {
        const leadId = parseInt(data.replace('view_lead_', ''), 10);
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return sendLeadDetails(chatId, leadId);
    }

    // 8. Delete lead
    if (data.startsWith('del_lead_')) {
        const leadId = parseInt(data.replace('del_lead_', ''), 10);
        const { rows } = await db.query('SELECT * FROM leads WHERE id = $1', [leadId]);
        if (rows.length === 0) return bot.sendMessage(chatId, '⚠️ Murojaat topilmadi.');
        const lead = rows[0];

        const inlineKeyboard = {
            reply_markup: {
                inline_keyboard: [
                    [
                        { text: '✅ Ha, o‘chirish', callback_data: `confirm_del_lead_${leadId}` },
                        { text: '❌ Yo‘q, orqaga', callback_data: `view_lead_${leadId}` }
                    ]
                ]
            }
        };

        bot.deleteMessage(chatId, messageId).catch(() => {});
        return bot.sendMessage(chatId, 
            `🗑 *Murojaatni O'chirish* \n\nRostdan ham *${lead.name}* tomonidan qoldirilgan murojaatni o'chirib tashlamoqchimisiz?`, 
            { parse_mode: 'Markdown', ...inlineKeyboard }
        );
    }

    // 9. Confirm Delete lead
    if (data.startsWith('confirm_del_lead_')) {
        const leadId = parseInt(data.replace('confirm_del_lead_', ''), 10);
        bot.deleteMessage(chatId, messageId).catch(() => {});

        try {
            await db.query('DELETE FROM leads WHERE id = $1', [leadId]);
            bot.sendMessage(chatId, '✅ Murojaat ro\'yxatdan muvaffaqiyatli o\'chirildi!');
            return sendLeadsList(chatId);
        } catch (err) {
            bot.sendMessage(chatId, `❌ Xatolik: ${err.message}`);
        }
    }

    // 10. Back to leads list
    if (data === 'back_to_leads') {
        bot.deleteMessage(chatId, messageId).catch(() => {});
        return sendLeadsList(chatId);
    }
});

// --- HELPER FUNCTIONS ---

// 1. Fetch & Send Packages Catalog (Interactive)
async function sendPackagesCatalog(chatId) {
    bot.sendMessage(chatId, '⏳ Paketlar ro\'yxati olinmoqda...');

    try {
        const { rows: packages } = await db.query('SELECT * FROM packages ORDER BY id ASC');

        if (!packages || packages.length === 0) {
            return bot.sendMessage(chatId, '📦 Hozircha hech qanday paket mavjud emas.');
        }

        const inlineKeyboard = packages.map(pkg => {
            return [{ text: `🔹 ${pkg.display_name} ($${pkg.price})`, callback_data: `view_pkg_${pkg.id}` }];
        });

        bot.sendMessage(chatId, 
            `📦 *Safo Marva Tour Paketlar Katalogi*\n\n` +
            `Batafsil ko'rish, narxini o'zgartirish yoki tahrirlash uchun kerakli paketni tanlang:`, 
            {
                parse_mode: 'Markdown',
                reply_markup: { inline_keyboard: inlineKeyboard }
            }
        );
    } catch (err) {
        bot.sendMessage(chatId, `❌ Xatolik yuz berdi: ${err.message}`);
    }
}

// 2. Send Package Details Page
async function sendPackageDetails(chatId, pkgId) {
    try {
        const { rows } = await db.query('SELECT * FROM packages WHERE id = $1', [pkgId]);
        if (rows.length === 0) {
            return bot.sendMessage(chatId, '❌ Paket topilmadi.');
        }
        const pkg = rows[0];

        const message = 
            `📦 *Paket Tafsilotlari:*\n\n` +
            `🔹 *Nomi:* ${pkg.display_name}\n` +
            `🔑 *Kalit so'z:* \`${pkg.key_name}\`\n` +
            `💵 *Narx:* $${pkg.price}\n` +
            `📝 *Tavsif:* _${pkg.description || 'Kiritilmagan'}_\n\n` +
            `*Quyidagi amallardan birini tanlang:*`;

        const inlineKeyboard = {
            reply_markup: {
                inline_keyboard: [
                    [
                        { text: '🏷 Nomini tahrirlash', callback_data: `edit_name_${pkg.id}` },
                        { text: '🖼 Rasm/Video yuklash', callback_data: `edit_media_${pkg.id}` }
                    ],
                    [
                        { text: '💵 Narxni o\'zgartirish', callback_data: `edit_price_${pkg.id}` },
                        { text: '📝 Tavsifni tahrirlash', callback_data: `edit_desc_${pkg.id}` }
                    ],
                    [
                        { text: '❌ Paketni o\'chirish', callback_data: `delete_pkg_${pkg.id}` }
                    ],
                    [
                        { text: '🔙 Orqaga', callback_data: 'back_to_catalog' }
                    ]
                ]
            }
        };

        bot.sendMessage(chatId, message, { parse_mode: 'Markdown', ...inlineKeyboard });
    } catch (err) {
        bot.sendMessage(chatId, '❌ Paket ma\'lumotlarini olib bo\'lmadi.');
    }
}

// 3. Fetch & Send Website Leads
async function sendLeadsList(chatId) {
    bot.sendMessage(chatId, '⏳ Kelgan murojaatlar olinmoqda...');

    try {
        const { rows: leads } = await db.query('SELECT * FROM leads ORDER BY id DESC LIMIT 10');

        if (!leads || leads.length === 0) {
            return bot.sendMessage(chatId, '👥 Hozircha saytdan hech qanday murojaat kelmagan.');
        }

        const inlineKeyboard = leads.map((lead, idx) => {
            return [{ text: `${idx + 1}. ${lead.name} (${lead.phone})`, callback_data: `view_lead_${lead.id}` }];
        });

        bot.sendMessage(chatId, 
            `👥 *Oxirgi 10 ta kelgan Murojaatlar* \n\n` +
            `Mijozning to'liq ma'lumotlarini ko'rish yoki uni ro'yxatdan o'chirish uchun ustiga bosing:`, 
            {
                parse_mode: 'Markdown',
                reply_markup: { inline_keyboard: inlineKeyboard }
            }
        );
    } catch (err) {
        bot.sendMessage(chatId, `❌ Murojaatlarni olishda xatolik: ${err.message}`);
    }
}

// 4. Send Lead Details Page
async function sendLeadDetails(chatId, leadId) {
    try {
        const { rows } = await db.query('SELECT * FROM leads WHERE id = $1', [leadId]);
        if (rows.length === 0) {
            return bot.sendMessage(chatId, '❌ Murojaat topilmadi.');
        }
        const lead = rows[0];

        const cleanPhone = lead.phone.replace(/\s/g, '');
        const message = 
            `👤 *Mijoz Ma'lumotlari:*\n\n` +
            `👤 *Ismi:* ${lead.name}\n` +
            `📞 *Telefon:* \`${lead.phone}\`\n` +
            `📦 *Tanlangan paket:* ${lead.package}\n` +
            `🛏 *Xona turi:* ${lead.room || 'Kiritilmagan'}\n` +
            `🌐 *Manba:* ${lead.source || 'Veb-sayt'}\n\n` +
            `📞 *Mijoz bilan bog'lanish:* Telegramda raqam ustiga bosib qo'ng'iroq qilishingiz yoki yozishingiz mumkin.`;

        const inlineKeyboard = {
            reply_markup: {
                inline_keyboard: [
                    [
                        { text: '📞 Qo\'ng\'iroq qilish (Direct)', url: `tel:${cleanPhone}` }
                    ],
                    [
                        { text: '🗑 Murojaatni o\'chirish', callback_data: `del_lead_${lead.id}` }
                    ],
                    [
                        { text: '🔙 Ro\'yxatga qaytish', callback_data: 'back_to_leads' }
                    ]
                ]
            }
        };

        bot.sendMessage(chatId, message, { parse_mode: 'Markdown', ...inlineKeyboard });
    } catch (err) {
        bot.sendMessage(chatId, '❌ Murojaat tafsilotlarini olib bo\'lmadi.');
    }
}

// 5. Statistics Panel
async function sendStatistics(chatId) {
    bot.sendMessage(chatId, '⏳ Statistika hisoblanmoqda...');

    try {
        const pkgRes = await db.query('SELECT COUNT(*) FROM packages');
        const leadRes = await db.query('SELECT COUNT(*) FROM leads');

        const pkgCount = pkgRes.rows[0].count;
        const leadCount = leadRes.rows[0].count;

        const message = 
            `📊 *Safo Marva Tour Umumiy Statistikasi:*\n\n` +
            `📦 *Faol Ziyorat Paketlari:* \`${pkgCount || 0}\` ta\n` +
            `👥 *Kelib tushgan jami so'rovlar (Leads):* \`${leadCount || 0}\` ta\n\n` +
            `💡 _Maslahat: Murojaatlar ro'yxatini ko'rish va ularga javob qaytarish uchun 'Murojaatlar' tugmasidan foydalaning._`;

        bot.sendMessage(chatId, message, { parse_mode: 'Markdown', ...mainMenuKeyboard });
    } catch (err) {
        bot.sendMessage(chatId, `❌ Statistikani olishda xatolik: ${err.message}`, mainMenuKeyboard);
    }
}

function updateLocalMedia(key, updates) {
    const mediaFile = path.join(__dirname, 'packages_media.json');
    let media = {};
    try {
        if (fs.existsSync(mediaFile)) {
            media = JSON.parse(fs.readFileSync(mediaFile, 'utf8'));
        }
    } catch (e) { console.error("Error reading media inside bot", e); }
    
    if (!media[key]) media[key] = {};
    
    if (updates.video_url) media[key].image_url = "";
    if (updates.image_url) media[key].video_url = "";
    
    Object.assign(media[key], updates);
    
    try {
        fs.writeFileSync(mediaFile, JSON.stringify(media, null, 2), 'utf8');
    } catch (e) { console.error("Error writing media inside bot", e); }
}
