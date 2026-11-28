package com.tys.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Hata yanıtları istemciye Java iç mesajı ("For input string: ...") taşımamalı. */
class ApiExceptionHandlerTest {

    @RestController
    static class SampleController {
        @GetMapping("/sample/{id}")
        String byId(@PathVariable("id") Long id) {
            return "ok";
        }

        @GetMapping("/sample")
        String byYear(@RequestParam("year") int year) {
            return "ok";
        }

        @GetMapping("/sample/parse")
        String parse() {
            return String.valueOf(Long.parseLong("abc"));
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new SampleController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void sayiOlmayanKayitNumarasiAnlasilirMesajla400() throws Exception {
        mvc.perform(get("/sample/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Geçersiz kayıt numarası."));
    }

    @Test
    void sayiOlmayanSorguParametresiParametreAdiyla400() throws Exception {
        mvc.perform(get("/sample").param("year", "iki-bin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Geçersiz değer: year."));
    }

    @Test
    void servisteSayiyaCevrilemeyenDegerIcMesajSizdirmaz() throws Exception {
        mvc.perform(get("/sample/parse"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Geçersiz sayı biçimi."));
    }

    @Test
    void mesajMetinleri() {
        assertThat(ApiExceptionHandler.invalidValueMessage("reservationId")).isEqualTo("Geçersiz kayıt numarası.");
        assertThat(ApiExceptionHandler.invalidValueMessage(" ")).isEqualTo("Geçersiz sayı biçimi.");
    }
}
