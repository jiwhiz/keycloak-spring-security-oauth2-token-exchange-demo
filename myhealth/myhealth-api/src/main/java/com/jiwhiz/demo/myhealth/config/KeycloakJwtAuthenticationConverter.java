package com.jiwhiz.demo.myhealth.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final List<String> clientIds;

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        log.debug("Logged in user oauth 2 jwt token claims: {}.", source.getClaims());
        return new JwtAuthenticationToken(
            source,
            Stream.concat(
                new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                extractResourceRoles(source).stream()).collect(Collectors.toSet()));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        var resourceAccess = new HashMap<>(jwt.getClaim("resource_access"));
        var resourceRoles = new ArrayList<String>();

        clientIds.stream().forEach( id -> {
            if (resourceAccess.containsKey(id)) {
                @SuppressWarnings("unchecked")
                var resource = (Map<String, List<String>>) resourceAccess.get(id);
                resource.get("roles").forEach(role -> resourceRoles.add(role));
            }
        });

        return resourceRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
            .collect(Collectors.toSet());
    }

}
