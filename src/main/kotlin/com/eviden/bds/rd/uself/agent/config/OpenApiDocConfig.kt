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

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
//    scheme = "bearer",
    scheme = "basic",
    bearerFormat = "JWT"
)
class OpenApiDocConfig {
    @Value("\${uself.server}")
    private lateinit var server: String

    @Value("\${uself.preload}")
    private lateinit var preload: String

    @Value("\${uself.version}")
    private lateinit var version: String

    @Bean
    fun springAgentOpenAPI(): OpenAPI? {
        val (licenseName, licenseURL) = if (preload.contains("dc4eu")) {
            Pair("Apache License, Version 2.0", "https://www.apache.org/licenses/LICENSE-2.0.txt")
        } else {
            Pair("TBD", "https://eviden.com")
        }
        return OpenAPI()
            .info(
                Info()
                    .title("Ledger uSelf Agent API")
                    .description(
                        """
                        Ledger uSelf Agent, server to help to the Service Providers to integrate their own services with a SSI solution
                        """.trimIndent()
                    )
                    .version(version)
                    .license(License().name(licenseName).url(licenseURL))
            )
            .servers(listOf(Server().url(server)))
            .externalDocs(
                ExternalDocumentation()
                    .description(
                        """
                            This API is based on the  Electronic Identification, Authentication and Trust Services (eIDAS) Regulation defined within the Architecture and Reference Framework (ARF).
                        """.trimIndent()
                    )
                    .url(
                        """
                         https://eu-digital-identity-wallet.github.io/eudi-doc-architecture-and-reference-framework/latest/
                        """.trimIndent()
                    )
            )
    }
}
