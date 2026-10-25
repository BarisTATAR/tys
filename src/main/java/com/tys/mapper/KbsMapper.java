package com.tys.mapper;

import com.tys.client.Parametreler;
import com.tys.response.KbsParameterResponse;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KbsMapper {

    /*@Mapping(target = "code", source = "parametreler.getKOD()")
    @Mapping(target = "value", source = "parametreler.getACIKLAMA()")*/
    KbsParameterResponse.Parameter mapKbsParameterToResponseParameter(Parametreler parametreler);
}
