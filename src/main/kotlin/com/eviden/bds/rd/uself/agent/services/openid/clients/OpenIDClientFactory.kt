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

object OpenIDClientFactory {
    private var clients = mutableMapOf<String, OpenIDClient>()

    init {
        val defaultEndPoint = "https://devtest2-auth.my.evidian.com/pxpadmin/trust_auth/login?client_name=Uself"
        val defaultClientId = "uself_agent_client"
        val defaultClientSecret = "password"
        val defaultRedirectURI = "http://uself-agent-web:4200/present"

        val keycloakEndpoint = "http://keycloak:8080/realms/master/broker/uself/endpoint"
        // val keycloakEndpoint = "http://192.168.137.124:8080/realms/master/broker/uself/endpoint"
        val keycloakClientId = "keycloak-client"
        val keycloakClientSecret = "password"
        val keycloakRedirectURI = "http://uself-agent-web:4200/present"
        // val keycloakRedirectURI = "http://192.168.137.124:4200/present"

        val keycloakProvider = "uself"

        clients = mutableMapOf(
            defaultClientId to DefaultOpenIDClientImp(
                defaultEndPoint,
                defaultClientId,
                defaultClientSecret,
                defaultRedirectURI,
                null
            ),
            keycloakClientId to KeyCloakOpenIDClientImp(
                keycloakEndpoint,
                keycloakClientId,
                keycloakClientSecret,
                keycloakRedirectURI,
                keycloakProvider
            )
        )
    }

    fun getOpenIDClient(clientId: String): OpenIDClient? {
        return clients[clientId]
    }
}
