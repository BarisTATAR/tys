package com.tys.client;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KBS WSDL'i repoda tutulur; istemci servis tanımını ağa çıkmadan classpath'ten yükleyebilmelidir.
 */
class KbsWsdlTest {

    @Test
    void wsdlVeSemalarClasspathteBulunur() {
        ClassLoader loader = getClass().getClassLoader();
        assertThat(loader.getResource("wsdl/SrvShsYtkTml.wsdl")).isNotNull();
        for (int i = 0; i < 4; i++) {
            assertThat(loader.getResource("wsdl/SrvShsYtkTml-xsd" + i + ".xsd")).as("xsd" + i).isNotNull();
        }
    }

    @Test
    void servisTanimiAgaCikmadanYuklenir() {
        URL wsdl = SrvShsYtkTml.WSDL_LOCATION;
        assertThat(wsdl).isNotNull();
        assertThat(wsdl.getProtocol()).isNotEqualTo("https");
        SrvShsYtkTml service = new SrvShsYtkTml();
        assertThat(service.getBasicHttpsBindingKBSServis()).isNotNull();
    }
}
