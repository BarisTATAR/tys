package com.tys.mapper;

import com.tys.client.Parametreler;
import com.tys.response.KbsParameterResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KbsMapper {

    @Mapping(target = "code", source = "kod")
    @Mapping(target = "value", source = "aciklama")
    KbsParameterResponse.Parameter mapKbsParameterToResponseParameter(Parametreler parametreler);
}
