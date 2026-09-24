package com.colegio.bff.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CognitoRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Cognito inyecta los grupos de usuarios en el claim cognito:groups
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
        
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }

        return groups.stream()
                .map(group -> {
                    String roleName = group.toUpperCase();
                    return new SimpleGrantedAuthority(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
                })
                .collect(Collectors.toList());
    }
}