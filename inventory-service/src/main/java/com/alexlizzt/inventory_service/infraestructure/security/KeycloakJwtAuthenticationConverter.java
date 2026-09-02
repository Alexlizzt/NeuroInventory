package com.alexlizzt.inventory_service.infraestructure.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        List<GrantedAuthority> authorities = new ArrayList<>(
                scopesConverter.convert(jwt)
        );

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null) {
            Object rolesObject = realmAccess.get("roles");

            if (rolesObject instanceof List<?> roles) {
                roles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
