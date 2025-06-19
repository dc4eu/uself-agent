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
import com.eviden.bds.rd.uself.agent.models.entities.status.StatusDAO
import com.eviden.bds.rd.uself.agent.services.repositories.statusList.StatusListRepositoryImpl
import com.eviden.bds.rd.uself.common.models.STATUS_TYPE
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class StatusListRepositoryImplTest(
    @Autowired private val statusDAO: StatusDAO
) : BehaviorSpec() {
    private val statusRepository = StatusListRepositoryImpl(statusDAO)

    init {
        context("a status repository") {
            statusDAO.deleteAll()

            given("getEncodedStatusList to initialize") {
                When("Create the status lists") {
                    // val id = "1"
                    val revStatusList = statusRepository.getEncodedStatusList(STATUS_TYPE.REVOCATION)
                    val susStatusList = statusRepository.getEncodedStatusList(STATUS_TYPE.SUSPENSION)
                    then("The lists shoudn't be null and all the status indexes are false") {
                        revStatusList shouldNotBe null
                        susStatusList shouldNotBe null

                        statusRepository.isRevoked(0) shouldBe false
                        statusRepository.isRevoked(1) shouldBe false
                        statusRepository.isRevoked(2) shouldBe false
                        statusRepository.isRevoked(3) shouldBe false
                        statusRepository.isRevoked(4) shouldBe false

                        statusRepository.isSuspended(0) shouldBe false
                        statusRepository.isSuspended(1) shouldBe false
                        statusRepository.isSuspended(2) shouldBe false
                        statusRepository.isSuspended(3) shouldBe false
                        statusRepository.isSuspended(4) shouldBe false
                    }
                }
            }

            given("revoke an index") {
                When("the index 2 is revoked") {
                    statusRepository.revoke(2)
                    then("the index 2 should be true(revoked)") {
                        statusRepository.isRevoked(0) shouldBe false
                        statusRepository.isRevoked(1) shouldBe false
                        statusRepository.isRevoked(2) shouldBe true
                        statusRepository.isRevoked(3) shouldBe false
                        statusRepository.isRevoked(4) shouldBe false
                    }
                }
            }

            given("suspend some indexes") {
                When("The index 3 and 1 are suspended") {
                    statusRepository.suspend(3)
                    statusRepository.suspend(1)
                    then("the index 1 and 3 should be true(suspended)") {
                        statusRepository.isSuspended(0) shouldBe false
                        statusRepository.isSuspended(1) shouldBe true
                        statusRepository.isSuspended(2) shouldBe false
                        statusRepository.isSuspended(3) shouldBe true
                        statusRepository.isSuspended(4) shouldBe false
                    }
                }
            }

            given("restore a index") {
                When("The index 3 is restored") {
                    statusRepository.restore(3)
                    then("only the index 1 is true(suspended)") {
                        statusRepository.isSuspended(0) shouldBe false
                        statusRepository.isSuspended(1) shouldBe true
                        statusRepository.isSuspended(2) shouldBe false
                        statusRepository.isSuspended(3) shouldBe false
                        statusRepository.isSuspended(4) shouldBe false
                    }
                }
            }
        }
    }
}
