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

package com.eviden.bds.rd.uself.agent.api.auth

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.openid.auth.Jwks
import com.eviden.bds.rd.uself.common.models.openid.auth.OpenIdConfiguration
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenResponse
import com.eviden.bds.rd.uself.common.models.openid.auth.UserInfo
import com.eviden.bds.rd.uself.common.models.openid.issuer.InlineResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.Suppress as Suppress

@Tag(name = "Authorize API", description = "Authorization Server API")
interface AuthApi {

    // fun getOpenIdConfiguration(@PathVariable version: Versions): ResponseEntity<String>
    @Operation(
        summary = "Authorisation Server discovery metadata",
        description = """Client or a wallet must obtain the Authorisation Server metadata prior to the transaction using strategies described in [OAuth 2.0 Authorization Server Metadata](https://www.rfc-editor.org/info/rfc8414) or out-of-band. """,
        tags = ["Authorize API"]
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
        //  value = ["{version}/auth/.well-known/openid-configuration"],
        value = ["/auth/.well-known/openid-configuration"],
        produces = ["application/json"],
        method = [RequestMethod.GET]
    )
    fun getOpenIdConfiguration(session: HttpSession): ResponseEntity<OpenIdConfiguration>

    @Operation(
        summary = "OIDC Authorization endpoint",
        description = "After the discovery, the client proceeds with Verifiable Credential Issuance flow by " +
            "requesting access for the required credential, from the Authorisation Server. " +
            "The Authorisation Request Object must be signed with the client's private keys, owned by the requesting the client_id." +
            " The client's public key must be discoverable through client's openid-configuration through the jwks_uri parameter.",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "Authorisation Server responds with one of the three responses: " +
                    "  - ID Token Request   - VP Token Request   - Error codes for authorization endpoint " +
                    " All responses are in the \"Location\" header parameter and are x-www-form-urlencoded "

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
        value = ["/auth/authorize"],
        produces = ["application/json", "application/problem+json"],
        method = [RequestMethod.GET]
    )
    fun getAuthorize(
        session: HttpSession,
        @NotNull @Parameter(
            description = "OpenID Connect requests MUST contain the openid scope value. If the openid scope value is not present, " +
                "the behavior is entirely unspecified. ",
            required = true
        ) @Valid @RequestParam(value = "scope", required = true) scope: String,
        @NotNull @Parameter(
            description = "REQUIRED. OAuth 2.0 Response Type value that determines the authorization processing flow to be used, " +
                "including what parameters are returned from the endpoints used. When using the Authorization Code Flow, this value is code.  MUST be 'code' ",
            required = true
        ) @Valid @RequestParam(value = "response_type", required = true) responseType: String,
        @NotNull @Parameter(
            description = "OAuth 2.0 Client Identifier valid at the Authorization Server." +
                "  Verifiable Accreditation Issuance: MUST be URL of the issuer requesting the accreditation that was registered with the Accreditation Issuer ",
            required = true
        ) @Valid @RequestParam(value = "client_id", required = true) clientId: String,
        @NotNull @Parameter(
            description = "REQUIRED. Redirection URI to which the response will be sent. This URI MUST exactly match one of the Redirection URI values for the Client pre-registered at the OpenID Provider. ",
            required = true
        ) @Valid @RequestParam(value = "redirect_uri", required = true) redirectUri: String,
        @Parameter(
            description = "RECOMMENDED. Opaque value used to maintain state between the request and the callback. Typically, Cross-Site Request Forgery (CSRF, XSRF) mitigation is done by cryptographically binding the value of this parameter with a browser cookie. ",
        ) @Valid @RequestParam(value = "state", required = false) state: String?,
        @Parameter(
            description = "OPTIONAL. String value used to associate a Client session with an ID Token, and to mitigate replay attacks. The value is passed through unmodified from the Authentication Request to the ID or VP Token. Sufficient entropy MUST be present in the nonce values used to prevent attackers from guessing values. ",
        ) @Valid @RequestParam(value = "nonce", required = false) nonce: String?,
        @Parameter(
            description = "Only for Service Wallets. Authorisation Request Object - The Request Object must be signed with the client's private keys, owned by the requesting client_id. The used private key's public key must be discoverable through client's openid-configuration through jwks_uri parameter.  See the Authorisation Request Object schema. "
        ) @Valid @RequestParam(value = "request", required = false) request: String?,
        @Parameter(
            description = "Only for Holder Wallets.  OID authorisation details data model.  Note: `authorization_details` must be a stringified JSON array.  See \"OID4VCI Authorisation Details\" schema for more information. "
        ) @Valid @RequestParam(value = "authorization_details", required = false) authorizationDetails: String?,
        @Parameter(
            description = "Only for Holder Wallets. Client Metadata including a link to an `authorization_endpoint` (optional). " +
                " Note: `client_metadata` must a stringified JSON object.  See \"Client Metadata\" schema for more information. "
        ) @Valid @RequestParam(value = "client_metadata", required = false) clientMetadata: String?,
        @Parameter(
            description = "REQUIRED if Credential Offering contained `issuer_state`. ",
        ) @Valid @RequestParam(value = "issuer_state", required = false) issuerState: String?,
        @Parameter(
            description = "Only for Holder Wallets. In format of `BASE64URL-ENCODE(SHA256(code_verifier as UTF-8 string))`. " +
                " `code_verifier` is client generated secure random, which will be used with token endpoint. It is between 43 and 128 characters long, " +
                "and contains characters A-Z, a-z, 0-9, hyphen, period, underscore, and tilde. Please see " +
                "[RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636#section-4.1) ",
        ) @Valid @RequestParam(value = "code_challenge", required = false) codeChallenge: String?,
        @Parameter(
            description = "Only for Holder Wallets. MUST be \"S256\". "
        ) @Valid @RequestParam(value = "code_challenge_method", required = false) codeChallengeMethod: String?,
        @Parameter(
            description = "If redirect is not defined or true the response will be redirected using 302 answer " +
                "(Out of the standard for helping implementation) "
        ) @Valid @RequestParam(value = "redirect", required = false) redirect: Boolean?
    ): ResponseEntity<Any>

    // fun postDirectPost(@Parameter(description = "") @Valid @RequestBody body: AuthDirectPost): ResponseEntity<Void>
    @Operation(
        summary = "Direct Post",
        description = "Authorisation Server's endpoint that accepts and processes the ID Token issued by the client. " +
            "The ID Token is self-issued by the client and it MUST be signed with client's DID document's authentication key.  " +
            "See the ID Token - Direct POST response schema. ",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "OK"
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
        value = ["/auth/direct_post"],
        // produces = ["application/problem+json"],
        // consumes = ["application/json", "application/x-www-form-urlencoded", "application/x-www-form-urlencoded;charset=UTF-8"],
        // consumes = ["application/x-www-form-urlencoded;charset=UTF-8"],
        method = [RequestMethod.POST]
    )
    fun postDirectPost(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
        // , @Parameter(description = "") @Valid @RequestBody body: AuthDirectPost
    ): ResponseEntity<String>

    @Operation(
        summary = "Direct Post Passport",
        description = "Authorisation Server's endpoint that accepts and processes the ID Token issued by ePassport app." +
            "The ID Token is self-issued by the client and it MUST be signed with client's DID document's authentication key.  " +
            "See the ID Token - Direct POST response schema. ",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "OK"
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
        value = ["/auth/direct_post/epassport"],
        produces = ["application/problem+json"],
        method = [RequestMethod.POST]
    )
    fun postDirectPostPassport(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
    ): ResponseEntity<Void>

    @Operation(
        summary = "Token endpoint",
        description = "The client proceeds with the code flow, and calls the Token Endpoint with " +
            "the required details and signs client_assertion JWT with client's private keys, which public key counterparts are resolvable " +
            "through jwks_uri or is in the Client Metadata shared in the pre-registration step. ",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class)
                    )
                ]
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
                responseCode = "401",
                description = "Unauthorised",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            ), ApiResponse(responseCode = "500", description = "")
        ]
    )
    @RequestMapping(
        value = ["/auth/token"],
        produces = ["application/json", "application/problem+json"],
        // consumes = ["application/json","application/x-www-form-urlencoded","application/x-www-form-urlencoded;charset=UTF-8"],
        // consumes = ["application/x-www-form-urlencoded","application/x-www-form-urlencoded;charset=UTF-8"],
        method = [RequestMethod.POST]
    )
    fun postToken(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
        // ,@RequestBody body: TokenRequest
    ): ResponseEntity<TokenResponse>

    @Operation(
        summary = "Request by reference",
        description = "Authorisation Server's endpoint that returns a [Request Object](https://openid.net/specs/openid-connect-core-1_0.html#UseRequestUri)." +
            " request_uri SHOULD be used whenever the request object is large. ",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = "application/jwt", schema = Schema(implementation = String::class))]
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
                description = "Request Not Found",
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
        value = ["/auth/request_uri/{requestId}"],
        // produces = ["application/jwt", "application/json", "application/problem+json"],
        method = [RequestMethod.GET]
    )
    fun getRequestUri(
        session: HttpSession,
        @Parameter(
            description = "Unique Request ID",
            required = true
        ) @PathVariable("requestId") requestId: String
    ): ResponseEntity<String>

    @Operation(
        summary = "Authorisation Server's JWKS endpoint",
        description = "A JSON Web Key (JWK) used by the authorisation server. [RFC4627]. ",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Success",
                content = [
                    Content(
                        mediaType = "application/jwk-set+json",
                        schema = Schema(implementation = Jwks::class)
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
        value = ["/auth/jwks"],
        produces = ["application/jwk-set+json", "application/problem+json"],
        method = [RequestMethod.GET]
    )
    fun jwks(session: HttpSession): ResponseEntity<Jwks>

    @Operation(
        summary = "Callback request to trigger the corresponding event",
        description = "this endpoint is used to trigger the callback event",
        tags = ["Authorize API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserInfo::class)
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
        value = ["/auth/callback"],
        produces = ["application/json", "application/problem+json"],
        consumes = ["*/*", "application/json"],
        method = [RequestMethod.GET]
    )
    fun callback(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
    ): ResponseEntity<Void>
}
