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
import com.eviden.bds.rd.uself.agent.models.entities.keys.KeysDAO
import com.eviden.bds.rd.uself.agent.services.repositories.kms.KMSRepositoryImp
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
@ContextConfiguration(classes =[AgentApplication::class, TestWebSecurityConfig::class])
class KMSRepositoryImpTest(
    @Autowired private val keysDAO: KeysDAO
) : BehaviorSpec() {

    val kmsRepository = KMSRepositoryImp(keysDAO)

    init {
        context("a KMSRepositoryImp") {
            keysDAO.deleteAll()
            given("insert method") {
                `when`("a new key is inserted") {
                    val kid = "1"
                    val key = "key1"

                    then("it should save the key in the database") {
                        kmsRepository.insert(kid, key)
                    }
                }
            }

            given("findByKid method") {
                `when`("the key exists") {
                    val kid = "1"

                    then("it should return the key") {
                        val result = kmsRepository.findByKid(kid)
                        result shouldBe "key1"
                    }
                }

                `when`("the key does not exist") {
                    val kid = "2"

                    then("it should return null") {
                        val result = kmsRepository.findByKid(kid)
                        result shouldBe null
                    }
                }
            }

            given("findAll method") {
                `when`("there are keys in the database") {
                    val keys = listOf("key1", "key2")

                    then("it should return a list of keys") {
                        kmsRepository.insert("2", "key2")
                        val result = kmsRepository.findAll()
                        result shouldBe keys
                    }
                }

                `when`("there are no keys in the database") {

                    then("it should return an empty list") {
                        keysDAO.deleteAll()
                        val result = kmsRepository.findAll()
                        result shouldBe emptyList()
                    }
                }
            }

            given("delete method") {
                `when`("a key is deleted") {
                    val kid = "1"
                    val key = "key1"
                    kmsRepository.insert(kid, key)
                    kmsRepository.delete(kid)

                    then("it should remove the key from the database") {
                        val result = kmsRepository.findByKid(kid)
                        result shouldBe null
                    }
                }
            }

            given("update method") {
                `when`("a key is updated") {
                    val kid = "1"
                    val key = "key1"
                    val updatedKey = "updatedKey1"
                    kmsRepository.insert(kid, key)
                    kmsRepository.update(kid, updatedKey)

                    then("it should update the key in the database") {
                        val result = kmsRepository.findByKid(kid)
                        result shouldBe updatedKey
                    }
                }
            }
        }
    }
}

