package com.tys.request;

import com.tys.client.SnfEnumServisUstKodParametreler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbsParameterRequest {
    private SnfEnumServisUstKodParametreler parameter;
}
