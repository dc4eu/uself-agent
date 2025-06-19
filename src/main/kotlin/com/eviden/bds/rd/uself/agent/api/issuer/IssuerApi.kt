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
@file:Suppress("NoWildcardImports", "MaxLineLength", "LongParameterList")

package com.eviden.bds.rd.uself.agent.api.issuer

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.openid.issuer.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Issuer API", description = "Issuer API")
interface IssuerApi {
    @Operation(
        summary = "Credential Issuer discovery metadata",
        description = "Client or a wallet must obtain the Credential Issuer metadata prior to the transaction using strategies " +
            "described in [OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/info/rfc8414) or out-of-band. ",
        tags = ["Issuer API"]
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
        value = ["/issuer/.well-known/openid-credential-issuer"],
        produces = ["application/json"],
        method = [RequestMethod.GET]
    )
    fun getOpenIdCredentialIssuer(session: HttpSession): ResponseEntity<OpenIdCredentialIssuer>

    @Operation(
        summary = "Credential Issuer Initiate operation",
        description = "The Issuer Initiate the flow, if it will be for cross device the result should be formatted as QRCode and if the same device it should answer with a redirection (302) ",
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["Issuer API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ), ApiResponse(
                responseCode = "302",
                description = "Issuer Server responds with one of the three responses:  " +
                    " - Error codes for authorization endpoint  All responses are in the \"Location\" header parameter and are x-www-form-urlencoded "
            ), ApiResponse(
                responseCode = "400",
                description = "Authorisation Endpoint Error ",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Error",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/issuer/initiate-credential-offer"],
        produces = ["application/json", "application/problem+json"],
        method = [RequestMethod.GET]
    )
    fun getInitiate(
        session: HttpSession,
        @RequestHeader("Authorization") bearerToken: String?,
        @Parameter(
            description = "OPTIONAL. To define if the credential is deferred flow or in time",
            required = false
        ) @Valid @RequestParam(value = "deferred", required = false) deferred: Boolean?,
        @NotNull @Parameter(
            description = "OPTIONAL. To define the type of result, the possible values are redirect=true -> same-device and therefore the response will be Redirected using 302 location, " +
                    "redirect=false -> cross device and the answer won't be redirect and will be used for generating a QRCode ",
            required = false
        ) @Valid @RequestParam(value = "redirect", required = false) redirect: Boolean?,
        @Parameter(
            description = "OPTIONAL. To define if the credential is pre-authorized flow or use a Authorized flow",
            required = false
        ) @Valid @RequestParam(value = "pre-authorized", required = false) preAuthorized: Boolean?,
        @Parameter(
            description = "OPTIONAL. To define the nonce, the value should be a random string, " +
                "the same value should be used in the response. ",
            required = false
        ) @Valid @RequestParam(value = "nonce", required = false) nonce: String?,

        @NotNull @Parameter(
            description = "REQUIRED.The credential to use: " +
                "SAME_IN_TIME = \"CTWalletSameAuthorisedInTime\"\n" +
                "SAME_DEFERRED = \"CTWalletSameAuthorisedDeferred\"\n" +
                "SAME_PRE_AUTH = \"CTWalletSamePreAuthorisedInTime\"\n" +
                "CROSS_IN_TIME = \"CTWalletCrossAuthorisedInTime\"\n" +
                "CROSS_DEFERRED = \"CTWalletCrossAuthorisedDeferred\"\n" +
                "CROSS_PRE_AUTH = \"CTWalletCrossPreAuthorisedInTime\"\n" +

                "TANGO_EPASSPORT_INFO = \"ePassport\"",
            required = true
        ) @Valid @RequestParam(value = "credential_type", required = true) credentialType: String,
        @Parameter(
            description = "OPTIONAL. Required for the EBSI's test. client_id it should be did:key:<NP> ",
            required = false
        ) @Valid @RequestParam(value = "client_id", required = false) clientId: String?,

        @NotNull @Parameter(
            description = "REQUIRED. To define the the output, by default: credential_offer_endpoint=openid-credential-offer:// ",
            required = true
        ) @Valid @RequestParam(value = "credential_offer_endpoint", required = true) credentialOfferEndpoint: String

    ): ResponseEntity<Any>

    @Operation(
        summary = "Credential Offer by reference",
        description = "Issuer endpoint that returns a [Credential Offer](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-11.html#section-4.1.3). " +
            "credential_offer_uri SHOULD be used whenever the credential_offer object is large. ",
        tags = ["Issuer API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CredentialOfferResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "302",
                description = "FOUND"
            ), ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "404",
                description = "Credential Offer Not Found",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Error",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/issuer/offers/{credentialOfferId}"],
        produces = ["application/json", "application/problem+json"],
        method = [RequestMethod.GET]
    )
    fun getCredentialOfferByID(
        session: HttpSession,
        @Parameter(
            description = "Unique Credential Offer ID",
            required = true
        ) @PathVariable("credentialOfferId") credentialOfferId: String
    ): ResponseEntity<CredentialOfferResponse>

    @Operation(
        summary = "Credential endpoint",
        description = "The client proceeds with the code flow, and calls the Token Endpoint with the required details and " +
            "signs client_assertion JWT with client's private keys, which public key counterparts are resolvable through jwks" +
            "_uri or is in the Client Metadata shared in the pre-registration step. ",
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["Issuer API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CredentialResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "400",
                description = "Invalid request error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = InlineResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "401",
                description = "Unauthorised request error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = InlineResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Error",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/issuer/credential"],
        produces = ["application/json", "application/problem+json"],
        consumes = ["application/json"],
        method = [RequestMethod.POST]
    )
    fun postCredential(
        session: HttpSession,
        @RequestHeader("Authorization") bearerToken: String,
        @Parameter(
            description = ""
        ) @Valid @RequestBody body: CredentialRequest
    ): ResponseEntity<CredentialResponse>

    @Operation(
        summary = "Deferred credential endpoint",
        description = "If the `/credential` endpoint returned an `acceptance_token` parameter, the client can call " +
            "the `/credential_deferred` endpoint in order to receive the requested credential. " +
            "The `acceptance_token` parameter MUST be sent as Access Token in the HTTP header. ",
        security = [SecurityRequirement(name = "bearerAuth")],
        tags = ["Issuer API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = DeferredCredentialResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "400",
                description = "Invalid request error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = InlineResponse::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Error",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @RequestMapping(
        value = ["/issuer/credential-deferred"],
        produces = ["application/json", "application/problem+json"],
        method = [RequestMethod.POST]
    )
    fun postCredentialDeferred(): ResponseEntity<DeferredCredentialResponse>
}
