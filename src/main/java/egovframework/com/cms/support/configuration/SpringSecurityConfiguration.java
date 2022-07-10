package egovframework.com.cms.support.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin()
                .and()
                .authorizeHttpRequests((auths) -> auths
                        .antMatchers("/h2-console/**").permitAll()
                        .antMatchers(HttpMethod.PUT, "/*").denyAll()
                        .antMatchers(HttpMethod.DELETE, "/*").denyAll()
                        .antMatchers(HttpMethod.TRACE, "/*").denyAll()
                        .antMatchers(HttpMethod.OPTIONS, "/*").denyAll()
                ).httpBasic().disable()
                .csrf().disable();
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/h2-console/**");
    }
}
