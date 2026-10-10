package br.edu.ifsc.gestao_tcc.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_ROLES = "roles";
    private static final String PREFIXO_ROLE = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        Object roles = realmAccess == null ? null : realmAccess.get(CLAIM_ROLES);

        if (!(roles instanceof Collection<?> listaDeRoles)) {
            return List.of();
        }

        return listaDeRoles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> role.startsWith(PREFIXO_ROLE))
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }
}