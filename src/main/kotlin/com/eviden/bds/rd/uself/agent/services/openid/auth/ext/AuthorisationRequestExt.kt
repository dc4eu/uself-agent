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
@file:Suppress("NoWildcardImports", "UnusedPrivateMember", "NestedBlockDepth", "ForbiddenComment")

package com.eviden.bds.rd.uself.agent.services.openid.auth.ext

import com.eviden.bds.rd.uself.common.models.*
import com.eviden.bds.rd.uself.common.models.openid.auth.AuthorisationRequest
import com.eviden.bds.rd.uself.common.models.openid.auth.PresentationDefinition
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.*
import org.apache.http.client.utils.URIBuilder
import java.time.Instant
import java.util.*

object AuthorisationRequestExt {

    private fun AuthorisationRequest.validateScope() {
        checkNotNull(this.scope)
        check(
            this.scope != SCOPE.OPEN_ID ||
                this.scope != SCOPE.VER_TEST_ID_TOKEN ||
                this.scope != SCOPE.VER_TEST_VP_TOKEN
        ) {
            "Not valid Scope value"
        }
    }

    fun AuthorisationRequest.signAuthRequest(jwtService: JWTSignerService, signerKid: String): SignedJWT {
        val set = JWTClaimsSet.Builder()
            .issuer(signerKid)
            .audience(this.clientId)
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)))
            .claim("iss", signerKid)
            .claim("client_id", this.clientId)
            .claim("response_type", this.responseType)
            .claim("response_mode", this.responseMode)
            .claim("redirect_uri", this.redirectUri)
            .claim("scope", this.scope)
            .claim("nonce", this.nonce)
            .claim("state", this.state)

        this.presentationDefinition?.let {
            val pd = Json.decodeFromString<PresentationDefinition>(it)
            val presDef = Json.encodeToJsonElement(pd).jsonObject.asMap()
            set.claim("presentation_definition", presDef)
        }

        return jwtService.signAndVerify(signerKid, set.build().toJSONObject())
    }

    fun JsonObject.asMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        this.forEach { t, u ->
            when (u) {
                is JsonArray -> {
                    val arrayElements = u.toMutableList()
                    val myList = mutableListOf<Any>()
                    arrayElements.forEach {
                        if (it is JsonObject) {
                            myList.add((it.jsonObject).asMap())
                        } else {
                            myList.add(it.jsonPrimitive.content)
                        }
                    }
                    map[t] = myList
                }

                is JsonObject -> map[t] = u.asMap()
                // is JsonLiteral -> mapOf(t to u)
                JsonNull -> TODO()
                else -> map[t] = u.jsonPrimitive.content
            }
        }
        return map
    }

    fun AuthorisationRequest.getResponse(redirect: Boolean?, uri: String): String {
        return when (redirect) {
            null,true -> this.getAllParameters(uri)
           false -> this.getRequestURIID(uri)
        }
    }

    private fun AuthorisationRequest.getAllParameters(uri: String): String {
        val result = URIBuilder()
            .addParameter("client_id", this.clientId)
            .addParameter("redirect_uri", this.redirectUri)
            .addParameter("response_type", this.responseType)
            .addParameter("response_mode", this.responseMode)
            .addParameter("scope", this.scope)
            .addParameter("state", this.state)
            .addParameter("nonce", this.nonce)
            .addParameter("request_uri", this.requestUri)
        this.request?.let {
            result.addParameter("request", this.request)
        }
        this.presentationDefinition?.let {
            result.addParameter("presentation_definition", this.presentationDefinition)
        }

        if (uri == "https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/redirect") {
            return END_POINT.OPEN_ID + result.build().toASCIIString()
        }
        return uri + result.build().toASCIIString()
    }

    private fun AuthorisationRequest.getRequestURIID(uri: String): String {
//        var uriResponse = uri
//        if (uriResponse == "openid://") {
//            uriResponse = "openid://redirect"
//        }

        val uriResponse = REDIRECT_URI.OPENID
        val result = URIBuilder()
            .addParameter("request_uri", this.requestUri)
            .build()
            .toASCIIString()
        return "${uriResponse}$result"
    }
}
