/*
 * Copyright (c) 2025 Atos Spain S.A. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eviden.bds.rd.uself.agent.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

object EntryPaths {
    val secured = listOf(
        "/openid-client/*",
        "/authentic-source/*",
        "/credential-specification/*",
        "/issued-credential",
        "/status/v1/revoke/*",
        "/status/v2/suspend/*",
        "/status/v2/restore/*",
        "/tm/register-did/*",
        "/tm/register-TIR/*",
        "/tm/register-TAO/*",
        "/tm/register-RootTAO/*",
        "/tm/compliance-qualification/*"
    )
    val noSecured = listOf(
        "/",
        "/auth/*",
        "/issuer/*",
        "/.well-known/*",
    )
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Value("\${uself.security.basic.username}")
    private lateinit var username: String

    @Value("\${uself.security.basic.password}")
    private lateinit var password: String

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf { csrf ->
                csrf.disable() // Disable CSRF protection for simplicity
            }
            .authorizeHttpRequests { authorize ->
                EntryPaths.secured.forEach { path ->
                    authorize.requestMatchers(path).authenticated() // Require authentication for secured paths
                }
                EntryPaths.noSecured.forEach { path ->
                    authorize.requestMatchers(path).permitAll() // Require authentication for secured paths
                }
                authorize.anyRequest().permitAll() // Allow other paths without authentication
            }
            .httpBasic(Customizer.withDefaults())
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.withDefaultPasswordEncoder()
            .username(username)
            .password(password)
            .build()
        return InMemoryUserDetailsManager(user)
    }
}

@Configuration
class CorsWebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // Allow all endpoints
            .allowedOrigins("*") // Allow all origins
            .maxAge(HOUR) // Cache pre-flight response for 1 hour
    }

    companion object {
        private const val HOUR = 3600L // 1 hour in seconds
    }
}
