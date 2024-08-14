package com.chat.chatbackend;

// import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.server.WebSocketGraphQlInterceptor;
import org.springframework.graphql.server.WebSocketSessionInfo;
import org.springframework.graphql.server.support.BearerTokenAuthenticationExtractor;
import org.springframework.graphql.server.webflux.AuthenticationWebSocketInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Mono;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

	String jwkSetUri = "http://localhost:8090/realms/chat-app/protocol/openid-connect/certs";

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.applyPermitDefaultValues();
		corsConfig.addAllowedMethod(HttpMethod.PUT);
		corsConfig.addAllowedMethod(HttpMethod.DELETE);
		corsConfig.setAllowedOrigins(List.of("http://localhost:5173"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/graphql", corsConfig);

		http
				.cors(cors -> cors.configurationSource(source))
				// FOR DEV ONLY
				.csrf(crsf -> crsf.disable())
				.authorizeExchange((authorize) -> authorize
						// websocket authorization is handled by the interceptor
						.pathMatchers("/subscriptions/**").permitAll()
						.anyExchange().permitAll())
				.oauth2ResourceServer((oauth2) -> oauth2
						.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));

		return http.build();
	}

	@Bean
	public ReactiveJwtDecoder jwtDecoder() {
		return ReactiveJwtDecoders
				.fromIssuerLocation("http://localhost:8090/realms/chat-app");
	}

	@Bean
	public WebSocketGraphQlInterceptor authInterceptor() {
		// Checks the websocket connectionInit payload for the key "Authentication" with
		// the value in the format of "Bearer {jwt}", then checks that jwt against the
		// keycloak issuer
		return new AuthenticationWebSocketInterceptor(
				new BearerTokenAuthenticationExtractor(),
				new JwtReactiveAuthenticationManager(jwtDecoder()));
	}

}
