package com.jiwhiz.demo.mydoctor.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.TokenExchangeOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultTokenExchangeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.TokenExchangeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.TokenExchangeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.MultiValueMap;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
public class ProjectConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors((cors) -> cors.configurationSource(request -> {
                var corsConfig = new CorsConfiguration();
                corsConfig.setAllowedOrigins(List.of("http://mydoctor:4200", "http://mydoctor:1234"));
                corsConfig.setAllowedMethods(
                    List.of("GET", "POST", "OPTIONS", "PUT", "DELETE")
                );
                corsConfig.setAllowedHeaders(List.of("*"));
                corsConfig.setAllowCredentials(true);
                return corsConfig;
            }))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/records").authenticated()
                .requestMatchers("/api/appointments").hasAuthority("ROLE_view_appointment")
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                .jwt( jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter(List.of("mydoctor-ui", "mydoctor-elm"))))
            )
            .oauth2Client(Customizer.withDefaults());
            ;
        // @formatter:on
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(oAuth2ResourceServerProperties.getJwt().getJwkSetUri()).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(oAuth2ResourceServerProperties.getJwt().getIssuerUri()));
        return jwtDecoder;
    }

    OAuth2AuthorizedClientProvider tokenExchange(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        Function<OAuth2AuthorizationContext, OAuth2Token> subjectResolver = (context) -> {
            if (context.getPrincipal() instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                Jwt jwt = jwtAuthenticationToken.getToken();
                OAuth2AccessToken token = new OAuth2AccessToken(TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
                log.debug("Get Access Token for current user from JwtAuthenticationToken: {}", token.getTokenValue());
                return token;
            }

            throw new RuntimeException("Cannot resolve subject token with context principal " + context.getPrincipal() );
        };

        Converter<TokenExchangeGrantRequest, RequestEntity<?>> requestEntityConverter = new TokenExchangeGrantRequestEntityConverter() {
            @Override
	        protected MultiValueMap<String, String> createParameters(TokenExchangeGrantRequest grantRequest) {
                MultiValueMap<String, String> parameters = super.createParameters(grantRequest);
                parameters.add("requested_issuer", "myhealth-keycloak-oidc");
                return parameters;
            }
        };
        DefaultTokenExchangeTokenResponseClient accessTokenResponseClient = new DefaultTokenExchangeTokenResponseClient();
        accessTokenResponseClient.setRequestEntityConverter(requestEntityConverter);

        TokenExchangeOAuth2AuthorizedClientProvider authorizedClientProvider =
                new TokenExchangeOAuth2AuthorizedClientProvider();
        authorizedClientProvider.setSubjectTokenResolver(subjectResolver);
        authorizedClientProvider.setAccessTokenResponseClient(accessTokenResponseClient);

        return authorizedClientProvider;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService clientService,
        OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken()
                        .clientCredentials()
                        .provider(tokenExchange(clientRegistrationRepository, authorizedClientRepository))
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("myhealth-client");

        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}

