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
import com.eviden.bds.rd.uself.agent.models.entities.activity.ActivityDAO
import com.eviden.bds.rd.uself.agent.services.repositories.activity.ActivityRepositoryImpl
import com.eviden.bds.rd.uself.common.models.openid.tracker.Activity
import com.eviden.bds.rd.uself.common.models.openid.tracker.TrackerType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
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
class ActivityRepositoryImpTest(
    private val activityDAO: ActivityDAO
) : BehaviorSpec() {

    private val activityRepository = ActivityRepositoryImpl(activityDAO)
    init {
        Context("ActivityRepository operations") {

            given("an empty database") {
                activityDAO.deleteAll()

                When("fetching all activities") {
                    val activities = activityRepository.getActivities()

                    then("it should return an empty list") {
                        activities shouldBe emptyList()
                    }
                }

                When("inserting a new activity") {
                    val action = "Login"
                    val actionDetailValue = "User logged in successfully"
                    val classNameValue = "UserController"
                    val trackerTypeValue = TrackerType.ERROR

                    activityRepository.insertActivity(action, actionDetailValue, classNameValue, trackerTypeValue)

                    then("the activity should be present in the database") {
                        val savedActivities = activityRepository.getActivities()
                        println(savedActivities.toString())
                        savedActivities.size shouldBe 1
                        with(savedActivities.first()) {
                            actionName shouldBe action
                            actionDetail shouldBe actionDetailValue
                            className shouldBe classNameValue
                            trackerType shouldBe trackerTypeValue.name
                        }
                    }
                }
                activityRepository.deleteActivity(1)
            }

            given("a database with one activity") {
                activityDAO.deleteAll()

                When("deleting the activity by id") {
                    activityRepository.insertActivity(
                        "Register",
                        "User registered",
                        "RegistrationService",
                        TrackerType.WARN
                    )

                    println(activityRepository.getActivities().toString())
                    then("the database should be empty") {
                        activityRepository.getActivities().size shouldBe 1
                    }
                }

                When("updating the activity") {
                    val activity = activityRepository.getActivities().first()
                    val updatedActivity = Activity(
                        id = activity.id,
                        actionName = "Updated detail",
                        actionDetail = activity.actionDetail,
                        className = activity.className,
                        trackerType = activity.trackerType
                    )
                    activityRepository.updateActivity(updatedActivity)

                    then("the activity should reflect the updated detail") {
                        updatedActivity shouldBe activityRepository.getActivities().first()
                    }
                }
            }
        }
    }
}
