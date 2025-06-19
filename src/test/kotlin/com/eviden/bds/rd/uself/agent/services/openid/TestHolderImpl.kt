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
@file:Suppress("NoWildcardImports")

package com.eviden.bds.rd.uself.agent.services.openid

import com.eviden.bds.rd.uself.agent.models.entities.didDoc.DIDDocDAO
import com.eviden.bds.rd.uself.agent.models.entities.keys.KeysDAO
import com.eviden.bds.rd.uself.agent.services.repositories.didDoc.DIDDocRepositoryImp
import com.eviden.bds.rd.uself.agent.services.repositories.kms.KMSRepositoryImp
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialResponse
import com.eviden.bds.rd.uself.common.services.crypto.CryptoService
import com.eviden.bds.rd.uself.common.services.crypto.CryptoServiceImp
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerServiceImp
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.verifier.JWSVerifierFactory
import com.eviden.bds.rd.uself.common.services.did.DIDService
import com.eviden.bds.rd.uself.common.services.did.DIDServiceImp
import com.eviden.bds.rd.uself.common.services.did.method.DIDMethodSingletonFactory
import com.eviden.bds.rd.uself.common.services.holder.HolderService
import com.eviden.bds.rd.uself.common.services.holder.HolderServiceImp
import com.eviden.bds.rd.uself.common.services.httpClient.HTTPClient
import com.eviden.bds.rd.uself.common.services.httpClient.HTTPClientImp
import com.eviden.bds.rd.uself.common.services.kms.KMSService
import com.eviden.bds.rd.uself.common.services.kms.KMSServiceImp
import com.eviden.bds.rd.uself.common.services.kms.repository.KMSRepository
import com.eviden.bds.rd.uself.common.services.tm.ebsi.didRegistry.client.DIDRegistryClientImp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TestHolderImpl {

    private lateinit var kmsService: KMSService
    private lateinit var cryptoService: CryptoService
    private lateinit var http: HTTPClient
    private lateinit var jwtService: JWTSignerService
    private lateinit var jwsVerifierFactory: JWSVerifierFactory
    private lateinit var didService: DIDService
    private lateinit var kmsRepository: KMSRepository
    private lateinit var keysDAO: KeysDAO
    private lateinit var didMethodSingletonFactory: DIDMethodSingletonFactory
    private lateinit var didDocDAO: DIDDocDAO

    // @InjectMocks
    private lateinit var holder: HolderService

    @BeforeEach
    fun setup() {
        // employeeRepository = Mockito.mock(EmployeeRepository.class);
        // employeeService = new EmployeeServiceImpl(employeeRepository);

        kmsRepository = KMSRepositoryImp(keysDAO)
        kmsService = KMSServiceImp(kmsRepository)
        http = HTTPClientImp()
        cryptoService = CryptoServiceImp(kmsService)
        val didDocRepository = DIDDocRepositoryImp(didDocDAO)
        val didRegistryClient = DIDRegistryClientImp(http, "", "")
        didMethodSingletonFactory = DIDMethodSingletonFactory(
            cryptoService,
            didDocRepository,
            didRegistryClient,
            null
        )

        didService = DIDServiceImp(didMethodSingletonFactory, didDocRepository)
        jwsVerifierFactory = JWSVerifierFactory(cryptoService, didService,http)
        jwtService = JWTSignerServiceImp(cryptoService,jwsVerifierFactory)
        holder = HolderServiceImp("", "", "")
    }

    @Test
    fun testHolderGetVCSameInTime() {
        val response = holder.runVCISameInTimeFlow()
        println(response)
    }

    @Test
    fun testHolderGetVCSameDeferred() {
        val response = holder.runVCISameDeferredFlow()
        println(response)
    }

    @Test
    fun testHolderGetVCSamePreAuthorised() {
        val response = holder.runVCICrossPreAuthorisedFlow()
        println(response)
    }

    @Test
    fun tesHolderEBSICompliance() {
        val credentials = arrayListOf<CredentialResponse>()

        val did = holder.getDID()
        println("\t NP DID: $did")

        // 1 . Obtain the Credentials
        // same-device-deferred-credential
        val crSameInTime = holder.runVCISameInTimeFlow()
        credentials.add(crSameInTime)
        println("\t crSameInTime: ${Json.encodeToString(crSameInTime)}")
        // cross-device-deferred-credential
        val crCrossInTime = holder.runVCICrossInTimeFlow()
        credentials.add(crCrossInTime)
        println("\t crCrossInTime:  ${Json.encodeToString(crCrossInTime)}")

        // To simulate same-device-deferred-credential
        val crSameDeferred = holder.runVCISameDeferredFlow()
        credentials.add(crSameDeferred)
        println("\t crSameDeferred: ${Json.encodeToString(crSameDeferred)}")
        // to simulate cross-device-deferred-credential
        val crCrossDeferred = holder.runVCICrossDeferredFlow()
        credentials.add(crCrossDeferred)
        println("\t crCrossDeferred:  ${Json.encodeToString(crCrossDeferred)}")

        // To simulate same-device-pre_authorised-credential
        val crSamePreAuth = holder.runVCISamePreAuthorisedFlow()
        credentials.add(crSamePreAuth)
        println("\t crSamePreAuth:  ${Json.encodeToString(crSamePreAuth)}")

        // cross-device-pre_authorised-credential
        val cdCrossPreAuth = holder.runVCICrossPreAuthorisedFlow()
        credentials.add(cdCrossPreAuth)
        println("\t cdCrossPreAuth:  ${Json.encodeToString(cdCrossPreAuth)}")

        val result = holder.runVPFlow(credentials)
        println("\t crQualificationCredential:  ${Json.encodeToString(result)}")
    }
}
