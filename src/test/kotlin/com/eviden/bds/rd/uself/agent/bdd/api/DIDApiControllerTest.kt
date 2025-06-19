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

@file:Suppress("MaxLineLength", "NoWildcardImports")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.TestsHelper
import com.eviden.bds.rd.uself.common.services.did.method.DIDMethodType
import com.eviden.bds.rd.uself.common.services.did.repository.DIDDocRepository
import com.eviden.bds.rd.uself.common.services.tm.ebsi.didRegistry.client.DIDRegistryClient
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import foundation.identity.did.DIDDocument
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.koin.core.context.*
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DIDApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    private val didDocRepository: DIDDocRepository
) : KoinTest, FunSpec({

    val didGenerateUrl = "/did/generate-DIDDoc"
    val didResolveUrl = "/did/resolve-DID"
    val allDIDsUrl = "/did/getAll"

    beforeSpec {

        val didRegistryClientMock: DIDRegistryClient = mockk<DIDRegistryClient>().apply {
            every { getDIDDocument(any()) } answers {
                val did = firstArg<String>()
                val didDoc = when {
                    did.startsWith("did:ebsi:z") -> TestsHelper.didDocEBSI
                    did.startsWith("did:web:") -> TestsHelper.didDocWeb
                    else -> throw IllegalArgumentException("DID Method not supported in DIDRegistryClientImp")
                }
                didDocRepository.delete(did)
                DIDDocument.fromJson(didDoc)
            }
        }
        val testModule = module {
            single<DIDRegistryClient> { didRegistryClientMock }
        }
        loadKoinModules(testModule)
    }

    test("generateDIDDocJwk") {

        val did = "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9"
        val type = DIDMethodType.DID_JWK

        val mockMvcResponse = mockMvc.get(didGenerateUrl) {
            param("did", did)
            param("type", type.name)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        DIDDocument.fromJson(mockMvcResponse.response.contentAsString) shouldBe DIDDocument.fromJson(TestsHelper.didDocJwk)
    }

    test("generateDIDDocKeyP256") {

        val did = "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP"
        val type = DIDMethodType.DID_KEY_P256

        val mockMvcResponse = mockMvc.get(didGenerateUrl) {
            param("did", did)
            param("type", type.name)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        DIDDocument.fromJson(mockMvcResponse.response.contentAsString) shouldBe DIDDocument.fromJson(TestsHelper.didDocKeyP256)
    }

    test("resolveDID") {

        val did = "did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW"

        val mockMvcResponse = mockMvc.get(didResolveUrl) {
            param("did", did)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        DIDDocument.fromJson(mockMvcResponse.response.contentAsString) shouldBe DIDDocument.fromJson(TestsHelper.didDocKey)
    }

    test("resolve didWeb not found in repository") {

        val did = "did:web:w3c-ccg.gitlab.io:user:alice"
        val mockMvcResponse = mockMvc.get(didResolveUrl) {
            param("did", did)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        // DIDDocument.fromJson(mockMvcResponse.response.contentAsString) shouldBe DIDDocument.fromJson(TestsHelper.didDocWeb)
    }

    test("resolve didEbsi not found in repository") {
        val did = "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N"
        val mockMvcResponse = mockMvc.get(didResolveUrl) {
            param("did", did)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        // DIDDocument.fromJson(mockMvcResponse.response.contentAsString) shouldBe DIDDocument.fromJson(TestsHelper.didDocEBSI)
    }

    test("getAll") {

        val mockMvcResponse = mockMvc.get(allDIDsUrl).andExpect {
            status { isOk() }
        }.andReturn()

        val responseListDidDoc: ArrayList<DIDDocument> = ArrayList()
        val objectMapper = ObjectMapper()
        val responseList: List<String> = objectMapper.readValue(
            mockMvcResponse.response.contentAsString,
            object : TypeReference<List<String>>() {}
        )
        for (response in responseList) {
            responseListDidDoc.add(objectMapper.readValue(response, DIDDocument::class.java))
        }

        responseListDidDoc shouldBe didDocRepository.findAll().map { DIDDocument.fromJson(it) }
    }

    test("did.json") {

        val didDocResponse = mockMvc.get(didGenerateUrl) {
            param("type", DIDMethodType.DID_WEB.name)
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val didId = DIDDocument.fromJson(didDocResponse).id.toString().substringAfter("did:web:localhost:did:")
        val didJsonUrl = "/did/$didId/did.json"
        val mockMvcResponse = mockMvc.get(didJsonUrl).andExpect {
            status { isOk() }
        }.andReturn()

        mockMvcResponse.response.contentAsString shouldBe didDocResponse
    }
})
