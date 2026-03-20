package com.tys.mapper;

import com.tys.dto.CafeDto;
import com.tys.model.Cafe;
import com.tys.request.CreateCafeRequest;
import com.tys.request.UpdateCafeRequest;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CafeMapperImpl implements CafeMapper {

    @Override
    public Cafe createCafeRequestToEntity(CreateCafeRequest createCafeRequest) {
        if ( createCafeRequest == null ) {
            return null;
        }

        Cafe cafe = new Cafe();

        cafe.setCompanyId( createCafeRequest.getCompanyId() );
        cafe.setName( createCafeRequest.getName() );
        cafe.setPrice( createCafeRequest.getPrice() );

        return cafe;
    }

    @Override
    public void updateExistingCafeWithCafeRequest(UpdateCafeRequest updateCafeRequest, Cafe existingCafe) {
        if ( updateCafeRequest == null ) {
            return;
        }

        existingCafe.setCompanyId( updateCafeRequest.getCompanyId() );
        existingCafe.setId( updateCafeRequest.getId() );
        existingCafe.setName( updateCafeRequest.getName() );
        existingCafe.setPrice( BigDecimal.valueOf( updateCafeRequest.getPrice() ) );
    }

    @Override
    public CafeDto toDto(Cafe cafe) {
        if ( cafe == null ) {
            return null;
        }

        CafeDto cafeDto = new CafeDto();

        cafeDto.setId( cafe.getId() );
        cafeDto.setName( cafe.getName() );
        cafeDto.setPrice( cafe.getPrice() );

        return cafeDto;
    }
}
