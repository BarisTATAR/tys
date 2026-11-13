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

    private static final Pattern PERSON_NAME = Pattern.compile("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]{1,80}$");
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
            throw new IllegalArgumentException(label + " yalnız harf içerebilir.");
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
