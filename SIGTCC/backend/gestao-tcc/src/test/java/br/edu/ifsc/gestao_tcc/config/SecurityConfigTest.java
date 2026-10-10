package br.edu.ifsc.gestao_tcc.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação completa (com H2) para exercitar a cadeia de filtros de
 * segurança real. O jwt() simula um token já validado, então estes testes não
 * dependem de um Keycloak no ar.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 401 quando a requisição não tem token")
    void endpointProtegido_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/orientadores"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    @DisplayName("Deve retornar 401 quando o token é inválido")
    void endpointProtegido_ComTokenInvalido_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/orientadores")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer isto-nao-e-um-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve permitir o acesso quando há um token válido")
    void endpointProtegido_ComTokenValido_DeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/orientadores")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ALUNO"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve responder ao preflight de CORS do frontend sem exigir token")
    void preflight_DeOrigemPermitida_DeveRetornar200() throws Exception {
        mockMvc.perform(options("/api/v1/orientadores")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}