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
import com.eviden.bds.rd.uself.agent.models.entities.credentialsSpecification.CredentialSpecificationDAO
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import com.eviden.bds.rd.uself.common.services.credentialSpecification.repository.CredentialSpecificationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
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
class CredentialSpecificationRepositoryImpTest(
    private val credentialSpecificationDAO: CredentialSpecificationDAO,
    private val credentialSpecificationRepository: CredentialSpecificationRepository
) : KoinTest, BehaviorSpec() {
    init {

        Context("an CredentialSpecificationRepositoryImp") {

            credentialSpecificationDAO.deleteAll()
            Given("insert method") {
                `when`("a new authentic source is inserted") {
                    val id = "1"
                    val source = "credential1"

                    then("it should save the authentic source in the database") {
                        val result = credentialSpecificationRepository.insert(id, source)
                        result shouldBe source
                    }
                }
            }

            Given("findById method") {
                `when`("the authentic source exists") {
                    val id = "1"

                    then("it should return the authentic source") {
                        val result = credentialSpecificationRepository.findById(id)
                        result shouldBe "credential1"
                    }
                }

                `when`("the authentic source does not exist") {
                    val id = "2"

                    then("it should return null") {
                        shouldThrow<OIDCException> {
                            credentialSpecificationRepository.findById(id)
                        }.message shouldBe "Credentials Specification not found"
                    }
                }
            }

            Given("findAll method") {
                `when`("there are authentic sources in the database") {
                    val credentials = listOf("credential1", "credential2")

                    then("it should return a list of authentic sources") {
                        credentialSpecificationRepository.insert("2", "credential2")
                        val result = credentialSpecificationRepository.findAll()
                        result shouldBe credentials
                    }
                }

                `when`("there are not authentic sources in the database") {

                    then("it should return a null list") {
                        credentialSpecificationDAO.deleteAll()
                        val result = credentialSpecificationRepository.findAll()
                        result shouldBe emptyList()
                    }
                }
            }
        }
    }
}
