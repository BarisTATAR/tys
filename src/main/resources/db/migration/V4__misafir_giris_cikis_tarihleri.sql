-- V4 — Konaklama durumundaki rezervasyonlarda misafir giriş/çıkış tarihleri
--
-- Uygulama misafir bazında "giriş bildirildi / çıkış bildirildi" durumunu guest.check_in_date ve
-- guest.check_out_date ile izler (sonradan eklenen misafirin KBS bildirimi, konaklayan misafirin
-- listeden çıkarılamaması, TCKN'sinin değiştirilememesi). Eski sürüm check-in'de bu tarihleri yazmıyordu.
-- Doldurulmazsa eski kayıtlardaki misafirler canlıya geçişten sonraki ilk güncellemede "yeni gelen"
-- sayılır ve KBS'ye ikinci kez bildirilirdi.
--
-- * Check-in yapılmış rezervasyonun misafirleri: giriş tarihi yoksa rezervasyonun giriş tarihi.
-- * Check-out yapılmış rezervasyonun misafirleri: giriş tarihi yoksa rezervasyonun giriş tarihi,
--   çıkış tarihi yoksa rezervasyonun çıkış tarihi.
-- Yalnız boş tarihler doldurulur; mevcut tarihler değişmez. Adı/soyadı girilmemiş yer tutucu
-- misafirlere dokunulmaz. Boş veritabanında hiçbir adım çalışmaz.

DO $$
BEGIN
    IF to_regclass('tys.guest') IS NOT NULL
       AND to_regclass('tys.reservation') IS NOT NULL
       AND to_regclass('tys.reservation_guest') IS NOT NULL THEN

        UPDATE tys.guest g
        SET check_in_date = r.check_in_date
        FROM tys.reservation_guest rg
        JOIN tys.reservation r ON r.id = rg.reservation_id
        WHERE rg.guest_id = g.id
          AND r.reservation_status IN ('CHECK_IN_DONE', 'CHECK_OUT_DONE')
          AND g.check_in_date IS NULL
          AND coalesce(trim(g.name), '') <> ''
          AND coalesce(trim(g.surname), '') <> '';

        UPDATE tys.guest g
        SET check_out_date = r.check_out_date
        FROM tys.reservation_guest rg
        JOIN tys.reservation r ON r.id = rg.reservation_id
        WHERE rg.guest_id = g.id
          AND r.reservation_status = 'CHECK_OUT_DONE'
          AND g.check_out_date IS NULL
          AND coalesce(trim(g.name), '') <> ''
          AND coalesce(trim(g.surname), '') <> '';
    END IF;
END
$$;
