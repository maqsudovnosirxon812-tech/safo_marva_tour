const { Pool } = require('pg');
require('dotenv').config();

const connectionString = process.env.DATABASE_URL;

const pool = connectionString 
  ? new Pool({ 
      connectionString, 
      ssl: connectionString.includes('render') || connectionString.includes('neon') ? { rejectUnauthorized: false } : false 
    })
  : new Pool({
      host: process.env.PGHOST || 'localhost',
      user: process.env.PGUSER || 'postgres',
      password: process.env.PGPASSWORD || 'root1234',
      database: process.env.PGDATABASE || 'postgres', // default DB
      port: parseInt(process.env.PGPORT || '5432', 10)
    });

// Run migrations on start to auto-create and auto-seed tables
async function runMigrations() {
  try {
    // 1. Create packages table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS packages (
        id SERIAL PRIMARY KEY,
        key_name VARCHAR(50) UNIQUE NOT NULL,
        display_name VARCHAR(100) NOT NULL,
        price VARCHAR(50) NOT NULL,
        description TEXT,
        discount VARCHAR(50)
      );
    `);

    // 2. Create leads table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS leads (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        phone VARCHAR(50) NOT NULL,
        package VARCHAR(100) NOT NULL,
        room VARCHAR(100) DEFAULT 'Kiritilmagan',
        source VARCHAR(50) DEFAULT 'web-site',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);

    console.log("✅ PostgreSQL tables are ready!");

    // 3. Auto-seed packages if empty
    const { rows } = await pool.query("SELECT COUNT(*) FROM packages");
    if (parseInt(rows[0].count, 10) === 0) {
      console.log("🌱 Seeding default packages into PostgreSQL...");
      await pool.query(`
        INSERT INTO packages (key_name, display_name, price, description) VALUES
        ('standard', 'Standart Paket', '1150', 'Sifatli va hamyonbop tanlov. Ramada Zad Al Tayser mehmonxonasi, Madinadan Makkaga tezyurar poezd, borish-kelish aviachiptalari, Umra vizasi, yo''lboshchi va tibbiy sug''urta.'),
        ('comfort', 'Komfort Paket', '1300', 'AL EBAA mehmonxonasi, tezyurar poezd, 2 mahal mazali taomlar, Makka va Madina bo''ylab ekskursiyalar, Haram masjidlarida Juma namozi.'),
        ('lux', 'LUX Paket', '1550', 'ANJUM mehmonxonasi, Haramga yaqin masofada, Haramain tezyurar poezdi, yuqori sifatli transfer va maxsus guruh rahbarlari.'),
        ('lux_premium', 'LUX Premium', '1650', 'Bosphorus Waqf Al Safi (Madina) va Jumeirah Jabal Omar (Makka) 5 yulduzli mehmonxonalari, hashamatli xizmatlar va premium sayohat.'),
        ('special_14day', '14 Kunlik Paket', '1390', '7 kecha Madina (Mehrob Toiba) va 7 kecha Makka (Al Ebaa), 2 mahal taomlar, tezyurar poezd, ekskursiyalar, hadyalar (Zam-Zam 5L, Nimcha, Sumka, Abaya, Beyjik).')
      `);
      console.log("✅ Seeding packages completed successfully!");
    }
  } catch (err) {
    console.error("❌ PostgreSQL migration error:", err.message);
  }
}

runMigrations();

module.exports = {
  query: (text, params) => pool.query(text, params),
  pool
};
