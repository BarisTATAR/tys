package com.tys.service;

import com.tys.dto.CafeDto;
import com.tys.mapper.CafeMapper;
import com.tys.model.Cafe;
import com.tys.repository.CafeRepository;
import com.tys.repository.ReservationCafeItemRepository;
import com.tys.request.CreateCafeRequest;
import com.tys.request.DeleteCafeRequest;
import com.tys.request.UpdateCafeRequest;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeService {

    private static final String NOT_FOUND = "Cafe ürünü bulunamadı.";

    private final CafeRepository cafeRepository;
    private final ReservationCafeItemRepository reservationCafeItemRepository;
    private final CafeMapper cafeMapper;

    public void createCafe(CreateCafeRequest request) {
        InputRules.cafeItem(request.getName(), request.getPrice());
        Cafe cafe = cafeMapper.createCafeRequestToEntity(request);
        cafeRepository.save(cafe);
    }

    @Transactional
    public void deleteCafe(DeleteCafeRequest request) {
        if (request == null || request.getId() == null || !cafeRepository.existsById(request.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND);
        }
        if (reservationCafeItemRepository.existsByCafe_Id(request.getId())) {
            // Rezervasyon hesaplarında kullanılmış ürün silinmez: menüden kaldırılır, hesaplar ve tutarlar korunur
            Cafe cafe = getCafeById(request.getId());
            cafe.setActive(Boolean.FALSE);
            cafeRepository.save(cafe);
            return;
        }
        cafeRepository.deleteById(request.getId());
    }

    public void updateCafe(UpdateCafeRequest request) {
        InputRules.cafeItem(request.getName(), request.getPrice());
        Cafe existingCafe = getCafeById(request.getId());
        if (Boolean.FALSE.equals(existingCafe.getActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND);
        }
        cafeMapper.updateExistingCafeWithCafeRequest(request, existingCafe);
        cafeRepository.save(existingCafe);
    }

    public Cafe getCafeById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND);
        }
        return cafeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND));
    }

    public List<CafeDto> getAllCafeItems(Long companyId) {
        return cafeRepository.findAllByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(cafeMapper::toDto)
                .toList();
    }
}
