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
@file:Suppress("MaxLineLength")

package com.eviden.bds.rd.uself.agent.bdd.services

import com.eviden.bds.rd.uself.agent.AgentApplication
import com.eviden.bds.rd.uself.agent.bdd.services.AuthTestHelper.authenticSourceResponse
import com.eviden.bds.rd.uself.agent.bdd.services.AuthTestHelper.mockAccessTokenResponse
import com.eviden.bds.rd.uself.agent.mocks.*
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
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateAuthRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateAuthResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.generateCredentialOfferResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getCredentialResponseExpected
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getDirectPostRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.getIssueCredentialRequest
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.jwtClaimsSetConverter
import com.eviden.bds.rd.uself.agent.mocks.WalletMock.signJWT
import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession
import com.eviden.bds.rd.uself.agent.services.openid.auth.AuthService
import com.eviden.bds.rd.uself.agent.services.openid.issuer.IssuerService
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES.DC4EU_EDUCATIONAL_ID
import com.eviden.bds.rd.uself.common.models.FLOW
import com.eviden.bds.rd.uself.common.models.GRAN_TYPE
import com.eviden.bds.rd.uself.common.models.HOLDER_DID
import com.eviden.bds.rd.uself.common.models.ISSUER_TIR
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenRequest
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenResponse
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialOfferRequest
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialOfferResponse
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.authenticSource.AuthenticSourceService
import com.eviden.bds.rd.uself.common.services.openIdClients.ClientType
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.Row9
import io.kotest.data.forAll
import io.kotest.matchers.shouldNotBe
import io.kotest.property.assume
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
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
        "uself.preload=dc4eu",
        "uself.server=http://localhost",
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
        "spring.data.redis.port=6370",
    ]
)
@ContextConfiguration(classes = [AgentApplication::class, TestWebSecurityConfig::class])
class OIDC4VCIAuthTest(
    private val authService: AuthService,
    private val issuerService: IssuerService,
    private val rClientSessionService: RClientSessionService
) : KoinTest, BehaviorSpec() {

    private val openIdClientService: OpenIdClientService by inject<OpenIdClientService>()
    private val authenticSourceService: AuthenticSourceService by inject<AuthenticSourceService>()

    private lateinit var sessionMock: MockHttpSession
    private lateinit var mockAuthSourceServer: MockWebServer
    private lateinit var mockKeyCloakServer: MockWebServer
    private lateinit var mockDIDRegServer: MockWebServer
    private lateinit var mockTRServer: MockWebServer
    private lateinit var mockStatusServer: MockWebServer
    private val redisServer = RedisServer(6370)

    private val mockTokenResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(mockAccessTokenResponse)

    private val mockUserInfoResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(authenticSourceResponse)

    private val jwtTI = signJWT(ISSUER_TIR.sigKey, ISSUER_TIR.did, jwtClaimsSetConverter(claimsTI))
    private val jwtTAO = signJWT(sigTAO, didTAO, jwtClaimsSetConverter(claimsTAO))
    private val jwtRTAO = signJWT(sigRTAO, didRTAO, jwtClaimsSetConverter(claimsRTAO))

    private val tirString = tirString(
        jwtTI,
        jwtTAO,
        jwtRTAO
    )
    private val mockTIResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(tirString.first!!)
    private val mockTIProxyResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(proxiesString())
    private val mockTAOResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(tirString.second!!)
    private val mockRTAOResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(tirString.third!!)
    private val mockDIDTIRegResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(ISSUER_TIR.didDoc)
    private val mockDIDTAORegResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(didDocTAO)
    private val mockDIDRTAORegResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(didDocRTAO)
    private val mockStatusResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(
            "eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm5QRjVweG13TjJ4UEgzVHphN3AzZXJMUUsxUlZmUXQ4bjJTaHY3REQxSnluZkJaWWY5M1dWdEV6eW5TNG04TnRHbzVhMjhBY3dybmhrNURYalV3NWdaQ3hqeGdZZ3VpQjdMMjZyNmE5QnN3bzNoMXFKRlAySjRGZ3RadmNXYkxTZVkjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtiblBGNXB4bXdOMnhQSDNUemE3cDNlckxRSzFSVmZRdDhuMlNodjdERDFKeW5mQlpZZjkzV1Z0RXp5blM0bThOdEdvNWEyOEFjd3JuaGs1RFhqVXc1Z1pDeGp4Z1lndWlCN0wyNnI2YTlCc3dvM2gxcUpGUDJKNEZndFp2Y1diTFNlWSIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjMuYXJpLWJpcC5ldS9zdGF0dXMvdjEiLCJuYmYiOjE3NDM2Nzc1MTgsImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtiblBGNXB4bXdOMnhQSDNUemE3cDNlckxRSzFSVmZRdDhuMlNodjdERDFKeW5mQlpZZjkzV1Z0RXp5blM0bThOdEdvNWEyOEFjd3JuaGs1RFhqVXc1Z1pDeGp4Z1lndWlCN0wyNnI2YTlCc3dvM2gxcUpGUDJKNEZndFp2Y1diTFNlWSIsImV4cCI6MTc3NTEyNzExOCwiaWF0IjoxNzQzNjc3NTE4LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh0dHBzOi8vdzNpZC5vcmcvdmMvc3RhdHVzLWxpc3QvMjAyMS92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiU3RhdHVzTGlzdDIwMjFDcmVkZW50aWFsIl0sImlkIjoiaHR0cHM6Ly91c2VsZi1hZ2VudC5kZXYzLmFyaS1iaXAuZXUvc3RhdHVzL3YxIiwiY3JlZGVudGlhbFNjaGVtYSI6eyJpZCI6Imh0dHBzOi8vYXBpLWNvbmZvcm1hbmNlLmVic2kuZXUvdHJ1c3RlZC1zY2hlbWFzLXJlZ2lzdHJ5L3YyL3NjaGVtYXMvejNNZ1VGVWtiNzIydXE0eDNkdjV5QUptbk5tekRGZUs1VUM4eDgzUW9lTEpNIiwidHlwZSI6IkZ1bGxKc29uU2NoZW1hVmFsaWRhdG9yMjAyMSJ9LCJ2YWxpZEZyb20iOiIyMDI1LTA0LTAzVDEwOjUxOjU4WiIsImlzc3VlZCI6IjIwMjUtMDQtMDNUMTA6NTE6NThaIiwiaXNzdWVyIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JuUEY1cHhtd04yeFBIM1R6YTdwM2VyTFFLMVJWZlF0OG4yU2h2N0REMUp5bmZCWllmOTNXVnRFenluUzRtOE50R281YTI4QWN3cm5oazVEWGpVdzVnWkN4anhnWWd1aUI3TDI2cjZhOUJzd28zaDFxSkZQMko0Rmd0WnZjV2JMU2VZIiwiaXNzdWFuY2VEYXRlIjoiMjAyNS0wNC0wM1QxMDo1MTo1OFoiLCJleHBpcmF0aW9uRGF0ZSI6IjIwMjYtMDQtMDJUMTA6NTE6NThaIiwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjMuYXJpLWJpcC5ldS9zdGF0dXMvdjEiLCJ0eXBlIjoiU3RhdHVzTGlzdDIwMjEiLCJzdGF0dXNQdXJwb3NlIjoicmV2b2NhdGlvbiIsImVuY29kZWRMaXN0IjoiSDRzSUFBQUFBQUFBLyszQk1RRUFBQURDb1BWUGJRd2ZvQUFBQUFBQUFBQUFBQUFBQUFBQUFJQzNBWWJTVktzQVFBQUEifX0sImp0aSI6Imh0dHBzOi8vdXNlbGYtYWdlbnQuZGV2My5hcmktYmlwLmV1L3N0YXR1cy92MSJ9.tP7Lk2hW1lz5kInS8aDHg_oslH_R4tsj83rr8EEW7bSVPRozNdwjw8ZgfP9yc6BUALfxXE5CCxE_K1LTyDvH2Q".trimIndent()
        )

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
        mockDIDRegServer.enqueue(mockDIDTIRegResponse)
        mockDIDRegServer.enqueue(mockDIDTAORegResponse)
        mockDIDRegServer.enqueue(mockDIDRTAORegResponse)

        mockTRServer = MockWebServer()
        mockTRServer.start(9997)
        mockTRServer.enqueue(mockTIResponse)
        mockTRServer.enqueue(mockTIProxyResponse)
        mockTRServer.enqueue(mockTIResponse)
        mockTRServer.enqueue(mockTAOResponse)
        mockTRServer.enqueue(mockRTAOResponse)

        mockStatusServer = MockWebServer()
        mockStatusServer.start(9996)
        mockStatusServer.enqueue(mockStatusResponse)
        mockStatusServer.enqueue(mockStatusResponse)
    }

    override suspend fun afterSpec(spec: Spec) {
        mockAuthSourceServer.shutdown()
        mockKeyCloakServer.shutdown()
        mockDIDRegServer.shutdown()
        mockTRServer.shutdown()
        mockStatusServer.shutdown()
        redisServer.stop()
    }

    init {
        Context("Obtaining a VerifiableEducationalID with an Authorization flow") {
            var requestCredSuccess = false
            var credValidationSuccess = false
            var autzReqSuccess = false
            var authReqValidationSuccess = false
            var credCodeSentSuccess = false
            var credOfferValidationSuccess = false
            lateinit var tokenResponse: TokenResponse

            Given("End user has already a PID credential") {
                val helperObject = Triple(
                    "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg",
                    authenticSourceResponse,
                    mutableMapOf(
                        "document_number" to "123456789",
                        "given_name" to "John",
                    )
                )
                forAll(
                    Row9(
                        DC4EU_EDUCATIONAL_ID,
                        createUUID(),
                        createUUID(),
                        createUUID(),
                        ISSUER_TIR.did,
                        ISSUER_TIR.sigKey,
                        HOLDER_DID.did,
                        HOLDER_DID.key,
                        helperObject
                    )
                ) { credentialType, nonce, cnonce, state, didIssuer, keyIssuer, didHolder, keyHolder, helper ->
                    lateinit var initiateCredentialResponse: URI
                    val authSourceClaims = helper.second
                    val codeChallenge = helper.first
                    val subjectClaims = helper.third
                    When(
                        "The user requests a PID: request the credential offer with credential type $credentialType"
                    ) {
                        val initiateRequest = CredentialOfferRequest(
                            credentialType = credentialType,
                            nonce = nonce,
                            redirect = false
                        )
                        initiateCredentialResponse = issuerService.initiate(sessionMock, initiateRequest)
                        Then("The credential offer uri and the identifier associated are is returned ") {
                            initiateCredentialResponse shouldNotBe null
                            requestCredSuccess = true
                        }
                    }

                    assume(requestCredSuccess)
                    lateinit var credentialOfferResponse: CredentialOfferResponse
                    When("Using the uri obtained before to request the credential offer") {
                        val id = initiateCredentialResponse.toString().split("%2F").last()
                        credentialOfferResponse = issuerService.getCredentialOfferByID(sessionMock, id)
                        Then("The credential offer is returned and validated") {
                            val credentialOfferResponseExpected = generateCredentialOfferResponseExpected(
                                credentialType,
                                nonce,
                                keyIssuer,
                                didIssuer,
                                preAuth = false
                            )
                            credentialOfferResponse shouldBeCredentialOffer credentialOfferResponseExpected
                            credValidationSuccess = true
                        }
                    }

                    assume(credValidationSuccess)
                    lateinit var authURIResponse: String
                    When("Using the value form the credential offer the End User request an authorization") {
                        val authRequest = generateAuthRequest(
                            didHolder,
                            state,
                            nonce,
                            credentialType,
                            codeChallenge,
                            FLOW.VCI
                        )
                        authRequest.issuerState = credentialOfferResponse.grants!!.authorizationCode!!.issuerState
                        authURIResponse = authService.getAuthorize(sessionMock, authRequest)
                        Then("The  system returns a request_uri where to obtain the authorization request") {
                            authURIResponse shouldNotBe null
                            autzReqSuccess = true
                        }
                    }

                    assume(autzReqSuccess)
                    When("The end user obtain the final authorization request using the previous uri") {
                        val authURIID = getURIId(authURIResponse)
                        val authResponse = authService.getRequestURI(sessionMock, authURIID)
                        Then("The auth request is obtained an validated") {
                            val authResponseExpected = generateAuthResponseExpected(
                                nonce,
                                state,
                                keyIssuer,
                                didIssuer,
                                credentialType,
                                FLOW.VCI
                            )
                            authResponse shouldNotBe null
                            authResponse!! shouldBeAuthResponse authResponseExpected
                            authReqValidationSuccess = true
                        }
                    }

                    assume(authReqValidationSuccess)
                    When(
                        "Once obtained the authorise request, the end user should send the vp_token with the pid credential"
                    ) {
                        val postDirect = getDirectPostRequest(
                            nonce,
                            state,
                            credentialType,
                            didHolder,
                            didIssuer,
                            keyHolder,
                            keyIssuer,
                            subjectClaims,
                            FLOW.VCI
                        )
                        val directPostResponse = authService.postDirectPost(
                            sessionMock,
                            postDirect
                        )

                        Then("The system validate the credential and sends the code associated") {
                            directPostResponse shouldNotBe null
                            credCodeSentSuccess = true
                        }
                    }

                    assume(credCodeSentSuccess)
                    lateinit var session: RClientSession
                    When("With the credential offer the wallet send a  Token Request") {
                        session = rClientSessionService.findByNonce(nonce).first()
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
                        tokenResponse = authService.postToken(sessionMock, postTokenRequest)
                        Then("The credential offer is returned and validated") {
                            tokenResponse!! shouldBeTokenResponse TokenResponse(tokenType = "Bearer", expiresIn = 200)
                            credOfferValidationSuccess = true
                        }
                    }

                    assume(credOfferValidationSuccess)
                    When("With the credential offer the wallet send a  Token Request") {
                        // change the cNonce to fix the test data request
                        session = rClientSessionService.findByNonce(nonce).first()
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
                            val jsonObject = JSONObject(authSourceClaims)
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

object AuthTestHelper {

    val authenticSourceResponse = """
        {"identifier":"123456789","familyName":"Doe","firstName":"John","displayName":"John Doe","dateOfBirth":"1990-01-01","commonName":"John Doe","mail":"john.doe@example.com","eduPersonPrincipalName":"44444444-A@urv.cat","eduPersonPrimaryAffiliation":"student","eduPersonScopedAffiliation":"[student@urv.cat]","eduPersonAssurance":"[https://refeds.org/assurance/IAP/low]"}
    """.trimIndent()
    val mockAccessTokenResponse = """
            {
              "access_token" : "eyJraWQiOiJkaWQ6ZWJzaTp6dFJvWXlKTmRHcjh0bUF0Vmg5Y2c5biIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsInN1YiI6ImRpZDplYnNpOnp0Um9ZeUpOZEdyOHRtQXRWaDljZzluIiwiaXNzIjoiaHR0cHM6Ly90YWRwb2xlLWludGVybmFsLW1hbW1hbC5uZ3Jvay1mcmVlLmFwcC9hdXRoIiwiY2xhaW1zIjp7ImNOb25jZSI6IjgyNzgxOTczMzA3NjE2MDUwNTkiLCJjTm9uY2VFeHBpcmVzSW4iOjE3Mzg1Nzc2MjIyMTksImNsaWVudElkIjoiZGlkOmVic2k6enRSb1l5Sk5kR3I4dG1BdFZoOWNnOW4ifSwiZXhwIjoxNzg5ODMzNjAwLCJpYXQiOjE3MjU2MDc0MTMsIm5vbmNlIjoiODI3ODE5NzMzMDc2MTYwNTA1OSJ9.xt_E279K9S5n0Hrq5LDRsmB1xxn9UNGcV1gqC1zhV9By8qrElVSqe5gSiYiJO-VP14KawtQcRDYAf8cAFsoHEg",
              "scope" : "openid profile email",
              "id_token" : "eyJraWQiOiJkaWQ6ZWJzaTp6dFJvWXlKTmRHcjh0bUF0Vmg5Y2c5biIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6ZWJzaTp6dFJvWXlKTmRHcjh0bUF0Vmg5Y2c5biIsImF1ZCI6ImRpZDplYnNpOnp0Um9ZeUpOZEdyOHRtQXRWaDljZzluIiwiaXNzIjoiaHR0cHM6Ly90YWRwb2xlLWludGVybmFsLW1hbW1hbC5uZ3Jvay1mcmVlLmFwcC9hdXRoIiwiZXhwIjoxNzg5ODMzNjAwLCJpYXQiOjE3MjU2MDc0MTMsIm5vbmNlIjoiODI3ODE5NzMzMDc2MTYwNTA1OSJ9.i7lJ_Xp8zL4alve6SN9QzXVNMBfebvP1QZw8rpxHmaBtqRfqqbQCwcno6tB1juOXQOMKfJCHRnkgsqwt7LB0jA",
              "token_type" : "Bearer",
              "expires_in" : 300
            }
    """.trimIndent()
}
