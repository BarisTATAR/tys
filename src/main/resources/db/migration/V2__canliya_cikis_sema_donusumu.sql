-- V2 — Canlıya çıkış şema dönüşümü (eski Hibernate şeması -> güncel entity tanımları)
--
-- * Enum kolonları sıra numarası (smallint) yerine ad (varchar) olarak saklanır; enum sırası değişse de veri bozulmaz.
--   Dönüşüm, Java enum bildirim sırasından üretilmiş ordinal -> ad eşlemesiyle yapılır (dizi indeksi = ordinal + 1).
-- * payment."advance payment" kolonu advance_payment olarak yeniden adlandırılır.
-- * Eksik olabilecek tablolar/kolonlar tamamlanır (rating, rating.company_id, revoked_token, login_lock, kbs_notice).
-- * İşletmesi boş eski misafir kayıtlarına rezervasyonları üzerinden işletme atanır.
--
-- Boş veritabanında (tablolar henüz entity tanımlarından kurulmamışken) hiçbir adım çalışmaz.

CREATE OR REPLACE FUNCTION pg_temp.tys_enum_to_text(p_table text, p_column text, p_values text[]) RETURNS void
LANGUAGE plpgsql AS $fn$
DECLARE
    v_type text;
    v_con record;
BEGIN
    SELECT data_type INTO v_type
    FROM information_schema.columns
    WHERE table_schema = 'tys' AND table_name = p_table AND column_name = p_column;
    IF v_type IS NULL OR v_type NOT IN ('smallint', 'integer', 'bigint') THEN
        RETURN; -- kolon yok ya da zaten metin
    END IF;
    FOR v_con IN
        SELECT c.conname
        FROM pg_constraint c
        WHERE c.conrelid = format('tys.%I', p_table)::regclass
          AND c.contype = 'c'
          AND pg_get_constraintdef(c.oid) LIKE '%' || p_column || '%'
    LOOP
        EXECUTE format('ALTER TABLE tys.%I DROP CONSTRAINT %I', p_table, v_con.conname);
    END LOOP;
    EXECUTE format(
        'ALTER TABLE tys.%I ALTER COLUMN %I TYPE varchar(255) USING (CASE WHEN %I IS NULL THEN NULL ELSE (%L::text[])[%I + 1] END)',
        p_table, p_column, p_column, p_values, p_column);
END
$fn$;

DO $$
BEGIN
    IF to_regclass('tys.reservation') IS NULL THEN
        RAISE NOTICE 'V2: bos sema, donusum gerekmiyor (tablolar entity tanimlarindan kurulacak).';
        RETURN;
    END IF;

    -- 1) Enum kolonları: ordinal -> ad
    IF to_regclass('tys.company') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('company', 'category', '{OTEL,TATIL_KOYU,PANSIYON,BUNGALOV,APART_OTEL,MOTEL,LUX_OTEL,BUTIK_OTEL}'::text[]);
    END IF;
    IF to_regclass('tys.guest') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('guest', 'country_code', '{TANIMSIZ,TURKIYE,AFGHANISTAN,ALBANIA,ALGERIA,AMERICAN_SAMOA,ANDORRA,ANGOLA,ANGUILLA,ANTARCTICA,ANTIGUA_AND_BARBUDA,ARGENTINA,ARMENIA,ARUBA,AUSTRALIA,AUSTRIA,AZERBAIJAN,BAHAMAS,BAHRAIN,BANGLADESH,BARBADOS,BELARUS,BELGIUM,BELIZE,BENIN,BERMUDA,BHUTAN,BOLIVIA,BOSNIA_AND_HERZEGOVINA,BOTSWANA,BOUVET_ISLAND,BRAZIL,BRITISH_INDIAN_OCEAN_TERRITORY,BRUNEI_DARUSSALAM,BULGARIA,BURKINA_FASO,BURUNDI,CAMBODIA,CAMEROON,CANADA,CAPE_VERDE,CAYMAN_ISLANDS,CENTRAL_AFRICAN_REPUBLIC,CHAD,CHILE,CHINA,CHRISTMAS_ISLAND,COCOS_KEELING_ISLANDS,COLOMBIA,COMOROS,CONGO_REPUBLIC_OF,CONGO_THE_DEMOCRATIC_REPUBLIC_OF_THE_FORMERLY_ZAIRE,COOK_ISLANDS,COSTA_RICA,C,CROATIA_HRVATSKA,CUBA,CYPRUS,CZECH_REPUBLIC,DENMARK,DJIBOUTI,DOMINICA,DOMINICAN_REPUBLIC,ECUADOR,EGYPT,EL_SALVADOR,EQUATORIAL_GUINEA,ERITREA,ESTONIA,ETHIOPIA,FAEROE_ISLANDS,FALKLAND_ISLANDS_MALVINAS,FIJI,FINLAND,FRANCE,FRENCH_GUIANA,FRENCH_POLYNESIA,FRENCH_SOUTHERN_TERRITORIES,GABON,GAMBIA,GEORGIA,GERMANY,GHANA,GIBRALTAR,GREECE,GREENLAND,GRENADA,GUADELOUPE,GUAM,GUATEMALA,GUERNSEY,GUINEA,GUINEA_BISSAU,GUYANA,HAITI,HEARD_ISLAND_AND_MCDONALD_ISLANDS,HONDURAS,HONG_KONG,HUNGARY,ICELAND,INDIA,INDONESIA,IRAN,IRAQ,IRELAND,ISRAEL,ITALY,JAMAICA,JAPAN,JERSEY,JORDAN,KAZAKHSTAN,KENYA,KIRIBATI,NORTH_KOREA,SOUTH_KOREA,KUWAIT,KUZEY_KIBRIS_TURK_CUMHURIYETI,KYRGYZSTAN,LAO_PEOPLES_DEMOCRATIC_REPUBLIC,LATVIA,LEBANON,LESOTHO,LIBERIA,LIBYA,LIECHTENSTEIN,LITHUANIA,LUXEMBOURG,MACAO,MACEDONIA,MADAGASCAR,MALAWI,MALAYSIA,MALDIVES,MALI,MALTA,MARSHALL_ISLANDS,MARTINIQUE,MAURITANIA,MAURITIUS,MAYOTTE,MEXICO,MICRONESIA,MOLDOVA,MONACO,MONGOLIA,MONTSERRAT,MOROCCO,MOZAMBIQUE,MYANMAR,NAMIBIA,NAURU,NEPAL,NETHERLANDS,NETHERLANDS_ANTILLES,NEW_CALEDONIA,NEW_ZEALAND,NICARAGUA,NIGER,NIGERIA,NIUE,NORFOLK_ISLAND,NORTHERN_MARIANA_ISLANDS,NORWAY,OMAN,PAKISTAN,PALAU,PALESTINIAN_TERRITORIES,PANAMA,PAPUA_NEW_GUINEA,PARAGUAY,PERU,PHILIPPINES,PITCAIRN,POLAND,PORTUGAL,PUERTO_RICO,QATAR,REPUBLIC_OF_KOSOVO,R,ROMANIA,RUSSIAN_FEDERATION,RWANDA,SAINT_HELENA,SAINT_KITTS_AND_NEVIS,SAINT_LUCIA,SAINT_PIERRE_AND_MIQUELON,SAINT_VINCENT_AND_THE_GRENADINES,SAMOA,SAN_MARINO,SAO_TOME_AND_PRINCIPE,SAUDI_ARABIA,SENEGAL,SERBIA_AND_MONTENEGRO,SEYCHELLES,SIERRA_LEONE,SINGAPORE,SLOVAKIA,SLOVENIA,SOLOMON_ISLANDS,SOMALIA,SOUTH_AFRICA,SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS,SPAIN,SRI_LANKA,SUDAN,SURINAME,SVALBARD_AND_JAN_MAYEN,SWAZILAND,SWEDEN,SWITZERLAND,SYRIAN_ARAB_REPUBLIC,TAIWAN,TAJIKISTAN,TANZANIA,THAILAND,TIMOR_LESTE,TOGO,TOKELAU,TONGA,TRINIDAD_AND_TOBAGO,TUNISIA,TURKMENISTAN,TURKS_AND_CAICOS_ISLANDS,TUVALU,UGANDA,UKRAINE,UNITED_ARAB_EMIRATES,UNITED_KINGDOM,UNITED_STATES,UNITED_STATES_MINOR_OUTLYING_ISLANDS,URUGUAY,UZBEKISTAN,VANUATU,VATICAN_CITY,VENEZUELA,VIET_NAM,VIRGIN_ISLANDS_BRITISH,VIRGIN_ISLANDS_US,WALLIS_AND_FUTUNA,WESTERN_SAHARA,YEMEN,ZAMBIA,ZIMBABWE}'::text[]);
    END IF;
    IF to_regclass('tys.guest') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('guest', 'guest_usage_type', '{TANIMSIZ,KONAKLAMA,GUNICI,AFETZEDE}'::text[]);
    END IF;
    IF to_regclass('tys.payment') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('payment', 'advance_payment_type', '{CASH,CREDIT_CARD,DEBIT_CARD,ALL}'::text[]);
    END IF;
    IF to_regclass('tys.payment') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('payment', 'close_payment_type', '{CASH,CREDIT_CARD,DEBIT_CARD,ALL}'::text[]);
    END IF;
    IF to_regclass('tys.reservation') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('reservation', 'reservation_status', '{RESERVATION_MADE,GUEST_INFORMATION_ENTERED,CHECK_IN_DONE,CHECK_OUT_DONE,CANCELLED}'::text[]);
    END IF;
    IF to_regclass('tys.reservation') IS NOT NULL THEN
        PERFORM pg_temp.tys_enum_to_text('reservation', 'reservation_type', '{ROOM_WITH_BREAKFAST,ROOM_ONLY}'::text[]);
    END IF;

    -- 2) Boşluklu kolon adı
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'tys' AND table_name = 'payment' AND column_name = 'advance payment')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'tys' AND table_name = 'payment' AND column_name = 'advance_payment') THEN
        ALTER TABLE tys.payment RENAME COLUMN "advance payment" TO advance_payment;
    END IF;

    -- 3) Eksik olabilecek tablolar / kolonlar
    CREATE TABLE IF NOT EXISTS tys.rating (
        id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        company_id bigint,
        rating_average numeric(38, 2),
        cleanliness integer,
        service integer,
        comfort integer,
        location integer,
        food integer
    );
    ALTER TABLE tys.rating ADD COLUMN IF NOT EXISTS company_id bigint;

    CREATE TABLE IF NOT EXISTS tys.revoked_token (
        token_hash varchar(64) PRIMARY KEY,
        expires_at timestamp NOT NULL
    );
    CREATE TABLE IF NOT EXISTS tys.login_lock (
        username varchar(120) PRIMARY KEY,
        failures integer NOT NULL,
        locked_until timestamp NULL
    );
    CREATE TABLE IF NOT EXISTS tys.kbs_notice (
        id bigserial PRIMARY KEY,
        reservation_id bigint NOT NULL,
        message varchar(2000) NOT NULL,
        created_at timestamp NOT NULL
    );

    -- 4) İşletmesi boş eski misafirler: ilk rezervasyonunun işletmesi atanır
    IF to_regclass('tys.reservation_guest') IS NOT NULL THEN
        UPDATE tys.guest g
        SET company_id = (
            SELECT r.company_id
            FROM tys.reservation_guest rg
            JOIN tys.reservation r ON r.id = rg.reservation_id
            WHERE rg.guest_id = g.id AND r.company_id IS NOT NULL
            ORDER BY r.id
            LIMIT 1)
        WHERE g.company_id IS NULL;
    END IF;
END
$$;
