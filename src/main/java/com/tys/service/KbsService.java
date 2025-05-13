package com.tys.service;

import com.tys.client.*;
import com.tys.mapper.KbsMapper;
import com.tys.request.KbsGuestCheckInRequest;
import com.tys.request.KbsGuestCheckOutRequest;
import com.tys.request.KbsGuestUpdateCheckInRequest;
import com.tys.request.KbsParameterRequest;
import com.tys.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KbsService {
    private final SrvShsYtkTml service = new SrvShsYtkTml();
    private final ISrvShsYtkTml port = service.getBasicHttpsBindingKBSServis();
    private final KbsMapper kbsMapper;

    public List<KbsGuestResponse> kbsGuestList() {
        try {
            List<KbsGuestResponse> kbsGuestResponseList = null;
            /*((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, 123L);
            ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123");*/

            MusteriKimlikNoListe musteriKimlikNoListe = port.musteriKimlikNoListele(11111070495L, 123L, "123");
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
                            musteriKimlikNoListeleSonuc.getHataKodu().getFirst());
                }
            }
            return kbsGuestResponseList;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoListele : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    public KbsParameterResponse kbsParameterList(KbsParameterRequest request) {
        try {
            KbsParameterResponse kbsParameterResponse = new KbsParameterResponse();
            Parametreler parametreler = port.parametreListele(11111070495L, 123L, "123", request.getParameter());
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
                            parametrelerSonuc.getHataKodu().getFirst());
                }
            }
            return kbsParameterResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, parametreListele : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsGuestCheckInResponse checkInGuest(KbsGuestCheckInRequest request) {
        try {
            KbsGuestCheckInResponse kbsGuestCheckInResponse = new KbsGuestCheckInResponse();
            MusteriKimlikNoGirisSonuc musteriKimlikNoGirisSonuc = port.musteriKimlikNoGiris(11111070495L, 123L, "123", request.getMusteri());
            Sonuc musteriKimlikNoGirisrSonuc = musteriKimlikNoGirisSonuc.getSonuc().getValue();

            if (musteriKimlikNoGirisrSonuc.isBasarili()) {
                kbsGuestCheckInResponse.setMusteri(musteriKimlikNoGirisSonuc);
            } else {
                if (Boolean.FALSE.equals(musteriKimlikNoGirisrSonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoGiris throws an error - code : {} , message : {}",
                            musteriKimlikNoGirisrSonuc.getMesaj().getValue(),
                            musteriKimlikNoGirisrSonuc.getHataKodu().getFirst());
                }
            }
            return kbsGuestCheckInResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoGiris : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsGuestCheckOutResponse checkOutGuest(KbsGuestCheckOutRequest request) {
        try {
            KbsGuestCheckOutResponse kbsGuestCheckOutResponse = new KbsGuestCheckOutResponse();
            Sonuc sonuc = port.musteriKimlikNoCikis(11111070495L, 123L, "123", request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsGuestCheckOutResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoCikis throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().getFirst());
                }
            }
            return kbsGuestCheckOutResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoCikis : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public KbsGuestUpdateCheckInResponse updateCheckInGuest(KbsGuestUpdateCheckInRequest request) {
        try {
            KbsGuestUpdateCheckInResponse kbsGuestUpdateCheckInResponse = new KbsGuestUpdateCheckInResponse();
            Sonuc sonuc = port.musteriKimlikNoGuncelle(11111070495L, 123L, "123", request.getMusteri());

            if (sonuc.isBasarili()) {
                kbsGuestUpdateCheckInResponse.setSonuc(sonuc);
            } else {
                if (Boolean.FALSE.equals(sonuc.getHataKodu().isEmpty())) {
                    log.error("musteriKimlikNoGuncelle throws an error - code : {} , message : {}",
                            sonuc.getMesaj().getValue(),
                            sonuc.getHataKodu().getFirst());
                }
            }
            return kbsGuestUpdateCheckInResponse;
        } catch (Exception ex) {
            log.error("Error occured while calling Kbs service, musteriKimlikNoGuncelle : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
