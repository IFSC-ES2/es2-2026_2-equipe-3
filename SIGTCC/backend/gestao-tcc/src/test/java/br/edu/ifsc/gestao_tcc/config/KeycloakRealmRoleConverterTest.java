package br.edu.ifsc.gestao_tcc.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    @DisplayName("Deve extrair ROLE_ALUNO e ROLE_PROFESSOR do claim realm_access.roles")
    void convert_ComRolesDoSistema_DeveRetornarAuthorities() {
        Jwt jwt = tokenComClaim("realm_access", Map.of("roles", List.of("ROLE_ALUNO", "ROLE_PROFESSOR")));

        List<String> authorities = nomes(converter.convert(jwt));

        assertEquals(List.of("ROLE_ALUNO", "ROLE_PROFESSOR"), authorities);
    }

    @Test
    @DisplayName("Deve descartar as roles internas do Keycloak")
    void convert_ComRolesInternasDoKeycloak_DeveDescartar() {
        Jwt jwt = tokenComClaim("realm_access",
                Map.of("roles", List.of("offline_access", "uma_authorization", "default-roles-sigtcc", "ROLE_ALUNO")));

        List<String> authorities = nomes(converter.convert(jwt));

        assertEquals(List.of("ROLE_ALUNO"), authorities);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o token não tem realm_access")
    void convert_SemRealmAccess_DeveRetornarListaVazia() {
        Jwt jwt = tokenComClaim("scope", "openid profile");

        assertTrue(converter.convert(jwt).isEmpty());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando realm_access não tem roles")
    void convert_RealmAccessSemRoles_DeveRetornarListaVazia() {
        Jwt jwt = tokenComClaim("realm_access", Map.of("outro", "valor"));

        assertTrue(converter.convert(jwt).isEmpty());
    }

    private Jwt tokenComClaim(String nome, Object valor) {
        return Jwt.withTokenValue("token-de-teste")
                .header("alg", "none")
                .subject("usuario-de-teste")
                .claim(nome, valor)
                .build();
    }

    private List<String> nomes(Collection<GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}