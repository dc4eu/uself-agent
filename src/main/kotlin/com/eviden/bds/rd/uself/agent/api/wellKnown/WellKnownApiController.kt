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

package com.eviden.bds.rd.uself.agent.api.wellKnown

import com.eviden.bds.rd.uself.agent.services.openid.conf.OpenIdConf
import com.eviden.bds.rd.uself.common.models.openid.auth.OpenIdConfiguration
import com.eviden.bds.rd.uself.common.models.openid.issuer.OpenIdCredentialIssuer
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class WellKnownApiController(private val openIdConfig: OpenIdConf) : WellKnownApi {

    private val tracker: Tracker = Tracker(this::class.java)
    private val authURL = " /.well-known/openid-configuration "
    private val issuerURL = " /.well-known/openid-configuration "

    override fun getOpenIdConfiguration(session: HttpSession): ResponseEntity<OpenIdConfiguration> {
        tracker.infoGET(authURL)
        tracker.debug("Session ID: ${session.id}")
        val metadata = openIdConfig.authMetadata()
        tracker.infoRESP200(authURL, metadata)
        return ResponseEntity<OpenIdConfiguration>(metadata, HttpStatus.OK)
    }

    override fun getOpenIdCredentialIssuer(session: HttpSession): ResponseEntity<OpenIdCredentialIssuer> {
        tracker.infoGET(issuerURL)
        tracker.debug("Session ID: ${session.id}")
        val metadata = openIdConfig.issuerMetadata()
        tracker.infoRESP200(issuerURL, metadata)
        return ResponseEntity<OpenIdCredentialIssuer>(metadata, HttpStatus.OK)
    }
}
