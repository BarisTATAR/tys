package com.tys.service;

import com.tys.client.*;
import com.tys.mapper.KbsMapper;
import com.tys.request.*;
import com.tys.response.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.xml.ws.BindingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KbsService {
    private final KbsMapper kbsMapper;
    private volatile ISrvShsYtkTml port;

    public KbsService(KbsMapper kbsMapper) {
        this.kbsMapper = kbsMapper;
    }

    private static void ensureCredentials(KbsCredentials c) {
        if (c == null || c.tesisKodu() <= 0L || c.kullaniciTc() <= 0L || c.sifre() == null || c.sifre().isBlank()) {
            throw new IllegalStateException(KbsCredentialService.NOT_CONFIGURED);
        }
    }

    private ISrvShsYtkTml port() {
        ISrvShsYtkTml local = port;
        if (local == null) {
            synchronized (this) {
                local = port;
                if (local == null) {
                    try {
                        SrvShsYtkTml service = new SrvShsYtkTml();
                        local = service.getBasicHttpsBindingKBSServis();
                        BindingProvider provider = (BindingProvider) local;
                        provider.getRequestContext().put("com.sun.xml.ws.connect.timeout", 8000);
                        provider.getRequestContext().put("com.sun.xml.ws.request.timeout", 8000);
                        provider.getRequestContext().put("jakarta.xml.ws.client.connectionTimeout", 8000);
                        provider.getRequestContext().put("jakarta.xml.ws.client.receiveTimeout", 8000);
                        port = local;
                    } catch (Exception ex) {
                        log.error("KBS client could not be initialized: {}", ex.getMessage());
                        throw new IllegalStateException("KBS servisi başlatılamadı (SSL/WSDL). Diğer API'ler çalışmaya devam eder.", ex);
                    }
                }
            }
        }
        return local;
    }

    public List<KbsGuestResponse> kbsGuestList(KbsCredentials c) {
        ensureCredentials(c);
        try {
            List<KbsGuestResponse> kbsGuestResponseList = null;
            /*((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, 123L);
            ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123");*/

            MusteriKimlikNoListe musteriKimlikNoListe = port().musteriKimlikNoListele(c.tesisKodu(), c.kullaniciTc(), c.sifre());
            Sonuc musteriKimlikNoListeleSonuc = musteriKimlikNoListe.getSonuc().getValue();
            if (musteriKimlikNoListeleSonuc.isBasarili()) {
                kbsGuestResponseList = musteriKimlikNoListe.getMItems().getValue().getMusteriKimlikNoGirisSonuc()
                        .stream()
                        .map(musteri -> KbsGuestResponse.builder()
                                .name(musteri.getADI().getValue())
                                .surname(musteri.getSOYADI().getValue())
                                .build())
                        .toList();

            } else {
                if (Boolean.FALSE.equals(musteriKimlikNoListeleSonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoListele throws an error - code : {} , message : {}",
                            musteriKimlikNoListeleSonuc.getMesaj().getValue(),
                            musteriKimlikNoListeleSonuc.getHataKodu().get(0));
                }
            }
            return kbsGuestResponseList;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoListele : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public List<KbsForeignGuestResponse> kbsForeignGuestList(KbsCredentials c) {
        ensureCredentials(c);
        try {
            List<KbsForeignGuestResponse> kbsForeignGuestResponseList = null;
            /*((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, 123L);
            ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123");*/

            MusteriYabanciListe musteriYabanciListe = port().musteriYabanciListele(c.tesisKodu(), c.kullaniciTc(), c.sifre());
            Sonuc musteriYabanciListeleSonuc = musteriYabanciListe.getSonuc().getValue();
            if (musteriYabanciListeleSonuc.isBasarili()) {
                kbsForeignGuestResponseList = musteriYabanciListe.getMItems().getValue().getMusteriYabanciGirisSonuc()
                        .stream()
                        .map(musteri -> KbsForeignGuestResponse.builder()
                                .name(musteri.getADI().getValue())
                                .surname(musteri.getSOYADI().getValue())
                                .build())
                        .toList();

            } else {
                if (Boolean.FALSE.equals(musteriYabanciListeleSonuc.getHataKodu().isEmpty())) {
                    log.error("musteriYabanciListele throws an error - code : {} , message : {}",
                            musteriYabanciListeleSonuc.getMesaj().getValue(),
                            musteriYabanciListeleSonuc.getHataKodu().get(0));
                }
            }
            return kbsForeignGuestResponseList;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoListele : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    public KbsParameterResponse kbsParameterList(KbsCredentials c, KbsParameterRequest request) {
        ensureCredentials(c);
        try {
            KbsParameterResponse kbsParameterResponse = new KbsParameterResponse();
            Parametreler parametreler = port().parametreListele(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getParameter());
            Sonuc parametrelerSonuc = parametreler.getSonuc().getValue();

            if (parametrelerSonuc.isBasarili()) {
                List<KbsParameterResponse.Parameter> parameterList = parametreler.getItems().getValue().getParametreler()
                        .stream()
                        .map(kbsMapper::mapKbsParameterToResponseParameter)
                        .toList();
                kbsParameterResponse.setParameterList(parameterList);
            } else {
                if (Boolean.FALSE.equals(parametrelerSonuc.getHataKodu().isEmpty())) {
                    log.error("parametreListele throws an error - code : {} , message : {}",
                            parametrelerSonuc.getMesaj().getValue(),
                            parametrelerSonuc.getHataKodu().get(0));
                }
            }
            return kbsParameterResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, parametreListele : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsGuestCheckInResponse checkInGuest(KbsCredentials c, KbsGuestCheckInRequest request) {
        ensureCredentials(c);
        try {
            KbsGuestCheckInResponse kbsGuestCheckInResponse = new KbsGuestCheckInResponse();
            MusteriKimlikNoGirisSonuc musteriKimlikNoGirisSonuc = port().musteriKimlikNoGiris(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());
            Sonuc musteriKimlikNoGirisrSonuc = musteriKimlikNoGirisSonuc.getSonuc().getValue();

            if (musteriKimlikNoGirisrSonuc.isBasarili()) {
                kbsGuestCheckInResponse.setMusteri(musteriKimlikNoGirisSonuc);
                return kbsGuestCheckInResponse;
            }
            String detail = "KBS girişi başarısız.";
            if (musteriKimlikNoGirisrSonuc.getMesaj() != null
                    && musteriKimlikNoGirisrSonuc.getMesaj().getValue() != null
                    && !musteriKimlikNoGirisrSonuc.getMesaj().getValue().isBlank()) {
                detail = musteriKimlikNoGirisrSonuc.getMesaj().getValue();
            }
            log.error("musteriKimlikNoGiris unsuccessful: {}", detail);
            throw new RuntimeException("KBS girişi yapılamadı. " + detail);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoGiris : {}", ex.getMessage());
            throw new RuntimeException("KBS girişi yapılamadı.");
        }
    }

    public KbsGuestCheckOutResponse checkOutGuest(KbsCredentials c, KbsGuestCheckOutRequest request) {
        ensureCredentials(c);
        try {
            KbsGuestCheckOutResponse kbsGuestCheckOutResponse = new KbsGuestCheckOutResponse();
            Sonuc sonuc = port().musteriKimlikNoCikis(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsGuestCheckOutResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoCikis throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().get(0));
                }
            }
            return kbsGuestCheckOutResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoCikis : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsGuestUpdateCheckInResponse updateCheckInGuest(KbsCredentials c, KbsGuestUpdateCheckInRequest request) {
        ensureCredentials(c);
        try {
            KbsGuestUpdateCheckInResponse kbsGuestUpdateCheckInResponse = new KbsGuestUpdateCheckInResponse();
            Sonuc sonuc = port().musteriKimlikNoGuncelle(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsGuestUpdateCheckInResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoGuncelle throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().get(0));
                }
            }
            return kbsGuestUpdateCheckInResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoGuncelle : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsForeignGuestCheckInResponse checkInForeignGuest(KbsCredentials c, KbsForeignGuestCheckInRequest request) {
        ensureCredentials(c);
        try {
            KbsForeignGuestCheckInResponse kbsForeignGuestCheckInResponse = new KbsForeignGuestCheckInResponse();
            MusteriYabanciGirisSonuc musteriYabanciGiris = port().musteriYabanciGiris(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());
            Sonuc musteriKimlikNoGirisrSonuc = musteriYabanciGiris.getSonuc().getValue();

            if (musteriKimlikNoGirisrSonuc.isBasarili()) {
                kbsForeignGuestCheckInResponse.setMusteri(musteriYabanciGiris);
            } else {
                if (Boolean.FALSE.equals(musteriKimlikNoGirisrSonuc.getHataKodu().isEmpty())) {
                    log.error("musteriYabanciGiris throws an error - code : {} , message : {}",
                            musteriKimlikNoGirisrSonuc.getMesaj().getValue(),
                            musteriKimlikNoGirisrSonuc.getHataKodu().get(0));
                }
            }
            return kbsForeignGuestCheckInResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriYabanciGiris : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsForeignGuestCheckOutResponse checkOutForeignGuest(KbsCredentials c, KbsForeignGuestCheckOutRequest request) {
        ensureCredentials(c);
        try {
            KbsForeignGuestCheckOutResponse kbsForeignGuestCheckOutResponse = new KbsForeignGuestCheckOutResponse();
            Sonuc sonuc = port().musteriYabanciCikis(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsForeignGuestCheckOutResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriYabanciCikis throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().get(0));
                }
            }
            return kbsForeignGuestCheckOutResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriYabanciCikis : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsForeignGuestUpdateCheckInResponse updateForeignCheckInGuest(KbsCredentials c, KbsForeignGuestUpdateCheckInRequest request) {
        ensureCredentials(c);
        try {
            KbsForeignGuestUpdateCheckInResponse kbsForeignGuestUpdateCheckInResponse = new KbsForeignGuestUpdateCheckInResponse();
            Sonuc sonuc = port().musteriYabanciGuncelle(c.tesisKodu(), c.kullaniciTc(), c.sifre(), request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsForeignGuestUpdateCheckInResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriYabanciGuncelle throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().get(0));
                }
            }
            return kbsForeignGuestUpdateCheckInResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriYabanciGuncelle : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
