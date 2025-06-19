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

package com.eviden.bds.rd.uself.agent.services.openid.clients

import com.eviden.bds.rd.uself.common.models.openid.auth.AuthorisationRequest
import org.apache.http.client.utils.URIBuilder

class DefaultOpenIDClientImp(
    override val endPoint: String,
    override val clientId: String,
    override val clientSecret: String,
    override val redirectURI: String,
    override val provider: String?
) : OpenIDClient {
    override fun getClientAuthorize(authReq: AuthorisationRequest): String {
        val accountLinkUrl = URIBuilder(redirectURI)
            .addParameter("nonce", authReq.nonce)
            .addParameter("state", authReq.state)
            .build()
            .toASCIIString()
        return accountLinkUrl
    }
}
