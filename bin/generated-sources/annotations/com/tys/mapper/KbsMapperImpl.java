package com.tys.mapper;

import com.tys.client.Parametreler;
import com.tys.response.KbsParameterResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class KbsMapperImpl implements KbsMapper {

    @Override
    public KbsParameterResponse.Parameter mapKbsParameterToResponseParameter(Parametreler parametreler) {
        if ( parametreler == null ) {
            return null;
        }

        KbsParameterResponse.Parameter.ParameterBuilder parameter = KbsParameterResponse.Parameter.builder();

        return parameter.build();
    }
}
