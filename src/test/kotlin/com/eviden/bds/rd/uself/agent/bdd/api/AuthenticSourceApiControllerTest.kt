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

@file:Suppress("NoWildcardImports", "MaxLineLength")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.KEYS.createBasicToken
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.services.authenticSource.repository.AuthenticSourceRepository
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticSourceApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val authenticSourceRepository: AuthenticSourceRepository
) : FunSpec({

    val getAuthenticSourceUrl = "/authentic-source/{id}"
    val postAuthenticSourceUrl = "/authentic-source"
    val getAuthenticSourcesUrl = "/authentic-source"
    val authSource = AuthenticSource("id", "requestUserInfoUrl")
    val authSourceString = Json.encodeToString(authSource)
    val username = "uself-agent"
    val password = "uself-agent-password"

    test("getAuthenticSourceUrl") {
        authenticSourceRepository.insert(authSource.id, authSourceString)
        mockMvc.perform(
            MockMvcRequestBuilders.get(getAuthenticSourceUrl, authSource.id)
                .header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
        }
        //  .andExpect(content().json("{}", false))
    }

    test("postAuthenticSource") {
        mockMvc.perform(
            MockMvcRequestBuilders.post(postAuthenticSourceUrl)
                .header("Authorization", createBasicToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(authSourceString)
        )
            .andDo { print() }
            .andExpect {
                status().isCreated
            }
    }
    test("getAuthenticSources") {
        authenticSourceRepository.insert(authSource.id, authSourceString)
//        val expectedAuthenticSources = """
//           [{"id":"uself-authentic-source","requestURL":"http://uself-authentic-source","openIdClientId":"uself-agent-client"},{"id":"tango","requestURL":"http://localhost/auth/userinfo"},{"id":"id","requestURL":"requestUserInfoUrl"}]
//        """.trimIndent()
        mockMvc.perform(
            MockMvcRequestBuilders.get(getAuthenticSourcesUrl)
                .header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
            jsonPath("$[?(@.id == 'id' && @.requestURL == 'requestUserInfoUrl')]").exists()
        } // .andExpect(content().string(expectedAuthenticSources))
    }

    test("updateAuthenticSource") {
        authenticSourceRepository.insert(authSource.id, authSourceString)
        val updatedAuthSource = AuthenticSource("id", "updatedRequestUserInfoUrl")
        val updatedAuthSourceString = Json.encodeToString(updatedAuthSource)
        mockMvc.perform(
            MockMvcRequestBuilders.put(postAuthenticSourceUrl)
                .header("Authorization", createBasicToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedAuthSourceString)
        )
            .andDo { print() }
            .andExpect {
                status().isOk
            }
    }

    test("deleteAuthenticSource") {
        authenticSourceRepository.insert(authSource.id, authSourceString)
        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                getAuthenticSourceUrl,
                authSource.id
            ).header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
        }
    }
})
