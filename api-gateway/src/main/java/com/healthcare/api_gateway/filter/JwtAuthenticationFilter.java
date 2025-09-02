package com.healthcare.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String SECRET_KEY = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> openApiEndpoints = List.of(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/auth/register/doctor",
            "/auth/register/patient",
            "/auth/login",
            "/auth/refresh",
            "/auth/forgot-password",
            "/api/messaging/otp/send",
            "/api/messaging/otp/verify"
    );

    private static final Map<String, Map<String, List<String>>> protectedEndpointsWithRoles = new HashMap<>();

    static {
        // Patient service URLs
        protectedEndpointsWithRoles.put("/patients", Map.of(
                "POST", List.of("ROLE_PATIENT"),
                "GET", List.of("ROLE_ADMIN")
        ));
        protectedEndpointsWithRoles.put("/patients/*", Map.of(
                "GET", List.of("ROLE_PATIENT", "ROLE_ADMIN"),
                "PUT", List.of("ROLE_PATIENT")
        ));
        protectedEndpointsWithRoles.put("/patients/*/bookings", Map.of("GET", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/book", Map.of("POST", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/book/*/cancel", Map.of("POST", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/payments/intent", Map.of("POST", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/doctor/*", Map.of("GET", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/doctor/*/availability", Map.of("GET", List.of("ROLE_PATIENT")));
        protectedEndpointsWithRoles.put("/patients/doctor/*/reviews", Map.of("GET", List.of("ROLE_PATIENT")));

        // Doctor service URLs
        protectedEndpointsWithRoles.put("/doctors/*/verify", Map.of("PUT", List.of("ROLE_ADMIN")));
        protectedEndpointsWithRoles.put("/doctors/*/activation", Map.of("PUT", List.of("ROLE_ADMIN")));
        protectedEndpointsWithRoles.put("/doctors", Map.of("POST", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*", Map.of("GET", List.of("ROLE_DOCTOR", "ROLE_ADMIN")));
        protectedEndpointsWithRoles.put("/doctors/*/profile", Map.of("PUT", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/availability/slots", Map.of(
                "POST", List.of("ROLE_DOCTOR"),
                "GET", List.of("ROLE_DOCTOR")
        ));
        protectedEndpointsWithRoles.put("/doctors/*/availability/slots/block", Map.of("PUT", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/appointments/upcoming", Map.of("GET", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/appointments/history", Map.of("GET", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/reviews", Map.of("GET", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/reviews/refresh-rating", Map.of("POST", List.of("ROLE_DOCTOR")));
        protectedEndpointsWithRoles.put("/doctors/*/prescriptions", Map.of(
                "POST", List.of("ROLE_DOCTOR"),
                "GET", List.of("ROLE_DOCTOR")
        ));

        // Generate appointment-service URLs dynamically
        List<String> appointmentUrls = List.of(
                "/appointments/*/confirm",
                "/appointments/*/complete",
                "/appointments/*/reject",
                "/appointments/*/cancel",
                "/appointments/*/reschedule",
                "/appointments/doctor/*/upcoming",
                "/appointments/doctor/*/past",
                "/appointments/patient/*/past",
                "/appointments/patient/*"
        );

        Map<String, List<String>> appointmentRoleMap = new HashMap<>();
        appointmentRoleMap.put("PUT", List.of("ROLE_DOCTOR", "ROLE_PATIENT")); // default
        appointmentRoleMap.put("GET", List.of("ROLE_DOCTOR", "ROLE_PATIENT")); // default

        for (String url : appointmentUrls) {
            Map<String, List<String>> methodRoles = new HashMap<>();
            if (url.contains("/confirm") || url.contains("/complete")) {
                methodRoles.put("PUT", List.of("ROLE_DOCTOR"));
            } else if (url.contains("/reject")) {
                methodRoles.put("PUT", List.of("ROLE_DOCTOR", "ROLE_ADMIN"));
            } else if (url.contains("/cancel") || url.contains("/reschedule")) {
                methodRoles.put("PUT", List.of("ROLE_DOCTOR", "ROLE_PATIENT"));
            } else if (url.contains("/doctor")) {
                methodRoles.put("GET", List.of("ROLE_DOCTOR"));
            } else if (url.contains("/patient")) {
                methodRoles.put("GET", List.of("ROLE_PATIENT"));
            }
            protectedEndpointsWithRoles.put(url, methodRoles);
        }
    }

    private Key getSigningKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        System.out.println("[JwtAuth] Incoming path: " + path);
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "";

        if (isPublicEndpoint(path)) {
            System.out.println("[JwtAuth] Public endpoint — skipping auth.");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JwtAuth] Missing or malformed Authorization header.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);
            System.out.println("[JwtAuth] Role from token: " + role);

            if (!isAuthorized(path, method, role)) {
                System.out.println("[JwtAuth] Access denied for role: " + role);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            exchange = exchange.mutate()
                    .request(r -> r.header("X-User-Role", role))
                    .build();

        } catch (JwtException e) {
            System.out.println("[JwtAuth] JWT validation error: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return openApiEndpoints.stream().anyMatch(path::startsWith);
    }

    private boolean isAuthorized(String path, String method, String role) {
        for (Map.Entry<String, Map<String, List<String>>> entry : protectedEndpointsWithRoles.entrySet()) {
            if (pathMatcher.match(entry.getKey(), path)) {
                List<String> allowedRoles = entry.getValue().getOrDefault(method.toUpperCase(), List.of());
                return allowedRoles.contains(role);
            }
        }
        return true;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
