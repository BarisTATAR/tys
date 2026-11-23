package com.tys.enums;

import lombok.Getter;

@Getter
public enum KbsErrorCode {
    UNKNOWN_ERROR(0, "Bilinmeyen hata"),
    SUCCESS(100, "İşlem başarılı"),
    RECORD_NOT_FOUND(101, "Kayıt bulunamadı"),
    NEW_RECORD_FAILED(102, "Yeni kayıt işlemi başarısız"),
    UPDATE_FAILED(103, "Güncelleme işlemi başarısız"),
    DELETE_FAILED(104, "Silme işlemi başarısız"),
    DATABASE_ERROR(105, "Veritabanı hatası"),
    GENERAL_ERROR(106, "Genel hata (tanımlı değil)"),
    INPUT_ERROR(107, "Girdi hatası - parametre tutarsızlığı"),
    AUTHORIZATION_ERROR(108, "Yetki hatası");

    private final int code;
    private final String description;

    KbsErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static KbsErrorCode fromCode(int code) {
        for (KbsErrorCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNKNOWN_ERROR;
    }
}