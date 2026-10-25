package com.tys.mapper;

import com.tys.client.Parametreler;
import com.tys.response.KbsParameterResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-03T19:31:25+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
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
