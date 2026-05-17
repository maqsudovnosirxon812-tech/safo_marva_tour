// server.js – Express backend for Safo Marva Tour
// Handles Telegram bot webhook, provides admin REST API, and accepts booking leads safely.

require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors()); // Allow requests from local frontend (file://) or other domains
app.use(bodyParser.json());

// Serve frontend static files (index.html, images, videos)
app.use(express.static(__dirname));

// ==================== 1. Lead / Booking Submission API ====================
// Frontend will POST booking/lead requests here
app.post('/api/leads', async (req, res) => {
  const { name, phone, package: selectedPackage, room } = req.body;

  // Validation
  if (!name || !phone || !selectedPackage) {
    return res.status(400).json({ error: 'Name, phone and package are required.' });
  }

  // Clean data to prevent HTML/XSS injection
  const cleanName = name.trim().replace(/[<>]/g, "");
  let cleanPhone = phone.trim().replace(/[^+0-9]/g, "");
  if (!cleanPhone.startsWith('+')) {
    cleanPhone = '+' + cleanPhone;
  }

  try {
    // 1. Save lead to PostgreSQL
    let dbSaved = false;
    try {
      await db.query(
        'INSERT INTO leads (name, phone, package, room, source) VALUES ($1, $2, $3, $4, $5)',
        [cleanName, cleanPhone, selectedPackage, room || 'Kiritilmagan', 'web-site']
      );
      dbSaved = true;
      console.log(`✅ Lead successfully saved to PostgreSQL: ${cleanName}`);
    } catch (dbErr) {
      console.warn("⚠️ PostgreSQL lead insertion warning:", dbErr.message);
      // We will still send the Telegram notification even if database save fails
    }

    // 2. Send Telegram Notification to Admins
    const leadBotToken = process.env.LEAD_BOT_TOKEN || process.env.BOT_TOKEN || '8726481674:AAEqvxqyqLsvrN3vIuDXi-2jJRJkPVPp0sc';
    const adminIdsStr = process.env.TELEGRAM_ADMINS || '6448561095,1809187274';
    const adminIds = adminIdsStr.split(',').map(id => id.trim());

    const now = new Date().toLocaleString("ru-RU", { timeZone: "Asia/Tashkent" });
    const roomInfo = selectedPackage === "14 Kunlik Paket" && room ? `\n🛏 <b>Xona turi:</b> ${room}` : "";

    const message = `🌟 <b>YANGI MUROJAAT!</b> 🌟\n\n` +
                    `👤 <b>Mijoz:</b> ${cleanName}\n` +
                    `📞 <b>Telefon:</b> <code>${cleanPhone}</code>\n` +
                    `📦 <b>Tanlangan paket:</b> ${selectedPackage}${roomInfo}\n\n` +
                    `📅 <b>Vaqt:</b> ${now}\n` +
                    `🌐 <b>Manba:</b> Safo Marva Tour (Veb-sayt)\n\n` +
                    `📞 <b>Qo'ng'iroq uchun:</b> <a href="tel:${cleanPhone.replace(/\s/g, '')}">${cleanPhone}</a>`;

    // Send notifications in parallel to all admins
    const sendPromises = adminIds.map(async (adminId) => {
      try {
        const url = `https://api.telegram.org/bot${leadBotToken}/sendMessage`;
        await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            chat_id: adminId,
            text: message,
            parse_mode: 'HTML'
          })
        });
      } catch (err) {
        console.error(`❌ Failed to send Telegram notification to admin ${adminId}:`, err.message);
      }
    });

    await Promise.all(sendPromises);

    res.json({ success: true, dbSaved, message: 'Booking lead successfully processed.' });
  } catch (err) {
    console.error("❌ Lead processing failed:", err);
    res.status(500).json({ error: 'Internal server error occurred.' });
  }
});

// ==================== 1.5. Dynamic Package Media API ====================
const fs = require('fs');
const path = require('path');
const MEDIA_FILE = path.join(__dirname, 'packages_media.json');

function readMedia() {
  try {
    if (fs.existsSync(MEDIA_FILE)) {
      return JSON.parse(fs.readFileSync(MEDIA_FILE, 'utf8'));
    }
  } catch (e) {
    console.error("Error reading media file:", e);
  }
  return {};
}

function writeMedia(data) {
  try {
    fs.writeFileSync(MEDIA_FILE, JSON.stringify(data, null, 2), 'utf8');
  } catch (e) {
    console.error("Error writing media file:", e);
  }
}

// GET media mapping
app.get('/api/packages/media', (req, res) => {
  res.json(readMedia());
});

// POST to update media mapping
app.post('/api/packages/media/:key', (req, res) => {
  const key = req.params.key;
  const { image_url, video_url } = req.body;
  const media = readMedia();

  if (!media[key]) media[key] = {};
  if (image_url !== undefined) media[key].image_url = image_url;
  if (video_url !== undefined) media[key].video_url = video_url;

  writeMedia(media);
  res.json({ success: true, media: media[key] });
});

// ==================== 2. Telegram Bot Webhook (Optional) ====================
// Fallback if they choose to run the bot via webhook instead of polling.
app.post('/telegram/webhook', async (req, res) => {
  const update = req.body;
  if (!update.message) return res.sendStatus(200);

  const chatId = update.message.chat.id;
  const text = update.message.text?.trim();

  if (text === '/start') {
    await sendMessage(chatId, `👋 Safo Marva Tour botiga xush kelibsiz!\n\n` +
      `Quyidagi buyruqlar mavjud:\n` +
      `/price <key> <new_price> – paket narxini yangilash (admin only)\n` +
      `/list – barcha paketlarni ro‘yxati`);
    return res.sendStatus(200);
  }

  const adminIds = process.env.TELEGRAM_ADMINS ? process.env.TELEGRAM_ADMINS.split(',') : [];
  const isAdmin = adminIds.includes(String(chatId));

  if (text && text.startsWith('/price')) {
    if (!isAdmin) {
      await sendMessage(chatId, '⚠️ Siz administrator emassiz.');
      return res.sendStatus(200);
    }
    const parts = text.split(' ');
    if (parts.length !== 3) {
      await sendMessage(chatId, '❌ Noto‘g‘ri format. Misol: /price lux 1700');
      return res.sendStatus(200);
    }
    const [, key, newPrice] = parts;
    
    try {
      const result = await db.query('UPDATE packages SET price = $1 WHERE key_name = $2', [newPrice, key]);
      if (result.rowCount === 0) {
        await sendMessage(chatId, `⚠️ Paket key_name topilmadi: ${key}`);
      } else {
        await sendMessage(chatId, `✅ ${key} paketining narxi ${newPrice} ga yangilandi.`);
      }
    } catch (err) {
      await sendMessage(chatId, `❌ Xatolik: ${err.message}`);
    }
    return res.sendStatus(200);
  }

  if (text === '/list') {
    try {
      const { rows: packages } = await db.query('SELECT display_name, price FROM packages');
      const list = packages.map(p => `${p.display_name}: $${p.price}`).join('\n');
      await sendMessage(chatId, `📦 Paketlar:\n${list}`);
    } catch (err) {
      await sendMessage(chatId, `❌ Xatolik: ${err.message}`);
    }
    return res.sendStatus(200);
  }

  await sendMessage(chatId, '❓ Noma‘lum buyruq. /start buyrug‘ini ko‘ring.');
  res.sendStatus(200);
});

// Helper for Webhook message sending
async function sendMessage(chatId, text) {
  const token = process.env.BOT_TOKEN || '8843389342:AAFcyDSoofSqojZZWW2IugS4dL3mL-PWtq4';
  const url = `https://api.telegram.org/bot${token}/sendMessage`;
  try {
    await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ chat_id: chatId, text, parse_mode: 'HTML' })
    });
  } catch (err) {
    console.error("❌ sendMessage failed:", err.message);
  }
}

// ==================== 3. Packages REST API ====================
// GET /api/packages – public endpoint to fetch all packages for the landing page
app.get('/api/packages', async (req, res) => {
  try {
    const { rows } = await db.query('SELECT * FROM packages ORDER BY id ASC');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Example: GET /admin/packages – returns all packages
app.get('/admin/packages', async (req, res) => {
  try {
    const { rows } = await db.query('SELECT * FROM packages ORDER BY id ASC');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Example: POST /admin/package/:id – update a package
app.post('/admin/package/:id', async (req, res) => {
  const adminKey = req.headers['x-admin-key'];
  if (adminKey !== process.env.ADMIN_API_KEY) {
    return res.status(403).json({ error: 'Forbidden' });
  }
  const { id } = req.params;
  const { display_name, price, description, discount } = req.body;
  try {
    await db.query(
      'UPDATE packages SET display_name = COALESCE($1, display_name), price = COALESCE($2, price), description = COALESCE($3, description), discount = COALESCE($4, discount) WHERE id = $5',
      [display_name, price, description, discount, id]
    );
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ==================== 4. Boot unified Telegram Bot ====================
// Run the Telegram polling bot (bot.js) inside the same Node process.
try {
  console.log("🤖 Loading Admin Telegram Bot (Polling)...");
  require('./bot.js');
} catch (botErr) {
  console.error("⚠️ Failed to load standalone Telegram Bot:", botErr.message);
}

// Start Server
app.listen(PORT, () => {
  console.log(`🚀 Safo Marva backend listening on http://localhost:${PORT}`);
});
