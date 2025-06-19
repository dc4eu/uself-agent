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
@file:Suppress("NoWildcardImports")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.KEYS.createBasicToken
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.services.openIdClients.ClientType
import com.eviden.bds.rd.uself.common.services.openIdClients.repository.ClientRepository
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.Matchers.hasSize
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OpenIdClientApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val openIdClientRepository: ClientRepository
) : FunSpec(
    {
        val getOpenIdClientUrl = "/openid-client/{id}"
        val getOpenIdClientsUrl = "/openid-client"
        val postOpenIdClientUrl = "/openid-client"
        val expectedClients = 3 // 3 clients are added by default
        val username = "uself-agent"
        val password = "uself-agent-password"

        val defaultClientData = OpenIdClientData(
            ClientType.DEFAULT,
            "http://localhost:8080/auth/realms/master",
            "client-id",
            "client-secret",
            "http://localhost:8080/auth/realms/master/protocol/openid-connect/auth",
            "keycloak"
        )

        val defaultClientDataString = Json.encodeToString(defaultClientData)

        test("Get OpenId Client") {
            openIdClientRepository.add(defaultClientData.clientId, defaultClientDataString)
            mockMvc.perform(
                MockMvcRequestBuilders.get(
                    getOpenIdClientUrl,
                    defaultClientData.clientId
                ).header("Authorization", createBasicToken(username, password))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(defaultClientDataString))
        }



        test("Post OpenId Client") {
            mockMvc.perform(
                MockMvcRequestBuilders.post(postOpenIdClientUrl)
                    .header("Authorization", createBasicToken(username, password))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(defaultClientDataString)
            )
                .andDo { print() }
                .andExpect {
                    status().isCreated
                }
        }
        test("Delete OpenId Client") {
            openIdClientRepository.add(defaultClientData.clientId, defaultClientDataString)
            mockMvc.perform(
                MockMvcRequestBuilders.delete(
                    getOpenIdClientUrl,
                    defaultClientData.clientId
                ).header("Authorization", createBasicToken(username, password))
            )
                .andExpect(status().isOk)
        }
        test("Update OpenId Client") {
            openIdClientRepository.add(defaultClientData.clientId, defaultClientDataString)
            val updatedClientData = OpenIdClientData(
                ClientType.DEFAULT,
                "http://localhost:8080/auth/realms/master",
                "new-client-id",
                "client-secret",
                "http://localhost:8080/auth/realms/master/protocol/openid-connect/auth",
                "keycloak"
            )
            val updatedClientDataString = Json.encodeToString(updatedClientData)
            mockMvc.perform(
                MockMvcRequestBuilders.put(getOpenIdClientUrl, defaultClientData.clientId)
                    .header("Authorization", createBasicToken(username, password))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedClientDataString)
            )
                .andExpect(status().isOk)
        }
    }
)
