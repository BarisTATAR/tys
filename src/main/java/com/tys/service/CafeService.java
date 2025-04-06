package com.tys.service;

import com.tys.mapper.CafeMapper;
import com.tys.model.Cafe;
import com.tys.repository.CafeRepository;
import com.tys.request.CreateCafeRequest;
import com.tys.request.DeleteCafeRequest;
import com.tys.request.UpdateCafeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;
    private final CafeMapper cafeMapper;

    public void createCafe(CreateCafeRequest request) {
        Cafe cafe = cafeMapper.createCafeRequestToEntity(request);
        cafeRepository.save(cafe);
    }

    public void deleteCafe(DeleteCafeRequest request) {
        if (!cafeRepository.existsById(request.getId())) {
            throw new RuntimeException("Cafe not found with Id: " + request.getId());
        }
        cafeRepository.deleteById(request.getId());
    }

    public void updateCafe(UpdateCafeRequest request) {
        Cafe existingCafe = cafeRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Cafe not found with Id: " + request.getId()));
        cafeMapper.updateExistingCafeWithCafeRequest(request, existingCafe);
        cafeRepository.save(existingCafe);
    }

    public Cafe getCafeById(Long id) {
        return cafeRepository.findById(id).orElseThrow(() -> new RuntimeException("Cafe not found with Id: " + id));
    }

}
