package com.tys.mapper;

import com.tys.dto.RulesFileDto;
import com.tys.model.RulesFile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RulesFileMapper {
    RulesFileDto toDto(RulesFile entity);
    RulesFile toEntity(RulesFileDto dto);
}
