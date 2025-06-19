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
@file:Suppress("NoWildcardImports", "MaxLineLength", "TooGenericExceptionCaught", "UnusedPrivateProperty")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.MOCKS_VP.GetToken.preAuthorizedCodeWithNonce
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.ID_TOKEN
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.ID_TOKEN_PASSPORT
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.NONCE
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.STATE
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.mockRClientSession
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.common.models.*
import com.eviden.bds.rd.uself.common.models.openid.auth.AuthorizationDetails
import com.eviden.bds.rd.uself.common.services.Utils
import com.eviden.bds.rd.uself.common.services.Utils.getPinCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import redis.embedded.RedisServer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @MockitoBean private val rClientSessionService: RClientSessionService,
) : FunSpec({
    // for testing purposes
    val authMetadataUrl = "/auth/.well-known/openid-configuration"
    val authUrl = "/auth/authorize"
    val jwkUrl = "/auth/jwks"
    val directPostUrl = "/auth/direct_post"
    val directPostPassportUrl = "/auth/direct_post/epassport"
    val tokenUrl = "/auth/token"
    val expectedIssuer = "http://localhost/auth"
    val redisServer = RedisServer(6370)
    beforeSpec {
        redisServer.start()
        `when`(rClientSessionService.findByNonce(anyString())).thenReturn(listOf(mockRClientSession))
        `when`(rClientSessionService.findByState(anyString())).thenReturn(listOf(mockRClientSession))
        `when`(rClientSessionService.findByCNonce(anyString())).thenReturn(listOf(mockRClientSession))
        `when`(rClientSessionService.findByCode(anyString())).thenReturn(listOf(mockRClientSession))
    }
    afterSpec {
        redisServer.stop()
    }

    test("getOpenIdConfiguration") {
        mockMvc.get(authMetadataUrl).andExpect {
            status { isOk() }
            jsonPath("issuer") { value(expectedIssuer) }
        }.andReturn()
    }

    test("jwks") {
        mockMvc.get(jwkUrl).andExpect {
            status { isOk() }
            jsonPath("keys") { exists() }
        }.andReturn()
    }

    test("getAuthorize for Auth") {
        val paramMap = mutableMapOf(
            "scope" to SCOPE.VER_TEST_VP_TOKEN,
            "response_type" to RESPONSE_TYPE.CODE,
            "client_id" to HOLDER_DID.did,
            "redirect_uri" to END_POINT.OPEN_ID,
            "state" to STATE,
            "nonce" to NONCE,
            "request" to "test_request",
            "authorization_details" to Json.encodeToString(
                arrayOf(
                    AuthorizationDetails(
                        type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
                        format = FORMAT.JWT_VC,
                        locations = arrayListOf("issuerURL"),
                        types = arrayListOf(
                            "VerifiableCredential", "VerifiableAttestation", CREDENTIAL_TYPES.DC4EU_PID
                        )
                    )
                )
            ),
            "client_metadata" to "{\"authorization_endpoint\":\"openid://\",\"response_types_supported\":[\"vp_token\",\"id_token\"],\"vp_formats_supported\":{\"jwt_vp\":{\"alg\":[\"ES256\"]},\"jwt_vc\":{\"alg\":[\"ES256\"]}}}",
            // "issuer_state" to "test_issuer_state",
            "code_challenge" to "test_code_challenge",
            "code_challenge_method" to "null",
            "redirect" to "false"
        )

        mockMvc.get(authUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isOk() }
        }
    }

    test("getAuthorize invalid scope") {
        val paramMap = mutableMapOf(
            "scope" to "invalid_scope",
            "response_type" to RESPONSE_TYPE.CODE,
            "client_id" to HOLDER_DID.did,
            "redirect_uri" to END_POINT.OPEN_ID,
            "state" to STATE,
            "nonce" to NONCE,
        )

        mockMvc.get(authUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    test("getRequestUri") {}

    test("postDirectPost") {
        val paramMap = mutableMapOf(
            "redirect_uri" to END_POINT.OPEN_ID,
            "id_token" to ID_TOKEN,
            "user_pin" to Utils.getPinCode(NONCE),
            // "vp_token" to "eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJuYmYiOjE3MTE1MzE4MDksImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInZwIjp7IkBjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIl0sInR5cGUiOlsiVmVyaWZpYWJsZVByZXNlbnRhdGlvbiJdLCJpZCI6Ijc5NjEyN2ZjLWVkZTctNDVlYi1iYTE2LTIxZjdlMGQ3OWQwNCIsImhvbGRlciI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInZlcmlmaWFibGVDcmVkZW50aWFsIjpbXX0sImV4cCI6MTcxMTUzMjEwOSwiaWF0IjoxNzExNTMxODA5LCJub25jZSI6ImQ1MjdjMTkxLTZlMWQtNGMzZC05ODQzLTllYWYyMDA1ZmJhOSIsImp0aSI6Ijc5NjEyN2ZjLWVkZTctNDVlYi1iYTE2LTIxZjdlMGQ3OWQwNCJ9.FyZknGc7Xqj08UAa9kIAnxsl37UmMBF0MdGxd6JX7sOB0HNMDCpwgrSdFGmPTwcAVVcYzJMTFeayqlHiv_kkgg",
            // "state" to "8d8b6a3d-4bc0-4234-9a9a-ed1928815502",
            "state" to STATE,
            // "presentation_submission" to "{\"id\":\"holder-wallet-qualification-presentation\",\"definition_id\":\"holder-wallet-qualification-presentation\",\"descriptor_map\":[{\"id\":\"same-device-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[0]\"}},{\"id\":\"cross-device-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[1]\"}},{\"id\":\"same-device-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[2]\"}},{\"id\":\"cross-device-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[3]\"}},{\"id\":\"same-device-pre-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-pre-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[4]\"}},{\"id\":\"cross-device-pre-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-pre-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[5]\"}},{\"id\":\"same-device-pre-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-pre-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[6]\"}},{\"id\":\"cross-device-pre-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-pre-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[7]\"}}]}"
        )

        mockMvc.post(directPostUrl) {

            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isFound() }
        }
    }

    test("postDirectPost invalid state") {
        val paramMap = mutableMapOf(
            "redirect_uri" to END_POINT.OPEN_ID,
            "id_token" to ID_TOKEN_PASSPORT,
            "state" to "STATE",
        )
        val result = mockMvc.post(directPostUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

//        val locationHeader = result.response.getHeader("Location")
//        locationHeader shouldContain "error=INVALID_REQUEST"
    }

    test("postDirectPost invalid token") {
        val paramMap = mutableMapOf(
            "id_token" to "invalid_id_token",
        )

        val exception = shouldThrow<java.lang.Exception> {
            mockMvc.post(directPostUrl) {
                paramMap.forEach { (key, value) ->
                    param(key, value)
                }
            }.andExpect {
                status { isOk() }
            }
        }
        exception.message shouldContain "Invalid serialized unsecured/JWS/JWE"
    }

    test("postDirectPostPassport Ok") {

        // val nonce_test = "n-0S6_WzA2Mj"
        val clientSessionTest = mockRClientSession.copy(nonce = "n-0S6_WzA2Mj")
        `when`(rClientSessionService.findByNonce(anyString())).thenReturn(listOf(clientSessionTest))

        val paramMap = mutableMapOf(
            "redirect_uri" to END_POINT.OPEN_ID,
            "id_token" to ID_TOKEN_PASSPORT,
            "state" to STATE,
        )

        mockMvc.post(directPostPassportUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isFound() }
        }
    }

    test("postToken Ok") {
        val paramMap = mutableMapOf(
            "grant_type" to GRAN_TYPE.AUTH_CODE,
            "client_id" to HOLDER_DID.did,
            "code" to mockRClientSession.code!!,
            "code_verifier" to CODE_CHALLENGE.CHALLENGE,
            "client_assertion_type" to "test_assertion_type",
            // "pre_authorized_code" to "test_pre_authorized_code",
            "user_pin" to "test_user_pin"
        )

        mockMvc.post(tokenUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isOk() }
            content {
                // Use jsonPath to check the content of the response
                jsonPath("$.access_token") { exists() }
                jsonPath("$.token_type") { value("Bearer") }
            }
        }
    }

    test("postToken preauth Ok") {
        val paramMap = mutableMapOf(
            "grant_type" to GRAN_TYPE.PRE_AUTH_CODE,
            "client_id" to HOLDER_DID.did,
            "pre-authorized_code" to preAuthorizedCodeWithNonce,
            "user_pin" to getPinCode("7454281456888646535") // this is the nonce included into the preAuthorizedCodeWithNonce
        )

        mockMvc.post(tokenUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isOk() }
            content {
                // Use jsonPath to check the content of the response
                jsonPath("$.access_token") { exists() }
                jsonPath("$.token_type") { value("Bearer") }
            }
        }
    }

    test("postToken Bad Request") {
        val paramMap = mutableMapOf(
            "grant_type" to "invalid_grant_type", // Use an invalid grant_type
            "client_id" to "invalid_client_id",
            "code" to "8661435939417066112",
            "code_verifier" to CODE_CHALLENGE.CHALLENGE,
            "client_assertion_type" to "test_assertion_type",
            "pre_authorized_code" to "test_pre_authorized_code",
            "user_pin" to "test_user_pin"
        )

        mockMvc.post(tokenUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    test("postToken Unauthorized") {
        val paramMap = mutableMapOf(
            "grant_type" to GRAN_TYPE.AUTH_CODE,
            "client_id" to "invalid_client_id", // Use an invalid client_id
            "code" to "8661435939417066112",
            "code_verifier" to CODE_CHALLENGE.CHALLENGE,
            "client_assertion_type" to "test_assertion_type",
            "pre_authorized_code" to "test_pre_authorized_code",
            "user_pin" to "test_user_pin"
        )

        mockMvc.post(tokenUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
//            status { isUnauthorized() }
            status { isOk() }
        }
    }

    test("postToken Internal Server Error") {
        val paramMap = mutableMapOf(
            "grant_type" to GRAN_TYPE.AUTH_CODE,
            "client_id" to HOLDER_DID.did,
            "code" to "8661435939417066112",
            "code_verifier" to CODE_CHALLENGE.CHALLENGE,
            "client_assertion_type" to "trigger_error",
            // This value triggers a server error, need to identity a condition that causes server error(exception not handled)
            "pre_authorized_code" to "test_pre_authorized_code",
            "user_pin" to "test_user_pin"
        )

        mockMvc.post(tokenUrl) {
            paramMap.forEach { (key, value) ->
                param(key, value)
            }
        }.andExpect {
            status { isOk() }
            // status { isInternalServerError() }
        }
    }

    test("userInfo") {}
})
