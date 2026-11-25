package com.tys.validation;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Ekran ve API için aynı giriş kuralları. Geçersiz değer 400 mesajına döner.
 */
public final class InputRules {

    public static final int MAX_BABY = 99;
    public static final int MAX_TEXT = 1000;
    public static final int MAX_SMS = 500;

    // Her dilden harf (yabancı misafirler: José, Müller, Seán), boşluk, tire, kesme işareti ve nokta; harfle başlar
    private static final Pattern PERSON_NAME = Pattern.compile("^\\p{L}[\\p{L}\\p{M}\\s'’.\\-]{0,79}$");
    private static final Pattern PLATE = Pattern.compile("^[a-zA-Z0-9ğüşıöçĞÜŞİÖÇ]{1,15}$");
    private static final Pattern CAFE_NAME = Pattern.compile("^[a-zA-Z0-9ğüşıöçĞÜŞİÖÇ\\s]{1,80}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@<>]{1,64}@[^\\s@<>]{1,120}\\.[^\\s@<>]{2,24}$");

    private InputRules() {
    }

    public static void stayCounts(Integer adult, Integer child, Integer baby, int roomCapacity) {
        int adults = adult == null ? 0 : adult;
        int children = child == null ? 0 : child;
        int babies = baby == null ? 0 : baby;
        if (adults < 1) {
            throw new IllegalArgumentException("Yetişkin sayısı en az 1 olmalıdır.");
        }
        if (children < 0) {
            throw new IllegalArgumentException("Çocuk sayısı 0'dan küçük olamaz.");
        }
        if (babies < 0 || babies > MAX_BABY) {
            throw new IllegalArgumentException("Bebek sayısı 0 ile 99 arasında olmalıdır.");
        }
        if (roomCapacity > 0 && adults + children > roomCapacity) {
            throw new IllegalArgumentException(
                    "Yetişkin ve çocuk sayısı (" + (adults + children)
                            + ") seçilen odaların toplam kapasitesinden (" + roomCapacity + ") fazla olamaz.");
        }
    }

    public static void guestFields(
            String name,
            String surname,
            String identityNumber,
            String phoneNumber,
            String email,
            String plateNumber,
            String previousIdentity,
            String previousPhone) {
        personName(name, "Misafir adı");
        personName(surname, "Misafir soyadı");
        if (hasText(identityNumber) && !sameText(identityNumber, previousIdentity)) {
            requireTckn(identityNumber.trim());
        }
        if (hasText(phoneNumber) && !sameDigits(phoneNumber, previousPhone)) {
            requireMobile(phoneNumber);
        }
        email(email);
        plate(plateNumber);
    }

    public static void personName(String value, String label) {
        if (!hasText(value)) {
            return;
        }
        if (!PERSON_NAME.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(label + " yalnız harf, boşluk, tire ve kesme işareti içerebilir.");
        }
    }

    public static void requireTckn(String value) {
        if (!isValidTckn(value)) {
            throw new IllegalArgumentException("Geçersiz TCKN.");
        }
    }

    public static void requireMobile(String value) {
        String digits = digits(value);
        if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 12 && digits.startsWith("90")) {
            digits = digits.substring(2);
        }
        if (digits.length() != 10 || digits.charAt(0) != '5') {
            throw new IllegalArgumentException("İletişim numarası 5 ile başlayan 10 hane olmalıdır.");
        }
    }

    public static void email(String value) {
        if (!hasText(value)) {
            return;
        }
        if (!EMAIL.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Geçerli bir e-posta giriniz.");
        }
    }

    public static void plate(String value) {
        if (!hasText(value)) {
            return;
        }
        if (!PLATE.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Plaka yalnız harf ve rakam içerebilir.");
        }
    }

    public static void plainText(String value, String label, int maxLength, boolean required) {
        if (!hasText(value)) {
            if (required) {
                throw new IllegalArgumentException(label + " boş olamaz.");
            }
            return;
        }
        String trimmed = value.trim();
        if (trimmed.indexOf('<') >= 0 || trimmed.indexOf('>') >= 0) {
            throw new IllegalArgumentException(label + " işaret karakteri içeremez.");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(label + " en fazla " + maxLength + " karakter olabilir.");
        }
    }

    public static void payment(BigDecimal amount, BigDecimal advance) {
        if (amount != null && amount.signum() < 0) {
            throw new IllegalArgumentException("Tutar 0'dan küçük olamaz.");
        }
        if (advance != null && advance.signum() < 0) {
            throw new IllegalArgumentException("Kapora 0'dan küçük olamaz.");
        }
        if (amount != null && advance != null && advance.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Toplam tutar kapora tutarından küçük olamaz.");
        }
    }

    public static void cafeItem(String name, double price) {
        cafeItem(name, BigDecimal.valueOf(price));
    }

    public static void cafeItem(String name, BigDecimal price) {
        if (!hasText(name) || !CAFE_NAME.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("Ürün adı yalnız harf, rakam ve boşluk içerebilir.");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Fiyat 0'dan küçük olamaz.");
        }
    }

    public static boolean isValidTckn(String value) {
        if (value == null) {
            return false;
        }
        String tckn = digits(value);
        if (tckn.length() != 11 || tckn.charAt(0) == '0') {
            return false;
        }
        boolean same = true;
        for (int i = 1; i < tckn.length(); i++) {
            if (tckn.charAt(i) != tckn.charAt(0)) {
                same = false;
                break;
            }
        }
        if (same) {
            return false;
        }
        int oddSum = 0;
        int evenSum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = tckn.charAt(i) - '0';
            if (i % 2 == 0) {
                oddSum += digit;
            } else {
                evenSum += digit;
            }
        }
        int digit10 = Math.floorMod(oddSum * 7 - evenSum, 10);
        int sum10 = 0;
        for (int i = 0; i < 10; i++) {
            sum10 += tckn.charAt(i) - '0';
        }
        int digit11 = Math.floorMod(sum10, 10);
        return digit10 == (tckn.charAt(9) - '0') && digit11 == (tckn.charAt(10) - '0');
    }

    // ------------------------------------------------------------------ hesap / işletme / oda / puan kuralları

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 72; // BCrypt yalnız ilk 72 baytı kullanır
    public static final int MAX_ROOMS = 5000;
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    private static final Pattern FACILITY = Pattern.compile("^[a-zA-Z0-9._/-]{1,50}$");
    private static final Pattern DIGITS = Pattern.compile("^\\d{1,15}$");

    /** Parola politikası: 8–72 karakter, en az bir harf ve bir rakam, boşluk yok. */
    public static void password(String value, String label) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(label + " gereklidir.");
        }
        if (value.length() < PASSWORD_MIN || value.length() > PASSWORD_MAX) {
            throw new IllegalArgumentException(label + " " + PASSWORD_MIN + "–" + PASSWORD_MAX + " karakter olmalıdır.");
        }
        boolean letter = false;
        boolean digit = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isWhitespace(ch)) {
                throw new IllegalArgumentException(label + " boşluk içeremez.");
            }
            letter |= Character.isLetter(ch);
            digit |= Character.isDigit(ch);
        }
        if (!letter || !digit) {
            throw new IllegalArgumentException(label + " en az bir harf ve bir rakam içermelidir.");
        }
    }

    public static void username(String value) {
        if (!hasText(value) || !USERNAME.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Kullanıcı adı 3–50 karakter olmalı; harf, rakam, nokta, tire ve alt çizgi içerebilir.");
        }
    }

    public static void requiredText(String value, String label, int maxLength) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(label + " gereklidir.");
        }
        plainText(value, label, maxLength, true);
    }

    public static void facilityNumber(String value) {
        if (!hasText(value) || !FACILITY.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Tesis (TSS) numarası gereklidir; harf, rakam ve - . / _ içerebilir.");
        }
    }

    public static void totalRoomNumber(Integer value) {
        if (value == null || value < 1 || value > MAX_ROOMS) {
            throw new IllegalArgumentException("Toplam oda sayısı 1 ile " + MAX_ROOMS + " arasında olmalıdır.");
        }
    }

    public static void optionalMobile(String value) {
        if (hasText(value)) {
            requireMobile(value);
        }
    }

    public static void startYear(Integer value) {
        if (value != null && (value < 1800 || value > 2100)) {
            throw new IllegalArgumentException("Kuruluş yılı geçersiz.");
        }
    }

    /** Oda numarası 1–99999, kapasite 1–50, kat -5–200. */
    public static void room(Integer number, Integer capacity, Integer floor) {
        if (number == null || number < 1 || number > 99_999) {
            throw new IllegalArgumentException("Oda numarası 1 ile 99999 arasında olmalıdır.");
        }
        if (capacity == null || capacity < 1 || capacity > 50) {
            throw new IllegalArgumentException("Oda kapasitesi 1 ile 50 arasında olmalıdır.");
        }
        if (floor != null && (floor < -5 || floor > 200)) {
            throw new IllegalArgumentException("Kat -5 ile 200 arasında olmalıdır.");
        }
    }

    /** Ek (kapanış) ödeme: tutar zorunlu ve 0'dan büyük, ödeme tipi zorunlu. */
    public static void paidAmount(BigDecimal amount, Object paymentType) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Ödeme tutarı 0'dan büyük olmalıdır.");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Ödeme tutarı en fazla 2 ondalık basamak içerebilir.");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Ödeme şekli seçilmelidir.");
        }
    }

    /** Değerlendirme puanları 1–5. */
    public static void rating(BigDecimal average, Integer... scores) {
        if (average == null || average.compareTo(BigDecimal.ONE) < 0 || average.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Ortalama puan 1 ile 5 arasında olmalıdır.");
        }
        for (Integer score : scores) {
            if (score != null && (score < 1 || score > 5)) {
                throw new IllegalArgumentException("Puanlar 1 ile 5 arasında olmalıdır.");
            }
        }
    }

    /** KBS tesis kodu ve kullanıcı TCKN rakamlardan oluşur. */
    public static long kbsNumber(String value, String label) {
        if (!hasText(value) || !DIGITS.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(label + " yalnız rakamlardan oluşmalıdır.");
        }
        long parsed = Long.parseLong(value.trim());
        if (parsed <= 0) {
            throw new IllegalArgumentException(label + " geçersiz.");
        }
        return parsed;
    }

    private static boolean sameText(String incoming, String previous) {
        return previous != null && incoming.trim().equals(previous.trim());
    }

    private static boolean sameDigits(String incoming, String previous) {
        return previous != null && digits(incoming).equals(digits(previous));
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String digits(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '0' && ch <= '9') {
                out.append(ch);
            }
        }
        return out.toString();
    }
}
