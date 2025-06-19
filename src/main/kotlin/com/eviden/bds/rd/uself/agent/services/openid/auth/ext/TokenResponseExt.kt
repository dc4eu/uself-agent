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
@file:Suppress("NestedBlockDepth")

package com.eviden.bds.rd.uself.agent.services.openid.auth.ext

import com.eviden.bds.rd.uself.common.models.DEFAULT_EXP_SECONDS
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenResponse
import com.eviden.bds.rd.uself.common.services.Utils.toMap
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService
import com.nimbusds.jwt.JWTClaimsSet
import java.time.Instant
import java.util.*

object TokenResponseExt {

    fun TokenResponse.signIdToken(jwtService: JWTSignerService, signerKid: String, userInfo: Map<String, Any>?) {
        val set = JWTClaimsSet.Builder()
            .issuer(this.issuer)
            .subject(this.clientId)
            .audience(this.clientId)
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)))
            .claim("nonce", this.cNonce)
            .claim("user_info", userInfo)

        this.idToken = jwtService.signAndVerify(signerKid, set.build().toJSONObject()).serialize()
    }

    fun TokenResponse.signAccessToken(
        jwtService: JWTSignerService,
        signerKid: String
    ) {
        val set = JWTClaimsSet.Builder()
            .issuer(this.issuer)
            .audience(this.audience)
            .subject(this.clientId)
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)))
            .claim("nonce", this.cNonce)
            .claim("claims", toMap(this.claims!!))
            .build()
        this.accessToken = jwtService.signAndVerify(signerKid, set.toJSONObject()).serialize()
    }
}
