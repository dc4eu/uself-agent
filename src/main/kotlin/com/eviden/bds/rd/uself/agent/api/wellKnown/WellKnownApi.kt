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
@file:Suppress("MaxLineLength", "NoWildcardImports", "LongParameterList")

package com.eviden.bds.rd.uself.agent.api.wellKnown

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.openid.auth.OpenIdConfiguration
import com.eviden.bds.rd.uself.common.models.openid.issuer.OpenIdCredentialIssuer
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Tag(name = ".well-known API", description = "API to obtain information needed to interact with OAuth 2.0")
interface WellKnownApi {

    @Operation(
        summary = "Authorisation Server discovery metadata",
        description = """Client or a wallet must obtain the Authorisation Server metadata prior to the transaction using strategies described in [OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/info/rfc8414) or out-of-band. """,
        tags = [".well-known API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OpenIdConfiguration::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/.well-known/openid-configuration"],
        produces = ["application/json"],
        method = [RequestMethod.GET]
    )
    fun getOpenIdConfiguration(session: HttpSession): ResponseEntity<OpenIdConfiguration>

    @Operation(
        summary = "Credential Issuer discovery metadata",
        description = "Client or a wallet must obtain the Credential Issuer metadata prior to the transaction using strategies " +
            "described in [OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/info/rfc8414) or out-of-band. ",
        tags = [".well-known API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OpenIdCredentialIssuer::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/.well-known/openid-credential-issuer"],
        produces = ["application/json"],
        method = [RequestMethod.GET]
    )
    fun getOpenIdCredentialIssuer(session: HttpSession): ResponseEntity<OpenIdCredentialIssuer>
}
