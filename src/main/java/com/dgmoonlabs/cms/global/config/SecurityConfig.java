package com.dgmoonlabs.cms.global.config;

import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.JsonErrorWriter;
import com.dgmoonlabs.cms.global.security.ZonedSecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {
    private static final RequestMatcher API = PathPatternRequestMatcher.pathPattern("/api/**");

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http, final SecurityContextRepository securityContextRepository,
                                            final JsonErrorWriter jsonErrorWriter) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members/username-availability").permitAll()
                .requestMatchers("/api/members/me/**", "/api/members/me").hasRole("MEMBER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/files").authenticated()
                .requestMatchers("/api/orders/**", "/api/orders", "/api/reservations/**", "/api/reservations",
                        "/api/payments/**", "/api/payments", "/api/subscriptions/**", "/api/cart/**", "/api/cart").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/coupons/*/download").hasRole("MEMBER")
                .requestMatchers("/api/digital/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews", "/api/programs/*/reviews").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/satisfactions").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/reports/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/media/me", "/api/applications/me", "/api/courses/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/courses/*/enroll", "/api/courses/*/lessons/*/quiz").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/submissions", "/api/applications/submissions/*/withdraw").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/media/*/start", "/api/media/*/progress").hasRole("MEMBER")
                .requestMatchers(API).permitAll()
                .anyRequest().permitAll()
        );

        http.securityContext(context -> context.securityContextRepository(securityContextRepository));

        // 애플 콜백은 cross-site POST 라 CSRF 토큰이 없다. 리다이렉트만 한다.
        http.csrf(configurer -> configurer.spa()
                .ignoringRequestMatchers(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/oauth/apple/callback")));

        http.exceptionHandling(handling -> handling
                .defaultAuthenticationEntryPointFor(
                        (request, response, e) -> jsonErrorWriter.write(response, ErrorCode.UNAUTHENTICATED), API)
                .defaultAccessDeniedHandlerFor(
                        (request, response, e) -> jsonErrorWriter.write(response, ErrorCode.ACCESS_DENIED), API)
        );

        http.formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    SecurityContextRepository securityContextRepository(final ZonedSecurityContextRepository zonedRepository) {
        return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(), zonedRepository);
    }
}
