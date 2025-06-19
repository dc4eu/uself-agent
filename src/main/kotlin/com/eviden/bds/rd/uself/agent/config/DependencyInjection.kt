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

package com.eviden.bds.rd.uself.agent.config

import com.eviden.bds.rd.uself.common.services.CommonModule
import com.eviden.bds.rd.uself.common.services.authenticSource.repository.AuthenticSourceRepository
import com.eviden.bds.rd.uself.common.services.credentialSpecification.repository.CredentialSpecificationRepository
import com.eviden.bds.rd.uself.common.services.did.repository.DIDDocRepository
import com.eviden.bds.rd.uself.common.services.issuedCredentials.repository.IssuedCredentialsRepository
import com.eviden.bds.rd.uself.common.services.kms.repository.KMSRepository
import com.eviden.bds.rd.uself.common.services.openIdClients.repository.ClientRepository
import com.eviden.bds.rd.uself.common.services.schema.repository.SchemaRepository
import com.eviden.bds.rd.uself.common.services.status.repository.StatusListRepository
import com.eviden.bds.rd.uself.common.services.tm.trerepository.TRERepository
import com.eviden.bds.rd.uself.common.services.tm.vcrepository.VCRepository
import com.eviden.bds.rd.uself.common.services.tracker.repository.ActivityRepository
import jakarta.annotation.PostConstruct
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Class that serves as the configuration for dependency injection using Koin.
 * This class defines the repositories and environment variables used across the application.
 * The @Autowired annotations are used to inject the necessary JPA repositories, ensuring
 * that each repository can interact with the database and manage the entities corresponding
 * to its domain.
 *
 */
@Component
class DependencyInjection {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var kmsRepository: KMSRepository

    @Autowired
    lateinit var didDocRepository: DIDDocRepository

    @Autowired
    lateinit var treRepository: TRERepository

    @Autowired
    lateinit var schemaRepository: SchemaRepository

    @Autowired
    lateinit var vcRepository: VCRepository

    @Autowired
    lateinit var activityRepository: ActivityRepository

    @Autowired
    lateinit var authenticSourceRepository: AuthenticSourceRepository

    /**
     * The `envPropertiesConverter` class is used to convert the variables from the `application.yml`
     * file into a Koin module.
     */
    @Autowired
    lateinit var envPropertiesConverter: EnvPropertiesConverter

    @Autowired
    lateinit var credentialSpecificationRepository: CredentialSpecificationRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var issuedCredentialsRepository: IssuedCredentialsRepository

    @Autowired
    lateinit var statusListRepository: StatusListRepository

    /**
     * The `@PostConstruct` method is necessary to wait for the environment initialization before performing
     * the injection.
     */
    @PostConstruct
    fun init() {
        logger.info("Initialization Koin DI for Agent")

        val agentModule = module {
            single<KMSRepository> { kmsRepository }
            single<DIDDocRepository> { didDocRepository }
            single<TRERepository> { treRepository }
            single<SchemaRepository> { schemaRepository }
            single<VCRepository> { vcRepository }
            single<CredentialSpecificationRepository> { credentialSpecificationRepository }
            single<ClientRepository> { clientRepository }
            single<ActivityRepository> { activityRepository }
            single<AuthenticSourceRepository> { authenticSourceRepository }
            single<IssuedCredentialsRepository> { issuedCredentialsRepository }
            single<StatusListRepository> { statusListRepository }
        }
        startKoin {
            modules(CommonModule().module)
            modules(envPropertiesConverter.envToModule())
            modules(agentModule)
        }
    }
}
