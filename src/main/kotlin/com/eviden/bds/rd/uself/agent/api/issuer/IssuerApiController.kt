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

@file:Suppress("LoggingSimilarMessage", "NoWildcardImports")

package com.eviden.bds.rd.uself.agent.api.issuer

import com.eviden.bds.rd.uself.agent.services.openid.conf.OpenIdConf
import com.eviden.bds.rd.uself.agent.services.openid.issuer.IssuerService
import com.eviden.bds.rd.uself.common.models.openid.issuer.*
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class IssuerApiController(private val openIdConfig: OpenIdConf, private val issuerService: IssuerService) : IssuerApi {
    private val metadataURL = "/issuer/.well-known/openid-credential-issuer"
    private val initURL = "/issuer/initiate-credential-offer"
    private val credURL = "/issuer/credential "

    private val tracker = Tracker(this::class.java)

    override fun getOpenIdCredentialIssuer(session: HttpSession): ResponseEntity<OpenIdCredentialIssuer> {
        tracker.infoGET(metadataURL)

        val metadata = openIdConfig.issuerMetadata()
        tracker.infoRESP200(metadataURL, metadata)
        return ResponseEntity<OpenIdCredentialIssuer>(metadata, HttpStatus.OK)
    }

    override fun getInitiate(
        session: HttpSession,
        @RequestHeader("Authorization") bearerToken: String?,
        @RequestParam(value = "deferred", required = false) deferred: Boolean?,
        @RequestParam(value = "redirect", required = false) redirect: Boolean?,
        @RequestParam(value = "pre-authorized", required = false) preAuthorized: Boolean?,
        @RequestParam(value = "nonce", required = false) nonce: String?,
        @RequestParam(value = "credential_type", required = true) credentialType: String,
        @RequestParam(value = "client_id", required = false) clientId: String?,
        @RequestParam(value = "credential_offer_endpoint", required = true) credentialOfferEndpoint: String
    ): ResponseEntity<Any> {
        val initiateRequest = CredentialOfferRequest(
            nonce = nonce,
            redirect = redirect,
            credentialType = credentialType,
            clientId = clientId,
            credentialOfferEndpoint = credentialOfferEndpoint,
            bearerToken = bearerToken,
            deferred = deferred,
            preAuthorized = preAuthorized
        )
        tracker.infoGET(initURL, initiateRequest)
        tracker.debug("Session ID: ${session.id}")

        val response = issuerService.initiate(session, initiateRequest)
        val result = "${initiateRequest.credentialOfferEndpoint}$response"
        tracker.infoRESP200(initURL, result)

        return when (initiateRequest.credentialType.contains("cross")) {
            true -> ResponseEntity<Any>("${initiateRequest.credentialOfferEndpoint}$response", HttpStatus.OK)
            false -> {
                if (!initiateRequest.redirect!!) {
                    ResponseEntity<Any>(result, HttpStatus.OK)
                } else {
                    ResponseEntity.status(HttpStatus.FOUND).location(
                        URI.create(result)
                    ).build()
                }
            }
        }

    }

    override fun getCredentialOfferByID(
        session: HttpSession,
        @PathVariable(
            "credentialOfferId"
        ) credentialOfferId: String
    ): ResponseEntity<CredentialOfferResponse> {
        val offer = issuerService.getCredentialOfferByID(session, credentialOfferId)
        tracker.infoRESP200("$credURL-offer/$credentialOfferId", offer)
        return ResponseEntity<CredentialOfferResponse>(offer, HttpStatus.CREATED)
    }

    override fun postCredential(
        session: HttpSession,
        @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: CredentialRequest
    ): ResponseEntity<CredentialResponse> {
        body.bearerToken = bearerToken.removePrefix("Bearer ")
        tracker.infoPOST(credURL, body)
        tracker.debug("Session ID: ${session.id}")

        val credential = issuerService.postCredential(session, body)
        tracker.infoRESP200(credURL, credential)

        return ResponseEntity<CredentialResponse>(credential, HttpStatus.OK)
    }

    override fun postCredentialDeferred(): ResponseEntity<DeferredCredentialResponse> {
        // TODO
        return ResponseEntity<DeferredCredentialResponse>(HttpStatus.NOT_IMPLEMENTED)
    }
}
