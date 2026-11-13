package com.tys.domain.company;

import com.tys.exception.RoomCountBelowRegisteredException;
import com.tys.model.Company;
import com.tys.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyRoomCountRule {

    private final RoomRepository roomRepository;

    public void assertTotalRoomNumberNotBelowRegistered(Company company, Integer requestedTotalRooms) {
        if (requestedTotalRooms == null) {
            return;
        }
        long registeredRoomCount = roomRepository.countByCompany(company);
        if (requestedTotalRooms < registeredRoomCount) {
            throw new RoomCountBelowRegisteredException(registeredRoomCount);
        }
    }
}
