package com.tys.mapper;

import com.tys.dto.RulesFileDto;
import com.tys.model.RulesFile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RulesFileMapperImpl implements RulesFileMapper {

    @Override
    public RulesFileDto toDto(RulesFile entity) {
        if ( entity == null ) {
            return null;
        }

        RulesFileDto rulesFileDto = new RulesFileDto();

        rulesFileDto.setCompanyId( entity.getCompanyId() );
        rulesFileDto.setContentType( entity.getContentType() );
        rulesFileDto.setCreatedAt( entity.getCreatedAt() );
        rulesFileDto.setFileName( entity.getFileName() );
        rulesFileDto.setId( entity.getId() );
        rulesFileDto.setRulesText( entity.getRulesText() );

        return rulesFileDto;
    }

    @Override
    public RulesFile toEntity(RulesFileDto dto) {
        if ( dto == null ) {
            return null;
        }

        RulesFile rulesFile = new RulesFile();

        rulesFile.setCompanyId( dto.getCompanyId() );
        rulesFile.setContentType( dto.getContentType() );
        rulesFile.setCreatedAt( dto.getCreatedAt() );
        rulesFile.setFileName( dto.getFileName() );
        rulesFile.setId( dto.getId() );
        rulesFile.setRulesText( dto.getRulesText() );

        return rulesFile;
    }
}
