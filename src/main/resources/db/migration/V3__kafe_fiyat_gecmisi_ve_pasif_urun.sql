-- V3 — Kafe hesabında fiyat geçmişi ve menüden kaldırılan ürünler
--
-- * reservation_cafe_item.unit_price: kalem eklendiği andaki birim fiyat. Menüde fiyat değişince geçmiş
--   ve kapanmış rezervasyon hesapları değişmez. Mevcut kalemler ürünün bugünkü fiyatıyla doldurulur.
-- * cafe.active: rezervasyon hesaplarında kullanılmış ürün silinmez, menüden kaldırılır (pasif).
-- * guest.city: misafirin ili.
--
-- Boş veritabanında (tablolar henüz entity tanımlarından kurulmamışken) hiçbir adım çalışmaz.

DO $$
BEGIN
    IF to_regclass('tys.reservation_cafe_item') IS NOT NULL THEN
        ALTER TABLE tys.reservation_cafe_item ADD COLUMN IF NOT EXISTS unit_price numeric(38, 2);
        UPDATE tys.reservation_cafe_item i
        SET unit_price = c.price
        FROM tys.cafe c
        WHERE c.id = i.cafe_id AND i.unit_price IS NULL;
    END IF;

    IF to_regclass('tys.cafe') IS NOT NULL THEN
        ALTER TABLE tys.cafe ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true;
    END IF;

    -- Misafir ili: Misafir Ekle ekranında seçiliyordu ama saklanmıyordu (SMS il filtresi bu alanı kullanır)
    IF to_regclass('tys.guest') IS NOT NULL THEN
        ALTER TABLE tys.guest ADD COLUMN IF NOT EXISTS city varchar(100);
    END IF;
END
$$;
