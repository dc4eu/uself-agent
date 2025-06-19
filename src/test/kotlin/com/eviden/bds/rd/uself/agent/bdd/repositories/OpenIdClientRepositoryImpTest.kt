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
import com.eviden.bds.rd.uself.agent.models.entities.openIdClient.OpenIdClientDAO
import com.eviden.bds.rd.uself.agent.services.repositories.openIdClient.OpenIdClientRepositoryImp
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
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
class OpenIdClientRepositoryImpTest(
    @Autowired private val openIdClientDAO: OpenIdClientDAO
) : BehaviorSpec() {

    val openIdClientRepository = OpenIdClientRepositoryImp(openIdClientDAO)

    init {
        context("an OpenIdClientRepositoryImp") {
            openIdClientDAO.deleteAll()

            given("add method") {
                `when`("a new OpenId client is added") {
                    val id = "1"
                    val client = "client1"

                    then("it should save the OpenId client in the database") {
                        openIdClientRepository.add(id, client)
                        val result = openIdClientRepository.get(id)
                        result shouldBe client
                    }
                }
            }

            given("get method") {
                `when`("the OpenId client exists") {
                    val id = "1"
                    val client = "client1"
                    openIdClientRepository.add(id, client)

                    then("it should return the OpenId client") {
                        val result = openIdClientRepository.get(id)
                        result shouldBe client
                    }
                }

                `when`("the OpenId client does not exist") {
                    val id = "2"
                    then("it should be null") {
                        val result = openIdClientRepository.get(id)
                        result shouldBe null
                    }
                }
            }

            given("getAll method") {
                `when`("there are OpenId clients in the database") {
                    val clients = mapOf("1" to "client1", "2" to "client2")
                    openIdClientRepository.add("1", "client1")
                    openIdClientRepository.add("2", "client2")

                    then("it should return a map of OpenId clients") {
                        val result = openIdClientRepository.getAll()
                        result shouldBe clients
                    }
                }

                `when`("there are no OpenId clients in the database") {

                    then("it should return an empty map") {
                        openIdClientDAO.deleteAll()
                        val result = openIdClientRepository.getAll()
                        result shouldBe emptyMap()
                    }
                }
            }

            given("remove method") {
                `when`("an OpenId client is removed") {
                    val id = "1"
                    val client = "client1"
                    openIdClientRepository.add(id, client)
                    openIdClientRepository.remove(id)

                    then("it should remove the OpenId client from the database") {
                        val result = openIdClientRepository.get(id)
                        result shouldBe null
                    }
                }
            }

            given("update method") {
                `when`("an OpenId client is updated") {
                    val id = "1"
                    val client = "client1"
                    val updatedClient = "updatedClient1"
                    openIdClientRepository.add(id, client)
                    openIdClientRepository.update(id, updatedClient)

                    then("it should update the OpenId client in the database") {
                        val result = openIdClientRepository.get(id)
                        result shouldBe updatedClient
                    }
                }
            }
        }
    }
}
