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

package com.eviden.bds.rd.uself.agent.bdd.services

import com.eviden.bds.rd.uself.agent.AgentApplication
import com.eviden.bds.rd.uself.agent.bdd.services.PreAuthTestHelper.authenticSourceResponse
import com.eviden.bds.rd.uself.agent.bdd.services.PreAuthTestHelper.claims
import com.eviden.bds.rd.uself.agent.bdd.services.PreAuthTestHelper.mockAccessTokenResponse
import com.eviden.bds.rd.uself.agent.mocks.*
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.claimsTI
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.proxiesString
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.tirString
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateCredentialOfferResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getCredentialResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getIssueCredentialRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.jwtClaimsSetConverter
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.signJWT
import com.eviden.bds.rd.uself.agent.services.openid.auth.AuthService
import com.eviden.bds.rd.uself.agent.services.openid.issuer.IssuerService
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.DC4EU_PID
import com.eviden.bds.rd.uself.common.models.HOLDER_DID
import com.eviden.bds.rd.uself.common.models.ISSUER_TIR
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenRequest
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenResponse
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialOfferRequest
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.Utils.getPinCode
import com.eviden.bds.rd.uself.common.services.authenticSource.AuthenticSourceService
import com.eviden.bds.rd.uself.common.services.openIdClients.ClientType
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.Row7
import io.kotest.data.forAll
import io.kotest.matchers.shouldNotBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest
import org.koin.test.inject
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import redis.embedded.RedisServer

@TestPropertySource(
    properties = [
        "uself.security.basic.username=uself-agent",
        "uself.security.basic.password=uself-agent-password",
        "uself.schemaExceptions=123",
        "uself.loadDir=data/",
        "uself.version=1.0.0",
        "uself.did.method=DID_EBSI_LEGAL_ENTITY_PRELOADED",
        "uself.tir.id=",
        "uself.tir.sigkey=",
        "uself.tir.ethkey=",
        "uself.preload=dc4eu",

        "uself.server=https://tadpole-internal-mammal.ngrok-free.app",
        "uself.ebsiApis.didRegURL=http://localhost:9998",
        "uself.ebsiApis.trustedIssuersURL=http://localhost:9997",
        "uself.authentic-source-url=http://localhost:9999",
        "uself.openid-idp-server=http://localhost:8080/realms/master/broker/uself/endpoint",
        "uself.openid-client-id=uself-agent-client",
        "uself.openid-client-secret=password",
        "uself.openid-client-gui=http://uself-agent-web:4200/present",
        "uself.openid-client-provider=uself",
        "uself.openid-client-token=http://keycloak:8080/realms/master/broker/uself/token",
        "master.key.kms=",
        "uself.ebsiApis.ledgerURL=https://api-conformance.ebsi.eu/ledger/v4/blockchains/besu",
        "uself.ebsiApis.identifiersURL=https://api-conformance.ebsi.eu/did-registry/v5/identifiers",
        "uself.ebsiApis.issuerURL=https://api-conformance.ebsi.eu/conformance/v3/issuer-mock",
        "uself.ebsiApis.authURL=https://api-conformance.ebsi.eu/conformance/v3/auth-mock",
        "uself.ebsiApis.authzURL=https://api-conformance.ebsi.eu/authorisation/v4",
        "uself.ebsiApis.presentationDefinitionURL=https://api-conformance.ebsi.eu/authorisation/v4/presentation-definitions",
        "uself.ebsiApis.tokenURL=https://api-conformance.ebsi.eu/authorisation/v4/token",
        "uself.ebsiApis.initiateURL=https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/initiate-credential-offer",
        "spring.data.redis.port=6370"
    ]
)
@ContextConfiguration(classes = [AgentApplication::class, TestWebSecurityConfig::class])
class OIDC4VCIPreAuthTest(
    private val authService: AuthService,
    private val issuerService: IssuerService,
    private val rClientSessionService: RClientSessionService,
) : KoinTest, BehaviorSpec() {

    private val openIdClientService: OpenIdClientService by inject<OpenIdClientService>()
    private val authenticSourceService: AuthenticSourceService by inject<AuthenticSourceService>()

    private lateinit var sessionMock: MockHttpSession
    private lateinit var mockAuthSourceServer: MockWebServer
    private lateinit var mockKeyCloakServer: MockWebServer
    private lateinit var mockDIDRegServer: MockWebServer
    private lateinit var mockTRServer: MockWebServer
    private val redisServer = RedisServer(6370)

    private val jwtTI = signJWT(ISSUER_TIR.sigKey, ISSUER_TIR.did, jwtClaimsSetConverter(claimsTI))

    private val mockTIResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(tirString(jwtTI, null, null).first!!)
    private val mockTIProxyResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(proxiesString())
    private val mockDIDRegResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(ISSUER_TIR.didDoc)
    private val mockTokenResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(mockAccessTokenResponse)
    private val mockUserInfoResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(authenticSourceResponse)

    override suspend fun beforeSpec(spec: Spec) {
        redisServer.start()
        sessionMock = MockHttpSession()

        // set the openIdClients
        val openIdClient = OpenIdClientData(
            type = ClientType.KEYCLOAK,
            endpoint = "http://localhost:8080",
            clientId = "uself-issuer-agent-client",
            clientSecret = "G0G9t0fCYk8delDoMgDTi65sbtkGgap5",
            redirectURI = "http://uself-agent-web:4200/present",
            provider = "uself"
        )
        openIdClientService.createOpenIdClient(openIdClient)
        // set the Authntic Source
        val authenticSource = AuthenticSource(
            id = "uself-authentic-source",
            requestURL = "http://localhost:9999",
            openIdClientId = "uself-issuer-agent-client"
        )
        authenticSourceService.postAuthenticSource(authenticSource)

        mockAuthSourceServer = MockWebServer()
        mockAuthSourceServer.start(9999)

        mockKeyCloakServer = MockWebServer()
        mockKeyCloakServer.start(8080)

        mockDIDRegServer = MockWebServer()
        mockDIDRegServer.start(9998)
        mockDIDRegServer.enqueue(mockDIDRegResponse)

        mockTRServer = MockWebServer()
        mockTRServer.start(9997)
        mockTRServer.enqueue(mockTIResponse)
        mockTRServer.enqueue(mockTIProxyResponse)
    }

    override suspend fun afterSpec(spec: Spec) {
        mockAuthSourceServer.shutdown()
        mockKeyCloakServer.shutdown()
        mockDIDRegServer.shutdown()
        mockTRServer.shutdown()
        redisServer.stop()
    }

    init {
        Context("Obtaining a PID with a pre-authenticated OIDC") {
            Given("A pre-authenticated OIDC after login in a Keycloak server and with a access_token") {
                forAll(
                    Row7(
                        DC4EU_PID,
                        createUUID(),
                        createUUID(),
                        ISSUER_TIR.did,
                        ISSUER_TIR.sigKey,
                        HOLDER_DID.did,
                        HOLDER_DID.key,
                    )
                ) { credentialType, nonce, cnonce, didIssuer, keyIssuer, didHolder, keyHolder ->

                    val endUserBearerToken = """
        eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJib3pCbU1NRGR6YVZRVDEyekQtYmJ2aEczWTZOdUNWdGN0Vi01WDdwNmFVIn0.eyJleHAiOjE3Mzg1NzE5NTAsImlhdCI6MTczODU3MTY1MCwiYXV0aF90aW1lIjoxNzM4NTY4NTI5LCJqdGkiOiJjNDEzYjU1MS05ZjQ4LTRiOTUtYTkwNS1lZjJiMTliZjk4MjEiLCJpc3MiOiJodHRwOi8vdXNlbGYta2V5Y2xvYWsubG9jYWwvcmVhbG1zL3VzZWxmLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImQ1ODRjY2U2LTZhMGYtNDVlZi05ODYxLTRhMWFiY2E4MTE2YyIsInR5cCI6IkJlYXJlciIsImF6cCI6InVzZWxmLWlzc3Vlci1ndWkiLCJub25jZSI6IlRXUjJXR2x5ZEZCMlNVOUdXVzFVVW1SNGNtUi1VMzVWZUd4RE0wWkhiVzFHVEVkblIzSlBkVWMxVWw5RiIsInNlc3Npb25fc3RhdGUiOiI1NWU3Nzk4My00MWU5LTRmYTktODQzYy03ZDA0N2E5N2I4MTciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly91c2VsZi1pc3N1ZXItZ3VpLmxvY2FsIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11c2VsZi1yZWFsbSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJzaWQiOiI1NWU3Nzk4My00MWU5LTRmYTktODQzYy03ZDA0N2E5N2I4MTciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZG9jdW1lbnRfbnVtYmVyIjoiMTIzNDU2Nzg5IiwibmFtZSI6InVzZWxmIGFkbWluIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlbGYtYWRtaW4iLCJnaXZlbl9uYW1lIjoidXNlbGYiLCJmYW1pbHlfbmFtZSI6ImFkbWluIiwiZW1haWwiOiJhZG1pbkB0ZXN0LmNvbSJ9.C-DHYpncTu4e4xgQ_wROXzn01AXfr4nhe1x-iUT5KJZ5rQKiQY_DwXg2Oew_IaOeuSjeKqA3Juqu18J1VUq6cr5AeDlEl9SOl8yXz0ok2uzvYXtvKTCvxNhIOzoe4faDHAnJUdDorSX76CfnEPZIjgiC2TgqW0t94TQp0C94zVVCN8dFoFiRy0-YyiygHpGIG8Cyurnjp3T0tgrRhiZ7OZzj58yTCiwc-wEIPZ7ytf1bBK4jx-OAYUHJMXqQEbBf3XhKKeyqOn32K-MZ2EYBsEjsmaIme1gmbf2w3djlccB6VDzsQ2cic4JfRDEFVrMz_LrUtzUznNLz47M49FO92w
                    """.trimIndent()
                    lateinit var tokenResponse: TokenResponse
                    When("The user requests a PID: request the credential offer") {
                        val initiateRequest = CredentialOfferRequest(
                            bearerToken = endUserBearerToken,
                            credentialType = credentialType,
                            nonce = nonce,
                            redirect = false
                        )
                        val initiateCredentialResponse = issuerService.initiate(sessionMock, initiateRequest)
                        Then("The the credential offer uri and the identifier associated are is returned ") {
                            initiateCredentialResponse shouldNotBe null
                        }

                        When("Using the uri obtained before to request the credential offer") {
                            val id = initiateCredentialResponse.toString().split("%2F")?.last()
                            val credentialOfferResponse = issuerService.getCredentialOfferByID(sessionMock, id!!)
                            Then("The the credential offer is returned and validated") {
                                val credentialOfferResponseExpected = generateCredentialOfferResponseExpected(
                                    credentialType,
                                    nonce,
                                    keyIssuer,
                                    didIssuer,
                                    preAuth = true
                                )
                                credentialOfferResponse shouldBeCredentialOffer credentialOfferResponseExpected
                            }
                            When("With the credential offer the wallet send a  Token Request") {
                                val postTokenRequest = TokenRequest(
                                    grantType = "urn:ietf:params:oauth:grant-type:pre-authorized_code",
                                    redirectUri = "https://www.izertis.com",
                                    userPin = getPinCode(nonce),
                                    preAuthorizedCode = credentialOfferResponse.grants?.preAuthorizedCode?.preAuthorizedCode!!
                                )
                                tokenResponse = authService.postToken(sessionMock, postTokenRequest)
                                Then("The the credential offer is returned and validated") {
                                    tokenResponse shouldBeTokenResponse TokenResponse(
                                        tokenType = "Bearer",
                                        expiresIn = 200
                                    )
                                }
                                When("With the credential offer the wallet send a  Token Request") {
                                    // change the cNonce to fix the test data request
                                    val session = rClientSessionService.findByNonce(nonce).first()
                                    session.cNonce = cnonce
                                    rClientSessionService.saveSession(session)

                                    mockKeyCloakServer.enqueue(mockTokenResponse)
                                    mockAuthSourceServer.enqueue(mockUserInfoResponse)
                                    val credentialRequest = getIssueCredentialRequest(
                                        credentialType,
                                        cnonce,
                                        didHolder,
                                        keyHolder,
                                    )
                                    credentialRequest.bearerToken = tokenResponse.accessToken
                                    val issueCredentialResponse = issuerService.postCredential(
                                        sessionMock,
                                        credentialRequest
                                    )

                                    Then("The credential offer is returned and validated") {
                                        val jsonObject = JSONObject(claims)
                                        val mutableMap: Map<String, Any?> = jsonObject.toMap()
                                        val credentialResponseExpected = getCredentialResponseExpected(
                                            credentialType,
                                            didIssuer,
                                            didHolder,
                                            keyIssuer,
                                            mutableMap
                                        )
                                        issueCredentialResponse shouldBeCredentialResponse credentialResponseExpected
                                        issueCredentialResponse.shouldHaveValidCredentialStatus()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Context("Obtaining a PID with a pre-authenticated OIDC and wrong PIN Code") {
            Given("A pre-authenticated OIDC after login in a Keycloak server and with a access_token") {
                forAll(
                    Row7(
                        DC4EU_PID,
                        createUUID(),
                        createUUID(),
                        ISSUER_TIR.did,
                        ISSUER_TIR.sigKey,
                        HOLDER_DID.did,
                        HOLDER_DID.key,
                    )
                ) { credentialType, nonce, cnonce, didIssuer, keyIssuer, didHolder, keyHolder ->

                    val endUserBearerToken = """
        eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJib3pCbU1NRGR6YVZRVDEyekQtYmJ2aEczWTZOdUNWdGN0Vi01WDdwNmFVIn0.eyJleHAiOjE3Mzg1NzE5NTAsImlhdCI6MTczODU3MTY1MCwiYXV0aF90aW1lIjoxNzM4NTY4NTI5LCJqdGkiOiJjNDEzYjU1MS05ZjQ4LTRiOTUtYTkwNS1lZjJiMTliZjk4MjEiLCJpc3MiOiJodHRwOi8vdXNlbGYta2V5Y2xvYWsubG9jYWwvcmVhbG1zL3VzZWxmLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImQ1ODRjY2U2LTZhMGYtNDVlZi05ODYxLTRhMWFiY2E4MTE2YyIsInR5cCI6IkJlYXJlciIsImF6cCI6InVzZWxmLWlzc3Vlci1ndWkiLCJub25jZSI6IlRXUjJXR2x5ZEZCMlNVOUdXVzFVVW1SNGNtUi1VMzVWZUd4RE0wWkhiVzFHVEVkblIzSlBkVWMxVWw5RiIsInNlc3Npb25fc3RhdGUiOiI1NWU3Nzk4My00MWU5LTRmYTktODQzYy03ZDA0N2E5N2I4MTciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly91c2VsZi1pc3N1ZXItZ3VpLmxvY2FsIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11c2VsZi1yZWFsbSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJzaWQiOiI1NWU3Nzk4My00MWU5LTRmYTktODQzYy03ZDA0N2E5N2I4MTciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZG9jdW1lbnRfbnVtYmVyIjoiMTIzNDU2Nzg5IiwibmFtZSI6InVzZWxmIGFkbWluIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlbGYtYWRtaW4iLCJnaXZlbl9uYW1lIjoidXNlbGYiLCJmYW1pbHlfbmFtZSI6ImFkbWluIiwiZW1haWwiOiJhZG1pbkB0ZXN0LmNvbSJ9.C-DHYpncTu4e4xgQ_wROXzn01AXfr4nhe1x-iUT5KJZ5rQKiQY_DwXg2Oew_IaOeuSjeKqA3Juqu18J1VUq6cr5AeDlEl9SOl8yXz0ok2uzvYXtvKTCvxNhIOzoe4faDHAnJUdDorSX76CfnEPZIjgiC2TgqW0t94TQp0C94zVVCN8dFoFiRy0-YyiygHpGIG8Cyurnjp3T0tgrRhiZ7OZzj58yTCiwc-wEIPZ7ytf1bBK4jx-OAYUHJMXqQEbBf3XhKKeyqOn32K-MZ2EYBsEjsmaIme1gmbf2w3djlccB6VDzsQ2cic4JfRDEFVrMz_LrUtzUznNLz47M49FO92w
                    """.trimIndent()

                    When("The user requests a PID: request the credential offer") {
                        val initiateRequest = CredentialOfferRequest(
                            bearerToken = endUserBearerToken,
                            credentialType = credentialType,
                            nonce = nonce,
                            redirect = false
                        )
                        val initiateCredentialResponse = issuerService.initiate(sessionMock, initiateRequest)
                        Then("The the credential offer uri and the identifier associated are is returned ") {
                            initiateCredentialResponse shouldNotBe null
                        }

                        When("Using the uri obtained before to request the credential offer") {
                            val id = initiateCredentialResponse.toString().split("%2F")?.last()
                            val credentialOfferResponse = issuerService.getCredentialOfferByID(sessionMock, id!!)
                            Then("The the credential offer is returned and validated") {
                                val credentialOfferResponseExpected = generateCredentialOfferResponseExpected(
                                    credentialType,
                                    nonce,
                                    keyIssuer,
                                    didIssuer,
                                    preAuth = true
                                )
                                credentialOfferResponse shouldBeCredentialOffer credentialOfferResponseExpected
                            }
                            When("With the credential offer the wallet send a  Token Request but with wrong PIN") {
                                val postTokenRequest = TokenRequest(
                                    grantType = "urn:ietf:params:oauth:grant-type:pre-authorized_code",
                                    redirectUri = "https://www.izertis.com",
                                    userPin = "1234",
                                    preAuthorizedCode = credentialOfferResponse.grants?.preAuthorizedCode?.preAuthorizedCode!!
                                )
                                val exception = assertThrows<OIDCException> {
                                    authService.postToken(sessionMock, postTokenRequest)
                                }
                                Then("The the credential offer is returned and validated") {
                                    // Optionally, you can also assert the exception message
                                    assertEquals("User Pin is not correct", exception.message)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

object PreAuthTestHelper {

    val authenticSourceResponse = """
    {"id":"123456789","family_name":"Doe","given_name":"John","birth_date":"1990-01-01","sex":1,"nationality":"Spanish","issuance_date":"2020-01-01","expiry_date":"2030-01-01","issuing_authority":"Government","issuing_country":"SPAIN","age_in_years":30,"age_over_18":true,"age_over_13":true,"document_number":"123456789"}
    """.trimIndent()

    val claims = """
        {"family_name":"Doe","given_name":"John","birth_date":"1990-01-01","sex":1,"nationality":"Spanish","issuance_date":"2020-01-01","expiry_date":"2030-01-01","issuing_authority":"Government","issuing_country":"SPAIN","age_in_years":30,"age_over_18":true,"age_over_13":true,"document_number":"123456789"}
    """.trimIndent()

    val mockAccessTokenResponse = """
            {
              "access_token" : "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvQTJ3aHFDcVpBcF9OS3pZZnp1eWM4T0MyaU1nQjFDbDJRSDFsNkVKZ3hvIn0.eyJleHAiOjE3MzQwNzcyMzUsImlhdCI6MTczNDA3NjkzNSwianRpIjoiZGQ3NTkyZTctYTllMS00NDRjLTg5NmItNGRlMzc4MTg5MjU1IiwiaXNzIjoiaHR0cDovL3VzZWxmLWtleWNsb2FrLmxvY2FsL3JlYWxtcy91c2VsZi1yZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIxY2I1ZTIwZi0wY2ZmLTQyNDYtOGM1MC0yNzUxNDUyZDY3MGYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ1c2VsZi1hZ2VudC1jbGllbnQiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11c2VsZi1yZWFsbSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InVzZWxmLWFnZW50LWNsaWVudCI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxNzIuMTguMC4xIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtdXNlbGYtYWdlbnQtY2xpZW50IiwiY2xpZW50QWRkcmVzcyI6IjE3Mi4xOC4wLjEiLCJjbGllbnRfaWQiOiJ1c2VsZi1hZ2VudC1jbGllbnQifQ.fPefnMWIVAZFfHjA1CrecTJjFWPrUot8C5aFzImi4A8XSyWeKUBYhybiM7JTVBRfL1wLpHJayEPTfjsl8Fdha4_5-_T5ZwRUuLN-COQz4aSTr3R7o6WIEp9xz_JoFsocBLMkqHWGu3tPD4eIdaaQo8D6yHVU5rnmQEiwj4kJRZ-sHzEWCYDSJUu4ImIldnmPwuWHk9XlVo9ma2BberCroNqnPTgiCK4hBfMg-q28yqzQS-DrWmPdZPdhNez0pymeNrqU41_taJAs51BnnUArtcfjUZfsjwwrjM3kCjtyo9r_75KkaW0qnEIXzbc2Di9CHFwJaGquPrEMzUs5BhMMBw",
              "scope" : "openid profile email",
              "id_token" : "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvQTJ3aHFDcVpBcF9OS3pZZnp1eWM4T0MyaU1nQjFDbDJRSDFsNkVKZ3hvIn0.eyJleHAiOjE3MzQwNzcyMzUsImlhdCI6MTczNDA3NjkzNSwiYXV0aF90aW1lIjowLCJqdGkiOiJiNGUzODg3OS1hYjEwLTQyMzktOTQ5OS01NjU0NjhmMjU1MjYiLCJpc3MiOiJodHRwOi8vdXNlbGYta2V5Y2xvYWsubG9jYWwvcmVhbG1zL3VzZWxmLXJlYWxtIiwiYXVkIjoidXNlbGYtYWdlbnQtY2xpZW50Iiwic3ViIjoiMWNiNWUyMGYtMGNmZi00MjQ2LThjNTAtMjc1MTQ1MmQ2NzBmIiwidHlwIjoiSUQiLCJhenAiOiJ1c2VsZi1hZ2VudC1jbGllbnQiLCJhdF9oYXNoIjoiNE83dzlHWi1zTUNFa21LaDhBc3FhdyIsImFjciI6IjEiLCJjbGllbnRIb3N0IjoiMTcyLjE4LjAuMSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LXVzZWxmLWFnZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxNzIuMTguMC4xIiwiY2xpZW50X2lkIjoidXNlbGYtYWdlbnQtY2xpZW50In0.kTylPGzvvyWYyEDo19UNQ4CDcES8D2643Uzg_48zTZW8t1TQn-tRO-l1uFlnfxTnEbBPcNVUligkNChMwNFQ9Pp8cvgww3rpuDkvKt-at2mIwB8Or8YE2EShePe5L19_ozqxfP8OWnlaJHLuTGLBinb13LYv-Fb5B2dWdgkKS-k7pz7iDH-LLY3rBwnjtxlSsf2Gh6dGN31KquObG0mk6nAn2B9G-wJERUAp9Yl-cgHwIKB-lTJO6mkIA93FabR2727JeJiCg0bymYogqPGxXyoS9DDyjFyOpq1XjMaiK6uqb7w-xGh5LEZNLODfcqKPkF_LN6qMECaEcQHndswTYw",
              "token_type" : "Bearer",
              "expires_in" : 300
            }
    """.trimIndent()
}
