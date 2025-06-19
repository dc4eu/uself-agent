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
@file:Suppress("NoWildcardImports", "MaxLineLength", "TooGenericExceptionCaught")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.CODE_CHALLENGE_TEST
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.mockRClientSession
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import io.kotest.core.spec.style.FunSpec
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import redis.embedded.RedisServer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WellKnownControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @MockitoBean private val rClientSessionService: RClientSessionService
) : FunSpec(
    {

        val authMetadataUrl = "/.well-known/openid-configuration"
        val expectedIssuer = "http://localhost/auth"
        val expectedCredentialIssuer = "http://localhost/issuer"
        val issMetadataUrl = "/.well-known/openid-credential-issuer"
        val redisServer = RedisServer(6370)

        beforeSpec {
            redisServer.start()
            mockRClientSession.codeChallenge = CODE_CHALLENGE_TEST
            `when`(rClientSessionService.findByClientId(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByNonce(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByState(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByCNonce(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByCode(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByCredOfferId(anyString())).thenReturn(listOf(mockRClientSession))
            `when`(rClientSessionService.findByRequestID(anyString())).thenReturn(listOf(mockRClientSession))
        }
        afterSpec {
            redisServer.stop()
        }

        test("getOpenIdConfigurationAuthenticator") {
            mockMvc.get(authMetadataUrl).andExpect {
                status { isOk() }
                jsonPath("issuer") { value(expectedIssuer) }
            }.andReturn()
        }

        test("getOpenIdConfigurationIssuer") {
            mockMvc.get(issMetadataUrl).andExpect {
                status { isOk() }
                jsonPath("credential_issuer") { value(expectedCredentialIssuer) }
            }.andReturn()
        }
    }
)
