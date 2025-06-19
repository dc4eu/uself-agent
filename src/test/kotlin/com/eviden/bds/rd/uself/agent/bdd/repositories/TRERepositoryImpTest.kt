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
import com.eviden.bds.rd.uself.agent.models.entities.tre.TREDAO
import com.eviden.bds.rd.uself.agent.services.repositories.tm.treregistry.TRERepositoryImp
import com.eviden.bds.rd.uself.common.models.ebsi.registries.TrustedRegistryEntry
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
class TRERepositoryImpTest(
    @Autowired private val treDAO: TREDAO
) : BehaviorSpec() {

    val treRepository = TRERepositoryImp(treDAO)

    init {
        context("a TRERepositoryImp") {
            treDAO.deleteAll()

            given("insert method") {
                `when`("a new TRE is inserted") {
                    val id = "did1"
                    val tre = TrustedRegistryEntry("did1", arrayListOf(), null)

                    then("it should save the TRE in the database") {
                        treRepository.insert(id, tre)
                        val result = treRepository.findByDID(id)
                        result shouldBe tre
                    }
                }
            }

            given("findById method") {
                `when`("the TRE exists") {
                    val id = "did2"
                    val tre = TrustedRegistryEntry("did2", arrayListOf(), null)
                    treRepository.insert(id, tre)

                    then("it should return the TRE") {
                        val result = treRepository.findByDID(id)
                        result shouldBe tre
                    }
                }

                `when`("the TRE does not exist") {
                    val id = "2"

                    then("it should return null") {
                        val result = treRepository.findByDID(id)
                        result shouldBe null
                    }
                }
            }

            given("findAll method") {
                `when`("there are TREs in the database") {
                    val tres =
                        listOf(
                            TrustedRegistryEntry("did1", arrayListOf(), null),
                            TrustedRegistryEntry("did2", arrayListOf(), null)
                        )
                    treRepository.insert("did1", TrustedRegistryEntry("did1", arrayListOf(), null))
                    treRepository.insert("did2", TrustedRegistryEntry("did2", arrayListOf(), null))

                    then("it should return a list of TREs") {
                        val result = treRepository.findAll()
                        result shouldBe tres
                    }
                }

                `when`("there are no TREs in the database") {

                    then("it should return an empty list") {
                        treDAO.deleteAll()
                        val result = treRepository.findAll()
                        result shouldBe emptyList()
                    }
                }
            }

            given("delete method") {
                `when`("a TRE is deleted") {
                    val id = "did1"
                    val tre = TrustedRegistryEntry("did1", arrayListOf(), null)
                    treRepository.insert(id, tre)
                    treRepository.delete(id)

                    then("it should remove the TRE from the database") {
                        val result = treRepository.findByDID(id)
                        result shouldBe null
                    }
                }
            }

            given("update method") {
                `when`("a TRE is updated") {
                    val id = "did1"
                    val tre = TrustedRegistryEntry("did1", arrayListOf(), null)
                    val updatedTRE = TrustedRegistryEntry("did1Updated", arrayListOf(), null)
                    treRepository.insert(id, tre)
                    treRepository.update(id, updatedTRE)

                    then("it should update the TRE in the database") {
                        val result = treRepository.findByDID(id)
                        result shouldBe updatedTRE
                    }
                }
            }
        }
    }
}
