package com.tys.service;

/**
 * Bir işletmenin Jandarma KBS tesis bilgileri. Her KBS çağrısı ilgili işletmenin bilgileriyle yapılır.
 */
public record KbsCredentials(long tesisKodu, long kullaniciTc, String sifre) {

    @Override
    public String toString() {
        return "KbsCredentials[tesisKodu=" + tesisKodu + ", kullaniciTc=***, sifre=***]";
    }
}
