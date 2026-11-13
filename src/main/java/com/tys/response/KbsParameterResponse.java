package com.tys.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbsParameterResponse {
    private List<Parameter> parameterList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Parameter {
        private String code;
        private String value;
    }
}
