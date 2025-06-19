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

package com.eviden.bds.rd.uself.agent.models.entities.rclientsession

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import org.springframework.data.redis.core.TimeToLive

@RedisHash("RClientSession")
data class RClientSession(
    @Id
    val id: String,
    @Indexed
    var clientId: String? = null,
    @Indexed
    var nonce: String? = null,
    @Indexed
    var state: String? = null,
    @Indexed
    var cNonce: String? = null,
    @Indexed
    var code: String? = null,

    @Indexed
    val credOfferId: String? = null,
    val credentialOfferResponse: String? = null,

    @Indexed
    var requestID: String? = null,
    var request: String? = null,

    var authorizationDetails: String? = null, //ArrayList<AuthorizationDetails>? = null,
    var codeChallenge: String? = null,
    var userInfo: Map<String, Any>? = null,
    val authRequest: String? = null,
    val tokenResponse: String? = null,
    var redirectURI: String? = null,
    var sessionId: String? = null,
    var presentationDefinition : String? = null,
    var validationRule : String? = null,

    @TimeToLive
    var expiration: Long? = null


)
