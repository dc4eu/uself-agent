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

import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import jakarta.servlet.*
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

//
//@Configuration
//class FilterConfig {
//    @Bean
//    fun authenticationFilterRegistration(
//        authFilter: AuthenticationFilter
//    ): FilterRegistrationBean<AuthenticationFilter> {
//        val registrationBean = FilterRegistrationBean<AuthenticationFilter>()
//        registrationBean.filter = authFilter
//        // Apply this filter to specific URL patterns
//        SecuredPaths.list.forEach {
//            registrationBean.addUrlPatterns(it)
//        }
//        // You can also set the order if you have multiple filters
//        // registrationBean.order = 1
//        //registrationBean.order = 15
//        registrationBean.order = Ordered.HIGHEST_PRECEDENCE
//        return registrationBean
//    }
//}
//
//@Component
//@OptIn(ExperimentalEncodingApi::class)
//class AuthenticationFilter : Filter {
//
//    private val tracker: Tracker = Tracker(this::class.java)
//
//    // Inject username and password from application properties or environment variables
//    @Value("\${uself.security.basic.username}")
//    private lateinit var expectedUsername: String
//
//    @Value("\${uself.security.basic.password}")
//    private lateinit var expectedPassword: String
//
//    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
//        val httpRequest = request as HttpServletRequest
//        val httpResponse = response as HttpServletResponse
//        val requestPath = httpRequest.servletPath
//        val isSecuredPath = SecuredPaths.list.any { path -> requestPath.startsWith(path) }
//        if (isSecuredPath) {
//            val authHeader = httpRequest.getHeader("Authorization")
//            if (authHeader != null && verifyAuth(authHeader)) {
//                chain.doFilter(request, response) // User is authenticated, proceed
//            } else {
//                // throw Exception401("Unauthorized access attempt to $requestPath")
//                tracker.error("Unauthorized: Invalid to $requestPath or missing Basic Authentication credentials.", "")
//                httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
//                httpResponse.writer.write(
//                    "Unauthorized: Invalid to $requestPath or missing Basic Authentication credentials."
//                )
//                return // Stop processing
//            }
//        } else {
//            chain.doFilter(request, response) // Path is not secured by this filter, proceed
//        }
//    }
//
//    private fun verifyAuth(basicAuthToken: String): Boolean {
//        if (!basicAuthToken.startsWith("Basic ", ignoreCase = true)) {
//            tracker.debug("Authorization header does not start with Basic: $basicAuthToken")
//            return false // Not a Basic Auth token
//        }
//
//        val credentialsToEncode = "$expectedUsername:$expectedPassword"
//        val expectedBase64Credentials = Base64.Default.encode(
//            credentialsToEncode.toByteArray(StandardCharsets.UTF_8)
//        )
//        val isValid = basicAuthToken == "Basic $expectedBase64Credentials"
//        if (!isValid) {
//            tracker.error(
//                "Authorization failed",
//                "Provided Basic Auth credentials do not match expected credentials."
//            )
//        }
//        return isValid
//    }
//
//    override fun destroy() {
//        // Cleanup code, if any
//        tracker.info("AuthenticationFilter destroyed.")
//    }
//}
//
//@Configuration
//class CorsWebConfig : WebMvcConfigurer {
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**") // Allow all endpoints
//            .allowedOrigins("*") // Allow all origins
////            .allowedOriginPatterns("*") // Allow all origins
////            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specific HTTP methods
////            .allowedHeaders("Authorization", "Content-Type", "Accept") // Allow Authorization and Content-Type headers
////            .exposedHeaders("Authorization")
//            // .allowCredentials(true) // Allow credentials
//            .maxAge(3600) // Cache pre-flight response for 1 hour
////        registry.addMapping("/**")
////            //.allowedOriginPatterns("*")
////            .allowedOriginPatterns("*","http://localhost:8888", "http://localhost:4200")
////            //.allowedHeaders("Authorization")
////            .maxAge(3600)
//    }


