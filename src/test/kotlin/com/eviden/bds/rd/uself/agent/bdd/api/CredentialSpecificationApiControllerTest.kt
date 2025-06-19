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
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES
import com.eviden.bds.rd.uself.common.models.FORMAT
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.models.credentialSpecification.CredentialSpec
import com.eviden.bds.rd.uself.common.models.openid.issuer.Display
import com.eviden.bds.rd.uself.common.models.openid.issuer.FieldDescription
import com.eviden.bds.rd.uself.common.services.authenticSource.repository.AuthenticSourceRepository
import com.eviden.bds.rd.uself.common.services.credentialPattern.CredPatternType
import com.eviden.bds.rd.uself.common.services.credentialSpecification.repository.CredentialSpecificationRepository
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
class CredentialSpecificationApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val credSpecRepo: CredentialSpecificationRepository,
    @Autowired private val authenticSourceRepository: AuthenticSourceRepository
) : FunSpec({
    val authenticSource = AuthenticSource("keycloak", "")
    authenticSourceRepository.insert(authenticSource.id, Json.encodeToString(authenticSource))

    val getCredentialSpecificationUrl = "/credential-specification/{id}"
    val postCredentialSpecificationUrl = "/credential-specification"
    val getCredentialSpecificationsUrl = "/credential-specification"
    val username = "uself-agent"
    val password = "uself-agent-password"

    val credSpec = CredentialSpec(
        id = CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL,
        format = FORMAT.JWT_VC,
        types = arrayListOf(CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL, CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL),
        display = Display(name = "Customer", description = "Customer Information", locale = "en"),
        credentialSubject = mapOf<String, FieldDescription>(),
        responseURL = "http://localhost:8080/auth/realms/master/protocol/openid-connect/userinfo",
        pattern = CredPatternType.CredentialPatternDefault,
        authenticSourceId = "keycloak"
    )
    val credSpecString = Json.encodeToString(credSpec)

    test("getCredentialSpecificationUrl") {
        credSpecRepo.insert(credSpec.id, credSpecString)
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                getCredentialSpecificationUrl,
                credSpec.id
            ).header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
        }
            .andExpect(content().string(credSpecString))
    }

    test("postCredentialSpecification") {
        mockMvc.perform(
            MockMvcRequestBuilders.post(postCredentialSpecificationUrl)
                .header("Authorization", createBasicToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(credSpecString)
        )
            .andDo { print() }
            .andExpect {
                status().isCreated
            }
    }
    test("getCredentialSpecifications") {
        credSpecRepo.insert(credSpec.id, credSpecString)
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                getCredentialSpecificationsUrl
            ).header("Authorization", createBasicToken(username, password))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.id == '${credSpec.id}' && @.format == '${credSpec.format}')]").exists())
        // .andExpect(jsonPath<JsonArray>("$", hasSize(20)))
    }
    test("updateCredentialSpecification") {
        credSpecRepo.insert(credSpec.id, credSpecString)
        val updatedCredSpec = credSpec.copy(
            format = FORMAT.JWT_VC,
            types = arrayListOf(CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL, CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL),
            display = Display(name = "Customer", description = "Customer Information", locale = "en"),
            credentialSubject = mapOf<String, FieldDescription>(),
            responseURL = "http://localhost:8080/auth/realms/master/protocol/openid-connect/userinfo",
            pattern = CredPatternType.CredentialPatternDefault,
            authenticSourceId = "keycloak"
        )
        val updatedCredSpecString = Json.encodeToString(updatedCredSpec)
        mockMvc.perform(
            MockMvcRequestBuilders.put("$postCredentialSpecificationUrl/${credSpec.id}")
                .header("Authorization", createBasicToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedCredSpecString)
        )
            .andDo { print() }
            .andExpect {
                status().isOk
            }
    }
    test("deleteCredentialSpecification") {
        credSpecRepo.insert(credSpec.id, credSpecString)
        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                "$postCredentialSpecificationUrl/${credSpec.id}"
            ).header("Authorization", createBasicToken(username, password))
        )
            .andDo { print() }
            .andExpect {
                status().isOk
            }
    }
})
