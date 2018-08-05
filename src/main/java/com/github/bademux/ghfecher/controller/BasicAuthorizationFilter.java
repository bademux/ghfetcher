package com.github.bademux.ghfecher.controller;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;


@Slf4j
@RequiredArgsConstructor
public final class BasicAuthorizationFilter implements WebFilter {

    private static final Charset CREDENTIAL_CHARSET = StandardCharsets.UTF_8;
    private static final String BASIC_AUTH_PREFIX = "Basic ";

    private final Set<User> users;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeaderValue = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (isAuthorized(authHeaderValue)) {
            return chain.filter(exchange);
        }

        return Mono.error(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    }

    boolean isAuthorized(String authHeaderValue) {
        return Optional.ofNullable(authHeaderValue)
                .map(this::extractToken)
                .map(this::decodeToken)
                .map(User::fromDelimited)
                .filter(this::isAuthorized)
                .isPresent();
    }


    private boolean isAuthorized(User user) {
        return users.contains(user);
    }

    private String extractToken(String authHeaderValue) {
        int prefixLength = BASIC_AUTH_PREFIX.length();
        if (authHeaderValue.regionMatches(true, 0, BASIC_AUTH_PREFIX, 0, prefixLength)) {
            return authHeaderValue.substring(prefixLength);
        }
        log.debug("bad header prefix");
        return null;
    }

    private String decodeToken(String token) {
        try {
            byte[] data = Base64Utils.decodeFromString(token);
            return new String(data, CREDENTIAL_CHARSET);
        } catch (Throwable e) {
            log.trace("bad basic auth token");
            return null;
        }
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    public static class User {
        private final String login;
        private final String password;

        public static User fromDelimited(String token) {
            int delim = token.indexOf(":");
            if (delim == -1) {
                log.trace("no delimeter found");
                return null;
            }

            String login = token.substring(0, delim);
            String password = isPasswordEmpty(delim, token.length()) ? "" : token.substring(delim + 1);
            return User.of(login, password);
        }

        private static boolean isPasswordEmpty(int delim, int length) {
            return delim == length;
        }
    }

}
