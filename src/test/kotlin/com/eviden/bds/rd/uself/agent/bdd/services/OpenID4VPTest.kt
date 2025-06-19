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
@file:Suppress("MaxLineLength", "LongMethod")

package com.eviden.bds.rd.uself.agent.bdd.services

import com.eviden.bds.rd.uself.agent.AgentApplication
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.claimsRTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.claimsTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.claimsTI
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.didDocRTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.didDocTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.didRTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.didTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.proxiesString
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.sigRTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.sigTAO
import com.eviden.bds.rd.uself.agent.mocks.TIRUtils.tirString
import com.eviden.bds.rd.uself.agent.mocks.TestWebSecurityConfig
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateAuthRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateAuthResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getDirectPostRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.jwtClaimsSetConverter
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.signJWT
import com.eviden.bds.rd.uself.agent.mocks.shouldBeAuthResponse
import com.eviden.bds.rd.uself.agent.mocks.shouldBeTokenResponse
import com.eviden.bds.rd.uself.agent.services.openid.auth.AuthService
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.CYLCOMED_CRED
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.DC4EU_CPC
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.DC4EU_EDUCATIONAL_ID
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.DC4EU_PID
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
import com.eviden.bds.rd.uself.common.models.FLOW
import com.eviden.bds.rd.uself.common.models.GRAN_TYPE
import com.eviden.bds.rd.uself.common.models.HOLDER_DID
import com.eviden.bds.rd.uself.common.models.ISSUER_TIR
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenRequest
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenResponse
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.authenticSource.AuthenticSourceService
import com.eviden.bds.rd.uself.common.services.openIdClients.ClientType
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import com.nimbusds.jwt.SignedJWT
import io.kotest.core.annotation.Isolate
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.Row1
import io.kotest.data.forAll
import io.kotest.matchers.shouldNotBe
import io.kotest.property.assume
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.koin.test.KoinTest
import org.koin.test.inject
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import redis.embedded.RedisServer
import java.net.URI

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
//        "uself.preload=dc4eu,cylcomed",
        "uself.preload=dc4eu,tango,cylcomed,ebsiCredential,ebsiAccreditation",
        "uself.server=http://localhost",
        "uself.issuer=https://uself-file-server.dev5.ari-bip.eu",
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
@Isolate
class OpenID4VPTest(
    private val authService: AuthService,
    private val rClientSessionService: RClientSessionService,
) : KoinTest, BehaviorSpec() {

    private val openIdClientService: OpenIdClientService by inject<OpenIdClientService>()
    private val authenticSourceService: AuthenticSourceService by inject<AuthenticSourceService>()

    private lateinit var sessionMock: MockHttpSession
    private lateinit var mockDIDRegServer: MockWebServer
    private lateinit var mockTRServer: MockWebServer
    private lateinit var mockStatusServer: MockWebServer
    private lateinit var redisServer: RedisServer

    private lateinit var jwtTI: SignedJWT
    private lateinit var jwtTAO: SignedJWT
    private lateinit var jwtRTAO: SignedJWT

    private lateinit var tirString: Triple<String?, String?, String?>

    private lateinit var mockTIResponse: MockResponse
    private lateinit var mockTIProxyResponse: MockResponse
    private lateinit var mockTAOResponse: MockResponse
    private lateinit var mockRTAOResponse: MockResponse
    private lateinit var mockDIDTIRegResponse: MockResponse
    private lateinit var mockDIDTAORegResponse: MockResponse
    private lateinit var mockDIDRTAORegResponse: MockResponse
    private lateinit var mockStatusResponse: MockResponse

    override suspend fun beforeSpec(spec: Spec) {
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

        jwtTI = signJWT(ISSUER_TIR.sigKey, ISSUER_TIR.did, jwtClaimsSetConverter(claimsTI))
        jwtTAO = signJWT(sigTAO, didTAO, jwtClaimsSetConverter(claimsTAO))
        jwtRTAO = signJWT(sigRTAO, didRTAO, jwtClaimsSetConverter(claimsRTAO))

        tirString = tirString(
            jwtTI,
            jwtTAO,
            jwtRTAO
        )

        mockTIResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(tirString.first!!)

        mockTIProxyResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(proxiesString())

        mockTAOResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(tirString.second!!)

        mockRTAOResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(tirString.third!!)

        mockDIDTIRegResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(ISSUER_TIR.didDoc)

        mockDIDTAORegResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(didDocTAO)

        mockDIDRTAORegResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(didDocRTAO)

        mockStatusResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
                "eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm5QRjVweG13TjJ4UEgzVHphN3AzZXJMUUsxUlZmUXQ4bjJTaHY3REQxSnluZkJaWWY5M1dWdEV6eW5TNG04TnRHbzVhMjhBY3dybmhrNURYalV3NWdaQ3hqeGdZZ3VpQjdMMjZyNmE5QnN3bzNoMXFKRlAySjRGZ3RadmNXYkxTZVkjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtiblBGNXB4bXdOMnhQSDNUemE3cDNlckxRSzFSVmZRdDhuMlNodjdERDFKeW5mQlpZZjkzV1Z0RXp5blM0bThOdEdvNWEyOEFjd3JuaGs1RFhqVXc1Z1pDeGp4Z1lndWlCN0wyNnI2YTlCc3dvM2gxcUpGUDJKNEZndFp2Y1diTFNlWSIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjMuYXJpLWJpcC5ldS9zdGF0dXMvdjEiLCJuYmYiOjE3NDM2Nzc1MTgsImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtiblBGNXB4bXdOMnhQSDNUemE3cDNlckxRSzFSVmZRdDhuMlNodjdERDFKeW5mQlpZZjkzV1Z0RXp5blM0bThOdEdvNWEyOEFjd3JuaGs1RFhqVXc1Z1pDeGp4Z1lndWlCN0wyNnI2YTlCc3dvM2gxcUpGUDJKNEZndFp2Y1diTFNlWSIsImV4cCI6MTc3NTEyNzExOCwiaWF0IjoxNzQzNjc3NTE4LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh0dHBzOi8vdzNpZC5vcmcvdmMvc3RhdHVzLWxpc3QvMjAyMS92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiU3RhdHVzTGlzdDIwMjFDcmVkZW50aWFsIl0sImlkIjoiaHR0cHM6Ly91c2VsZi1hZ2VudC5kZXYzLmFyaS1iaXAuZXUvc3RhdHVzL3YxIiwiY3JlZGVudGlhbFNjaGVtYSI6eyJpZCI6Imh0dHBzOi8vYXBpLWNvbmZvcm1hbmNlLmVic2kuZXUvdHJ1c3RlZC1zY2hlbWFzLXJlZ2lzdHJ5L3YyL3NjaGVtYXMvejNNZ1VGVWtiNzIydXE0eDNkdjV5QUptbk5tekRGZUs1VUM4eDgzUW9lTEpNIiwidHlwZSI6IkZ1bGxKc29uU2NoZW1hVmFsaWRhdG9yMjAyMSJ9LCJ2YWxpZEZyb20iOiIyMDI1LTA0LTAzVDEwOjUxOjU4WiIsImlzc3VlZCI6IjIwMjUtMDQtMDNUMTA6NTE6NThaIiwiaXNzdWVyIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JuUEY1cHhtd04yeFBIM1R6YTdwM2VyTFFLMVJWZlF0OG4yU2h2N0REMUp5bmZCWllmOTNXVnRFenluUzRtOE50R281YTI4QWN3cm5oazVEWGpVdzVnWkN4anhnWWd1aUI3TDI2cjZhOUJzd28zaDFxSkZQMko0Rmd0WnZjV2JMU2VZIiwiaXNzdWFuY2VEYXRlIjoiMjAyNS0wNC0wM1QxMDo1MTo1OFoiLCJleHBpcmF0aW9uRGF0ZSI6IjIwMjYtMDQtMDJUMTA6NTE6NThaIiwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjMuYXJpLWJpcC5ldS9zdGF0dXMvdjEiLCJ0eXBlIjoiU3RhdHVzTGlzdDIwMjEiLCJzdGF0dXNQdXJwb3NlIjoicmV2b2NhdGlvbiIsImVuY29kZWRMaXN0IjoiSDRzSUFBQUFBQUFBLyszQk1RRUFBQURDb1BWUGJRd2ZvQUFBQUFBQUFBQUFBQUFBQUFBQUFJQzNBWWJTVktzQVFBQUEifX0sImp0aSI6Imh0dHBzOi8vdXNlbGYtYWdlbnQuZGV2My5hcmktYmlwLmV1L3N0YXR1cy92MSJ9.tP7Lk2hW1lz5kInS8aDHg_oslH_R4tsj83rr8EEW7bSVPRozNdwjw8ZgfP9yc6BUALfxXE5CCxE_K1LTyDvH2Q".trimIndent()
            )

        mockDIDRegServer = MockWebServer()
        mockDIDRegServer.start(9998)

        mockTRServer = MockWebServer()
        mockTRServer.start(9997)

        // This message enqueued corresponds to the initialization of the server. As we are creating the server once
        // we need to enqueue the message once
        mockTRServer.enqueue(mockTIResponse)
        mockTRServer.enqueue(mockTIProxyResponse)

        mockStatusServer = MockWebServer()
        mockStatusServer.start(9996)
        mockStatusServer.enqueue(mockStatusResponse)

        redisServer = RedisServer(6370)
        redisServer.start()
        sessionMock = MockHttpSession()
    }

    override suspend fun afterSpec(spec: Spec) {
        mockDIDRegServer.shutdown()
        mockTRServer.shutdown()
        redisServer.stop()
    }

    init {
        Context("Verifier wants to authenticate an end user using Verifiable Credentials") {
            Given("End User access the Verifier web site") {
                And("Verifier wants to verify that end user possess a Credential") {
                    forAll(
                        Row1(DC4EU_PID),
                        Row1(DC4EU_EDUCATIONAL_ID),
                    ) { credentialType ->
                        val nonce = createUUID()
                        val state = createUUID()
                        val cnonce = createUUID()
                        val didIssuer = ISSUER_TIR.did
                        val keyIssuer = ISSUER_TIR.sigKey
                        val didHolder = HOLDER_DID.did
                        val keyHolder = HOLDER_DID.key
                        val codeChallenge = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg"
                        val subjectClaims = mutableMapOf(
                            "document_number" to "123456789",
                            "given_name" to "John",
                        )

                        var autzValid = false
                        var autzFromUriValid = false
                        var userAutenticated = false

                        /*
                         * Add the responses corresponding to each executio of the test. This is carried out each
                         * execution of the test
                         */
                        mockDIDRegServer.enqueue(mockDIDTIRegResponse)
                        mockDIDRegServer.enqueue(mockDIDTAORegResponse)
                        mockDIDRegServer.enqueue(mockDIDRTAORegResponse)

                        mockTRServer.enqueue(mockTIResponse)
                        mockTRServer.enqueue(mockTAOResponse)
                        mockTRServer.enqueue(mockRTAOResponse)

                        mockStatusServer.enqueue(mockStatusResponse)
                        mockStatusServer.enqueue(mockStatusResponse)

                        When("Verifier generates an Authorisation Request for the end user") {
                            val request = generateAuthRequest(
                                didHolder,
                                state,
                                nonce,
                                credentialType,
                                codeChallenge,
                                FLOW.VP,
                            )
                            val result = authService.getAuthorize(sessionMock, request)
                            When("Obtain the Auth request_uri") {

                                Then("Verify the authorise request uri is valid") {
                                    result shouldNotBe null
                                    autzValid = true
                                }
                            }

                            assume(autzValid)
                            When("Obtain a Authorisation object using the request_uri") {
                                val authURIID = getURIId(result)
                                val authResponse = authService.getRequestURI(sessionMock, authURIID)
                                println(authResponse)
                                Then("Verify the result") {
                                    val authResponseExpected = generateAuthResponseExpected(
                                        nonce,
                                        state,
                                        keyIssuer,
                                        didIssuer,
                                        credentialType,
                                        FLOW.VP
                                    )
                                    authResponse shouldNotBe null
                                    authResponse!! shouldBeAuthResponse authResponseExpected
                                    autzFromUriValid = true
                                }
                            }
                        }

                        assume(autzFromUriValid) // Check with Angel
                        When("End user provides a verifiable presentation to the Verifier") {
                            val postDirect = getDirectPostRequest(
                                nonce,
                                state,
                                credentialType,
                                didHolder,
                                didIssuer,
                                keyHolder,
                                keyIssuer,
                                subjectClaims,
                                FLOW.VP
                            )
                            val directPostResponse = authService.postDirectPost(
                                sessionMock,
                                postDirect
                            )
                            Then(
                                "Verify the result if the end user has been authenticate (the verifiable presentation is valid)"
                            ) {
                                directPostResponse shouldNotBe null
                                userAutenticated = true
                            }
                        }

                        assume(userAutenticated)
                        When("With the credential offer the wallet send a  Token Request") {
                            val session = rClientSessionService.findByNonce(nonce).first()
                            val postTokenRequest = TokenRequest(
                                grantType = GRAN_TYPE.AUTH_CODE,
                                clientId = didHolder,
                                redirectUri = "https://www.izertis.com",
                                codeVerifier = "password"
                            )
                            postTokenRequest.code = session.code
                            session.codeChallenge = codeChallenge
                            session.cNonce = cnonce
                            rClientSessionService.saveSession(session)
                            val tokenResponse = authService.postToken(sessionMock, postTokenRequest)
                            Then("The credential offer is returned and validated") {
                                tokenResponse shouldBeTokenResponse TokenResponse(tokenType = "Bearer", expiresIn = 200)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun getURIId(authURIResponse: String): String {
        val authResponseURI = URI(authURIResponse)
        val query = authResponseURI.query
        val params = query.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }
        val authURIID = params["request_uri"]
        return authURIID!!.split("/").last()
    }
}
