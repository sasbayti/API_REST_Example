package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] SECURED_URLs = {"/productos/**"};

    private static final String[] UN_SECURED_URLs = {
            "/users/**"
    };
    @Bean
    /* static solo cuando lo quiera crear*/ PasswordEncoder passwordEncoder() {
        // Metodo que permite encriptar o cifrar la contraseña
        return new BCryptPasswordEncoder();

    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf().disable();
                 http.authorizeHttpRequests()
                .requestMatchers(UN_SECURED_URLs).permitAll().and()
                .authorizeHttpRequests().requestMatchers(SECURED_URLs)
                .hasAuthority("ADMIN").anyRequest()
                .authenticated().and().httpBasic(withDefaults());

        return http.build();
    }
    // para crear usuarios y cifrar la contraseña
 /*   public static void main(String[] args) {

    // Ya no hace falta static si hago new
    System.out.println("La contraseña es: " 
    + new SecurityConfig().passwordEncoder().encode("Temp2023$$"));
   }  */
  
}
 