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

package com.eviden.bds.rd.uself.agent.services.openid

import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential
import com.eviden.bds.rd.uself.agent.AgentApplication
import com.eviden.bds.rd.uself.agent.mocks.*
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.mockRClientSession
import com.eviden.bds.rd.uself.agent.services.openid.auth.AuthService
import com.eviden.bds.rd.uself.agent.services.openid.issuer.IssuerService
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES
import com.eviden.bds.rd.uself.common.models.GRAN_TYPE
import com.eviden.bds.rd.uself.common.models.openid.auth.TokenRequest
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialOfferResponse
import com.eviden.bds.rd.uself.common.services.Utils.getUserPin
import com.eviden.bds.rd.uself.common.services.httpClient.HTTPClient
import com.eviden.bds.rd.uself.common.services.openIdClients.ClientType
import com.eviden.bds.rd.uself.common.services.openIdClients.repository.ClientRepository
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.contain
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@Ignored
@TestPropertySource(
    properties = [
        "uself.did.method=DID_WEB",
        "uself.preload=ebsiCredential, tango",
        "uself.server=http://localhost",
        "uself.authentic-source-url=http://luself-authentic-source",
        "uself.issuer=https://uself-file-server.dev5.ari-bip.eu",
        "uself.openid-idp-server=http://localhost:8080/realms/master/broker/uself/endpoint",
        "uself.openid-client-id=uself-agent-client",
        "uself.openid-client-secret=password",
        "uself.openid-client-gui=http://uself-agent-web:4200/present",
        "uself.openid-client-provider=uself",
        "uself.openid-client-token=http://localhost:8080/realms/master/broker/uself/token",
        "master.key.kms=",
        "uself.ebsiApis.serverURL=https://api-conformance.ebsi.eu",
        "uself.ebsiApis.didRegURL=https://api-conformance.ebsi.eu/did-registry/v5",
        "uself.ebsiApis.trustedIssuersURL=https://api-conformance.ebsi.eu/trusted-issuers-registry/v5",
        "uself.ebsiApis.ledgerURL=https://api-conformance.ebsi.eu/ledger/v4/blockchains/besu",
        "uself.ebsiApis.identifiersURL=https://api-conformance.ebsi.eu/did-registry/v5/identifiers",
        "uself.ebsiApis.issuerURL=https://api-conformance.ebsi.eu/conformance/v3/issuer-mock",
        "uself.ebsiApis.authURL=https://api-conformance.ebsi.eu/conformance/v3/auth-mock",
        "uself.ebsiApis.authzURL=https://api-conformance.ebsi.eu/authorisation/v4",
        "uself.ebsiApis.presentationDefinitionURL=https://api-conformance.ebsi.eu/authorisation/v4/presentation-definitions",
        "uself.ebsiApis.tokenURL=https://api-conformance.ebsi.eu/authorisation/v4/token",
        "uself.ebsiApis.initiateURL=https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/initiate-credential-offer"
    ]
)
@ContextConfiguration(classes = [(AgentApplication::class)])
class OpenID4VCITest(
    private val authService: AuthService,
    private val issuerService: IssuerService,
    private val rClientSessionService: RClientSessionService
) : KoinTest, BehaviorSpec({

    val clientRepository: ClientRepository by inject(ClientRepository::class.java)

    lateinit var mockWebServer: MockWebServer
    lateinit var sessionMock: HttpSession

    beforeTest {
        sessionMock = MockHttpSession()
        rClientSessionService.saveSession(mockRClientSession)
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"name":"John","email":"john@email.com"}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val apiClient = MockHTTPClient(mockEngine)
            val testModule = module {
                single<HTTPClient> { apiClient }
            }
            loadKoinModules(testModule)
        }
    }

    var credentialTypes: ArrayList<String>
    Context("End user obtains a issued Verifiable Credential") {
        Given("End User is not already authenticated") {
            And("End user wants a " + CREDENTIAL_TYPES.EBSI_SAME_IN_TIME) {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                    CREDENTIAL_TYPES.EBSI_SAME_IN_TIME
                )

                When("Obtain a Credential Offer") {

                    val request = MOCKS_AUTH.CredentialOffer.request
                    val result = issuerService.initiate(sessionMock, request).toString()
                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            val offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.authOffer
                            }
                        }
                    }
                }

                When("Obtain an Authorisation") {

                    val request = MOCKS_AUTH.Authorisation.request
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResult.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
                        When("Obtain a Authorisation object using the request_uri") {
                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
                            Then("Verify the result") {
                                auth shouldNotBe null
                            }
                        }
                    }
                }

                When("Obtain an Authorisation without client metadata") {

                    val request = MOCKS_AUTH.Authorisation.request
                    request.clientMetadata = null
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResult.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
                        When("Obtain a Authorisation object using the request_uri") {
                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
                            Then("Verify the result") {
                                auth shouldNotBe null
                            }
                        }
                    }
                }
                var code = "code"
                When("Authenticate the end user via direct_post") {
                    val result = authService.postDirectPost(sessionMock, MOCKS_AUTH.DirectPost.request)
                    Then("Verify the result") {

                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.DirectPost.expectedResult.split("&")
                        // nonce
                        // resultParsed[0] shouldBe expectedParsed[0]
                        resultParsed[0] shouldNotBe null
                        code = resultParsed[0].split("=").last()
                        // state
                        // resultWithoutSignedRequest[1] shouldBe expectedWithoutSignedRequest[1]
                    }
                }

                When("Obtain the Access Token using the code obtained in the previous step") {
                    val tokenReq = MOCKS_AUTH.GetToken.request
                    tokenReq.code = code
                    tokenReq.codeVerifier = "password"
                    val result = authService.postToken(sessionMock, tokenReq)
                    Then("Verify the result") {
                        result shouldNotBe null
                    }
                }

                When("Obtain the Verifiable Credential using the Access Token obtained on the previous operation") {
                    // we change the value of the cNonce to match the jwt
                    val rSession = rClientSessionService.findByCode(code).firstOrNull()
                    rSession!!.cNonce = "8661435939417066112"
                    rClientSessionService.saveSession(rSession)
                    val request = MOCKS_AUTH.IssueCredential.request
                    request.bearerToken = MOCKS_AUTH.IssueCredential.bearer

                    val result = issuerService.postCredential(sessionMock, request)
                    Then("Verify the result") {
                        result.credential shouldNotBe null
                        val vc = JwtVerifiableCredential.fromCompactSerialization(result.credential)
                        vc shouldNotBe null
                    }
                }
            }

            And("End user wants a " + CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO) {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                    CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
                )

                When("Obtain a Credential Offer") {

                    val request = MOCKS_AUTH.CredentialOffer.request
                    request.credentialType = credentialTypes.last()
                    val result = issuerService.initiate(sessionMock, request).toString()

                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            val offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.passportAuthOffer
                            }
                        }
                    }
                }

                When("Obtain an Authorisation") {

                    val request = MOCKS_AUTH.Authorisation.requestEpassport
                    request.authorizationDetails?.get(0)!!.types = credentialTypes
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResultEpassport.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
                        When("Obtain a Authorisation object using the request_uri") {
                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
                            Then("Verify the result") {
                                auth shouldNotBe null
                            }
                        }
                    }
                }

                When("Authenticate the end user via direct_post") {
                    val result = authService.postDirectPostPassport(sessionMock, MOCKS_AUTH.DirectPostPassport.request)
                    Then("Verify the result") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.DirectPostPassport.expectedResult.split("&")
                        // nonce
                        resultParsed[0] shouldNotBe null
                        // state
                        // resultWithoutSignedRequest[1] shouldBe expectedWithoutSignedRequest[1]
                    }
                }

                When("Obtain the Access Token using the code obtained in the previous step") {
                    val result = authService.postToken(sessionMock, MOCKS_AUTH.GetToken.requestEPassport)
                    Then("Verify the result") {
                    }
                }

                When("Obtain the Verifiable Credential using the Access Token obtained on the previous operation") {
                    sessionMock.setAttribute(
                        "userInfo",
                        JSONObject(
                            MOCKS_AUTH.IssueCredential.expectedCredentialPassport.credentialSubject.jsonObject
                        ).toString()
                    )
                    val request = MOCKS_AUTH.IssueCredential.requestEPassport

                    request.bearerToken = MOCKS_AUTH.IssueCredential.bearerEPassport
                    // request.types = credentialTypes
                    val result = issuerService.postCredential(sessionMock, request)
                    Then("Verify the result") {
                        result.credential shouldNotBe null
                        val vc = JwtVerifiableCredential.fromCompactSerialization(result.credential)
                        vc shouldNotBe null
                    }
                }
            }
            And("End user wants a " + CREDENTIAL_TYPES.TANGO_EMPLOYEE_CREDENTIAL) {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL,
                    CREDENTIAL_TYPES.TANGO_EMPLOYEE_CREDENTIAL
                )
                val tangoClientData = OpenIdClientData(
                    ClientType.KEYCLOAK,
                    "http://localhost:8080/auth/realms/master",
                    "tango",
                    "client-secret",
                    "http://localhost:8080/auth/realms/master/protocol/openid-connect/auth",
                    "tango-provider"
                )
                val tangoClientDataString = Json.encodeToString(tangoClientData)
                clientRepository.add(tangoClientData.clientId, tangoClientDataString)

                mockWebServer = MockWebServer()
                mockWebServer.start(8080)
                val mockTokenResponse = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(MOCKS_OPENID_CLIENT.mockAcessTokenResponse)
                mockWebServer.enqueue(mockTokenResponse)

                val mockUserInfoResponse = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(MOCKS_OPENID_CLIENT.expectedUserInfo)
                mockWebServer.enqueue(mockUserInfoResponse)
                // mockWebServer.enqueue(MockResponse().setBody(expectedAccessToken).setResponseCode(200))

                When("Obtain a Credential Offer") {

                    val request = MOCKS_AUTH.CredentialOffer.request
                    request.credentialType = credentialTypes.last()
                    val result = issuerService.initiate(sessionMock, request).toString()
                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            val offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.employeeAuthOffer
                            }
                        }
                    }
                }

                When("Obtain an Authorisation") {

                    val request = MOCKS_AUTH.Authorisation.request
                    request.authorizationDetails?.get(0)!!.types = credentialTypes
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResult.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
                        When("Obtain a Authorisation object using the request_uri") {
                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
                            Then("Verify the result") {
                                auth shouldNotBe null
                            }
                        }
                    }
                }

                When("Authenticate the end user via direct_post") {
                    // setting the correct nonce for this id token
                    val result = authService.postDirectPost(sessionMock, MOCKS_AUTH.DirectPost.request)
                    Then("Verify the result") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.DirectPostPassport.expectedResult.split("&")
                        // nonce
                        resultParsed[0] shouldNotBe null
                        // state
                        // resultWithoutSignedRequest[1] shouldBe expectedWithoutSignedRequest[1]
                    }
                }

                When("Obtain the Access Token using the code obtained in the previous step") {
                    val result = authService.postToken(sessionMock, MOCKS_AUTH.GetToken.request)
                    Then("Verify the result") {
                    }
                }

                When("Obtain the Verifiable Credential using the Access Token obtained on the previous operation") {
                    val request = MOCKS_AUTH.IssueCredential.requestEmployee

                    request.bearerToken = MOCKS_AUTH.IssueCredential.bearer
                    // request.types = credentialTypes
                    val result = issuerService.postCredential(sessionMock, request)
                    Then("Verify the result") {
                        result.credential shouldNotBe null
                        val vc = JwtVerifiableCredential.fromCompactSerialization(result.credential)
                        vc shouldNotBe null
                    }
                }

                mockWebServer.shutdown()
            }
            And("End user wants a " + CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL) {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL
                )
                mockWebServer = MockWebServer()
                mockWebServer.start(8080)
                val mockTokenResponse = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(MOCKS_OPENID_CLIENT.mockAcessTokenResponse)
                mockWebServer.enqueue(mockTokenResponse)

                val mockUserInfoResponse = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(MOCKS_OPENID_CLIENT.expectedUserInfo)
                mockWebServer.enqueue(mockUserInfoResponse)

                When("Obtain a Credential Offer") {

                    val request = MOCKS_AUTH.CredentialOffer.request
                    request.credentialType = credentialTypes.last()
                    val result = issuerService.initiate(sessionMock, request).toString()
                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            val offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.customerAuthOffer
                            }
                        }
                    }
                }

                When("Obtain an Authorisation") {

                    val request = MOCKS_AUTH.Authorisation.request
                    request.authorizationDetails?.get(0)!!.types = credentialTypes
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResult.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
                        When("Obtain a Authorisation object using the request_uri") {
                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
                            Then("Verify the result") {
                                auth shouldNotBe null
                            }
                        }
                    }
                }

                When("Authenticate the end user via direct_post") {
                    val result = authService.postDirectPost(sessionMock, MOCKS_AUTH.DirectPost.request)
                    Then("Verify the result") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.DirectPostPassport.expectedResult.split("&")
                        // nonce
                        resultParsed[0] shouldNotBe null
                        // state
                        // resultWithoutSignedRequest[1] shouldBe expectedWithoutSignedRequest[1]
                    }
                }

                When("Obtain the Access Token using the code obtained in the previous step") {
                    val result = authService.postToken(sessionMock, MOCKS_AUTH.GetToken.request)
                    Then("Verify the result") {
                    }
                }

                When("Obtain the Verifiable Credential using the Access Token obtained on the previous operation") {

                    val request = MOCKS_AUTH.IssueCredential.requestEmployee

                    request.bearerToken = MOCKS_AUTH.IssueCredential.bearer
                    // request.types = credentialTypes
                    val result = issuerService.postCredential(sessionMock, request)
                    Then("Verify the result") {
                        result.credential shouldNotBe null
                        val vc = JwtVerifiableCredential.fromCompactSerialization(result.credential)
                        vc shouldNotBe null
                    }
                }

                mockWebServer.shutdown()
            }

            And("End user wants a keycloak token") {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                    CREDENTIAL_TYPES.EBSI_SAME_IN_TIME
                )

                val keycloakClientData = OpenIdClientData(
                    ClientType.KEYCLOAK,
                    "http://localhost:8080/auth/realms/master",
                    "keycloak-client",
                    "client-secret",
                    "http://localhost:8080/auth/realms/master/protocol/openid-connect/auth",
                    "keycloak"
                )

                val keycloakClientDataString = Json.encodeToString(keycloakClientData)
                clientRepository.add(keycloakClientData.clientId, keycloakClientDataString)

                When("Obtain a Credential Offer") {

                    val request = MOCKS_AUTH.CredentialOffer.requestKeycloak
                    val result = issuerService.initiate(sessionMock, request).toString()
                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            val offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.authOffer
                            }
                        }
                    }
                }

                When("Obtain an Authorisation") {

                    val request = MOCKS_AUTH.Authorisation.requestKeyCloak
                    val result = authService.getAuthorize(sessionMock, request)

                    When("Obtain the Auth request_uri") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResultKeyCloak.split("&")
                        Then("Verify the authorise request uri is valid") {
                            resultParsed shouldBeValidRequestUri expectedParsed
                        }
//                        When("Obtain a Authorisation object using the request_uri") {
//                            val auth = authService.getRequestURI(resultParsed[4].split("%2F").last())
//                            Then("Verify the result") {
//                                auth shouldNotBe null
//                            }
//                        }
                    }
                }

                When("Authenticate the end user via direct_post") {
                    val result = authService.postDirectPost(sessionMock, MOCKS_AUTH.DirectPost.requestKeycloak)
                    Then("Verify the result") {
                        val resultParsed = result.split("&")
                        val expectedParsed = MOCKS_AUTH.DirectPost.expectedResultKeycloak.split("&")
                        // nonce
                        resultParsed[0] shouldBe expectedParsed[0]
                        // state
                        // resultWithoutSignedRequest[1] shouldBe expectedWithoutSignedRequest[1]
                    }
                }

                When("Obtain the Access Token using the code obtained in the previous step") {
                    val result = authService.postToken(sessionMock, MOCKS_AUTH.GetToken.requestKeycloak)
                    Then("Verify the result") {
                        result shouldNotBe null
                    }
                }
            }
        }

        Given("End User is already authenticated") {
            var offer: CredentialOfferResponse? = null
            And("End user wants a " + CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH) {
                credentialTypes = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                    CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH
                )

                When("Obtain a Credential Offer") {
                    val request = MOCKS_AUTH.CredentialOffer.preAuthRequest
                    request.credentialType = credentialTypes.last()
                    val result = issuerService.initiate(sessionMock, request).toString()
                    When("End user obtains the credential offer uri") {
                        Then("Verify the offer uri") {
                            result shouldBe contain(MOCKS_AUTH.CredentialOffer.expectedResult)
                        }
                        When("Obtain a Credential Offer from the offer_uri") {
                            offer = issuerService.getCredentialOfferByID(sessionMock, result.split("%2F").last())
                            Then("Verify the credential Offer is correct") {
                                offer!! shouldBeCredentialOffer MOCKS_AUTH.CredentialOffer.preAuthOffer
                            }
                        }
                    }
                }

//                When("Obtain an Authorisation") {
//
//                    val request = MOCKS_AUTH.Authorisation.request
//
//                    val result = authService.getAuthorize(sessionMock, request)
//
//                    When("Obtain the Auth request_uri") {
//                        val resultParsed = result.split("&")
//                        val expectedParsed = MOCKS_AUTH.Authorisation.expectedResult.split("&")
//                        Then("Verify the authorise request uri is valid") {
//                            resultParsed shouldBeValidRequestUri expectedParsed
//                        }
//                        When("Obtain a Authorisation object using the request_uri") {
//
//                            val auth = authService.getRequestURI(sessionMock, resultParsed[7].split("%2F").last())
//                            Then("Verify the result") {
//                                auth shouldNotBe null
//                                // val tt = SignedJWT.parse(auth)
//                                // tt.payload.toJSONObject().get("nonce") shouldBe NONCE
//                            }
//                        }
//                    }
//                }

                When("Obtain the Access Token pin code ") {
                    val tokenRequest = TokenRequest(
                        clientId = MOCKS_AUTH.IssueCredential.did,
                        grantType = GRAN_TYPE.PRE_AUTH_CODE,
                        userPin = getUserPin(MOCKS_AUTH.IssueCredential.did),
                        preAuthorizedCode = offer?.grants?.preAuthorizedCode?.preAuthorizedCode
                    )
                    val result = authService.postToken(sessionMock, tokenRequest)
                    Then("Verify the result") {
                        result shouldNotBe null
                        result.accessToken shouldNotBe null
                    }
                }

                When("Obtain the Verifiable Credential using the Access Token obtained on the previous operation") {
                    val request = MOCKS_AUTH.IssueCredential.request
                    request.bearerToken = MOCKS_AUTH.IssueCredential.bearer
                    // request.types = credentialTypes
                    val result = issuerService.postCredential(sessionMock, request)
                    Then("Verify the result") {
                        result.credential shouldNotBe null
                        val vc = JwtVerifiableCredential.fromCompactSerialization(result.credential)
                        vc shouldNotBe null
                    }
                }
            }
        }
    }
})

fun <T> anyObject(): T {
    Mockito.any<T>()
    return uninitialized()
}

fun <CredentialOfferResponse> anyCredentialOffer(): CredentialOfferResponse {
    Mockito.any<CredentialOfferResponse>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T
