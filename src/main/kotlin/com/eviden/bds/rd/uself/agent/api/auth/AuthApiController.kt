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

@file:Suppress("NoWildcardImports", "LoggingSimilarMessage")

package com.eviden.bds.rd.uself.agent.api.auth

import com.eviden.bds.rd.uself.agent.services.openid.auth.AuthService
import com.eviden.bds.rd.uself.common.models.openid.auth.*
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class AuthApiController(private val authService: AuthService) : AuthApi {

    private val tracker = Tracker(this::class.java)

    private val authMetURL = " /auth/.well-known/openid-configuration "
    private val jwksURL = " /auth/jwks "
    private val authURL = " /auth/authorize "
    private val reqByIdURL = "/auth/request_uri"
    private val directPostURL = "/auth/direct_post"
    private val directEPassPostURL = "/auth/epassport"
    private val tokenURL = "/auth/token"
    private val callbackURL = "/auth/callback"

    override fun getOpenIdConfiguration(session: HttpSession): ResponseEntity<OpenIdConfiguration> {
        tracker.infoGET(authMetURL)
        tracker.info("Session ID: ${session.id}")
        val metadata = authService.getMetadata()
        tracker.infoRESP200(authMetURL, metadata)
        return ResponseEntity<OpenIdConfiguration>(metadata, HttpStatus.OK)
    }

    override fun jwks(session: HttpSession): ResponseEntity<Jwks> {
        tracker.infoGET(jwksURL)
        tracker.debug("Session ID: ${session.id}")
        val jks = authService.getJWKs(session)
        tracker.infoRESP200(jwksURL, jks)
        return ResponseEntity<Jwks>(jks, HttpStatus.OK)
    }

// get improve this method

    override fun getAuthorize(
        session: HttpSession,
        @RequestParam(required = true, value = "scope") scope: String,
        @RequestParam(required = true, value = "response_type") responseType: String,
        @RequestParam(required = true, value = "client_id") clientId: String,
        @RequestParam(required = true, value = "redirect_uri") redirectUri: String,
        @RequestParam(required = false, value = "state") state: String?,
        @RequestParam(required = false, value = "nonce") nonce: String?,
        @RequestParam(required = false, value = "request") request: String?,
        @RequestParam(required = false, value = "authorization_details") authorizationDetails: String?,
        @RequestParam(required = false, value = "client_metadata") clientMetadata: String?,
        @RequestParam(required = false, value = "issuer_state") issuerState: String?,
        @RequestParam(required = false, value = "code_challenge") codeChallenge: String?,
        @RequestParam(required = false, value = "code_challenge_method") codeChallengeMethod: String?,
        @RequestParam(required = false, value = "redirect") redirect: Boolean?
    ): ResponseEntity<Any> {
        // the required values are scope, response:type, client_id and redirect_uri
        // Here I should verify the data passed by
        // scope must be openid

        // If input parameters are all right we will process the data
        val authRequest = AuthorisationRequest(
            scope = scope,
            responseType = responseType,
            clientId = clientId,
            redirectUri = redirectUri,
            state = state,
            nonce = nonce,
            request = request,
            authorizationDetails = authorizationDetails?.let { Json.decodeFromString(it) },
            clientMetadata = clientMetadata?.let { Json.decodeFromString(it) },
            issuerState = issuerState,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
            redirect = redirect
        )

        tracker.infoGET(authURL, authRequest)
        tracker.debug("Session ID: ${session.id}")

        val response = authService.getAuthorize(session, authRequest)

        if (redirect != null && redirect == false) {
            tracker.infoRESP200(authURL, response)
            return ResponseEntity<Any>(response, HttpStatus.OK)
        }
        tracker.infoRESP302(authURL, response)
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response)).build()
    }

    override fun getRequestUri(
        session: HttpSession,
        @PathVariable(
            "requestId"
        ) requestId: String
    ): ResponseEntity<String> {
        tracker.infoGET("$reqByIdURL/$requestId")
        val response = authService.getRequestURI(session, requestId)
        tracker.infoRESP200("$reqByIdURL/$requestId", response)
        return ResponseEntity(response, HttpStatus.OK)
    }

    override fun postDirectPost(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
    ): ResponseEntity<String> {
        val authDirectPost = AuthDirectPost(
            redirectUri = paramMap["redirect_uri"],
            idToken = paramMap["id_token"],
            vpToken = paramMap["vp_token"],
            state = paramMap["state"],
            presentationSubmission = paramMap["presentation_submission"]
        )
        tracker.infoPOST(directPostURL, authDirectPost)
        tracker.debug("Session ID: ${session.id}")
        val response = authService.postDirectPost(session, authDirectPost)
        tracker.infoRESP302(directPostURL, response)
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response)).build()
    }

    override fun postDirectPostPassport(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
    ): ResponseEntity<Void> {
        val authDirectPost = AuthDirectPost(
            idToken = paramMap["id_token"],
            state = paramMap["state"]
        )

        tracker.infoPOST(directEPassPostURL, authDirectPost)
        val response = authService.postDirectPostPassport(session, authDirectPost)
        tracker.infoRESP302(directEPassPostURL, response)
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response)).build()
    }

    override fun postToken(
        session: HttpSession,
        @RequestParam paramMap: MutableMap<String, String>
    ): ResponseEntity<TokenResponse> {
        val tokenRequest = if (paramMap.isNotEmpty()) {
            TokenRequest(
                redirectUri = paramMap["redirect_uri"],
                grantType = paramMap["grant_type"],
                clientId = paramMap["client_id"],
                code = paramMap["code"],
                codeVerifier = paramMap["code_verifier"],
                clientAssertionType = paramMap["client_assertion_type"],
                preAuthorizedCode = paramMap["pre-authorized_code"],
                userPin = paramMap["user_pin"]
            )
        } else {
            // body
            TokenRequest()
        }

        tracker.infoPOST(tokenURL, tokenRequest)
        tracker.debug("Session ID: ${session.id}")
        val response = authService.postToken(session, tokenRequest)
        tracker.infoRESP200(tokenURL, response)

        // Set the access token in a cookie
        val accessTokenCookie = ResponseCookie
            .from("access_token", response.accessToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .build()
        // Set the id token in a cookie
        val idTokenCookie = ResponseCookie
            .from("id_token", response.idToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .build()

        // Return the response with the access token in the cookie
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            .header(HttpHeaders.SET_COOKIE, idTokenCookie.toString())
            .body(response)
    }

    override fun callback(session: HttpSession, @RequestParam paramMap: MutableMap<String, String>): ResponseEntity<Void> {
        tracker.infoGET(callbackURL, paramMap)
        val response = authService.callback(session, paramMap)
        tracker.infoRESP200(callbackURL, response)
        return ResponseEntity(HttpStatus.OK)
    }
}
