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
@file:Suppress("TooManyFunctions")

package com.eviden.bds.rd.uself.agent.services.openid.conf

import com.eviden.bds.rd.uself.common.models.openid.auth.OpenIdConfiguration
import com.eviden.bds.rd.uself.common.models.openid.issuer.OpenIdCredentialIssuer

interface OpenIdConf {
    fun issuerDID(): String
    fun serverURL(): String
    fun authMetadata(): OpenIdConfiguration
    fun issuerMetadata(): OpenIdCredentialIssuer

    // For supported Credentials
    //   fun init()

    //   fun getMapSupportedVC(): MutableMap<String, CredentialSupported>

    //   fun getListSupportedVC(): ArrayList<CredentialSupported>

    // fun getSupportedCredential(credentialType: String): CredentialSupported

    // fun getAuthorizationDetails(credentialType: String): AuthorizationDetails

    // For supported Presentation Definitions
    // fun getPresentationDefinition(presentationDefinitionId: String): PresentationDefinition

    // fun getFormattedPresentationDefinition(presentationDefinitionId: String): String

    //  fun getRedirectURI(): ArrayList<String>
}
