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

package com.eviden.bds.rd.uself.agent.bdd.repositories

import com.eviden.bds.rd.uself.agent.AgentApplication
import com.eviden.bds.rd.uself.agent.mocks.TestWebSecurityConfig
import com.eviden.bds.rd.uself.agent.models.entities.vc.VCDAO
import com.eviden.bds.rd.uself.agent.services.repositories.tm.vcregistry.VCRepositoryImp
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialResponse
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
    properties = [
        "uself.security.basic.username=uself-agent",
        "uself.security.basic.password=uself-agent-password",
        "uself.schemaExceptions=123",
        "uself.loadDir=data/",
        "uself.version=1.0.0",
        "uself.did.method=DID_WEB",
        "uself.tir.id=",
        "uself.tir.sig-key=",
        "uself.tir.eth-key=",
        "uself.preload=ebsiCredential",
        "uself.server=http://localhost",
        "uself.authentic-source-url=http://luself-authentic-source",
        "uself.issuer=https://uself-file-server.dev5.ari-bip.eu",
        "uself.openid-idp-server=http://keycloak:8080/realms/master/broker/uself/endpoint",
        "uself.openid-client-id=uself-agent-client",
        "uself.openid-client-secret=password",
        "uself.openid-client-gui=http://uself-agent-web:4200/present",
        "uself.openid-client-provider=uself",
        "uself.openid-client-token=http://keycloak:8080/realms/master/broker/uself/token",
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
        "uself.ebsiApis.initiateURL=https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/initiate-credential-offer",
        "spring.jpa.defer-datasource-initialization=true",
    ]
)
@ContextConfiguration(classes = [AgentApplication::class, TestWebSecurityConfig::class])
class VCRepositoryImpTest(
    @Autowired private val vcDAO: VCDAO
) : BehaviorSpec() {

    val vcRepository = VCRepositoryImp(vcDAO)

    init {
        context("a VCRepositoryImp") {
            vcDAO.deleteAll()

            given("insert method") {
                `when`("a new VC is inserted") {
                    val id = "1"
                    val credentialResponse = CredentialResponse("credential1")

                    then("it should save the VC in the database") {
                        vcRepository.insert(id, credentialResponse)
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf(credentialResponse)
                    }
                }
            }

            given("findByID method") {
                `when`("the VC exists") {
                    val id = "1"
                    val credentialResponse = CredentialResponse("credential1")
                    vcRepository.insert(id, credentialResponse)

                    then("it should return the VC") {
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf(credentialResponse)
                    }
                }

                `when`("the VC does not exist") {
                    val id = "2"

                    then("it should return an empty list") {
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf<CredentialResponse>()
                    }
                }
            }

            given("findAll method") {
                `when`("there are VCs in the database") {
                    val credentialResponse1 = CredentialResponse("credential1")
                    val credentialResponse2 = CredentialResponse("credential2")
                    vcRepository.insert("1", credentialResponse1)
                    vcRepository.insert("2", credentialResponse2)

                    then("it should return a list of VCs") {
                        val result = vcRepository.findAll()
                        result shouldBe mutableListOf(
                            arrayListOf(credentialResponse1),
                            arrayListOf(credentialResponse2)
                        )
                    }
                }

                `when`("there are no VCs in the database") {

                    then("it should return an empty list") {
                        vcDAO.deleteAll()
                        val result = vcRepository.findAll()
                        result shouldBe mutableListOf<ArrayList<CredentialResponse>>()
                    }
                }
            }

            given("delete method") {
                `when`("a VC is deleted") {
                    val id = "1"
                    val credentialResponse = CredentialResponse("credential1")
                    vcRepository.insert(id, credentialResponse)
                    vcRepository.delete(id)

                    then("it should remove the VC from the database") {
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf<CredentialResponse>()
                    }
                }
            }

            given("update method") {
                `when`("a VC is updated without existing") {
                    val id = "1"
                    val updatedCredentialResponse = CredentialResponse("updatedCredential1")
                    vcRepository.update(id, updatedCredentialResponse)

                    then("it should update the VC in the database") {
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf(updatedCredentialResponse)
                    }
                }

                `when`("a VC is updated with one existing") {
                    val id = "1"
                    val credentialResponse = CredentialResponse("credential1")
                    val updatedCredentialResponse = CredentialResponse("updatedCredential1")
                    vcRepository.insert(id, credentialResponse)
                    vcRepository.update(id, updatedCredentialResponse)

                    then("it should update the VC in the database") {
                        val result = vcRepository.findByID(id)
                        result shouldBe arrayListOf(credentialResponse, updatedCredentialResponse)
                    }
                }
            }
        }
    }
}
