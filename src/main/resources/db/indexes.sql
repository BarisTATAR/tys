-- Sık kullanılan sorgular için indeksler (yabancı anahtarlar PostgreSQL'de otomatik indekslenmez).
-- Her açılışta SchemaBootstrap tarafından çalıştırılır, tekrar uygulanabilir.
CREATE INDEX IF NOT EXISTS idx_reservation_company ON tys.reservation (company_id);
CREATE INDEX IF NOT EXISTS idx_reservation_dates ON tys.reservation (company_id, check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_reservation_room_room ON tys.reservation_room (room_id);
CREATE INDEX IF NOT EXISTS idx_reservation_room_reservation ON tys.reservation_room (reservation_id);
CREATE INDEX IF NOT EXISTS idx_reservation_guest_guest ON tys.reservation_guest (guest_id);
CREATE INDEX IF NOT EXISTS idx_reservation_guest_reservation ON tys.reservation_guest (reservation_id);
CREATE INDEX IF NOT EXISTS idx_room_company ON tys.room (company_id);
CREATE INDEX IF NOT EXISTS idx_guest_company ON tys.guest (company_id);
CREATE INDEX IF NOT EXISTS idx_guest_identity ON tys.guest (identity_number);
CREATE INDEX IF NOT EXISTS idx_payment_reservation ON tys.payment (reservation_id);
CREATE INDEX IF NOT EXISTS idx_payment_date ON tys.payment (payment_date);
CREATE INDEX IF NOT EXISTS idx_cafe_company ON tys.cafe (company_id);
CREATE INDEX IF NOT EXISTS idx_reservation_cafe_item_reservation ON tys.reservation_cafe_item (reservation_id);
CREATE INDEX IF NOT EXISTS idx_rating_company ON tys.rating (company_id);
