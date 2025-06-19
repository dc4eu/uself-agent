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
package com.eviden.bds.rd.uself.agent.services.repositories.authenticSource

import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.agent.models.entities.authenticSource.AuthenticSourceDAO
import com.eviden.bds.rd.uself.common.models.credentialSpecification.CredentialSpec
import com.eviden.bds.rd.uself.common.services.authenticSource.repository.AuthenticSourceRepository
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

/**
 * This class is responsible for managing the Authentic Source repository.
 */
@Service
class AuthenticSourceRepositoryImp(private val authenticSourceDAO: AuthenticSourceDAO) : AuthenticSourceRepository {

    override fun findById(id: String): String? {
        return authenticSourceDAO.findById(id).getOrNull()?.authenticSource
    }

    override fun insert(id: String, authenticSource: String): String {
        val parsed = com.eviden.bds.rd.uself.agent.models.entities.authenticSource.AuthenticSource(
            id = id,
            authenticSource = authenticSource
        )
        return authenticSourceDAO.save(parsed).authenticSource
    }

    override fun findAll(): ArrayList<String> {
        val authenticSources = ArrayList<String>()
        authenticSourceDAO.findAll().forEach {
            authenticSources.add(it.authenticSource)
        }
        return authenticSources
    }

    override fun delete(id: String): String {
        val authenticSource = authenticSourceDAO.findById(id).get().authenticSource
        authenticSourceDAO.deleteById(id)
        return "Deleted: $authenticSource"
    }

    override fun findByOpenIdClientId(openIdClientId: String): ArrayList<String> {
        val authenticSources = ArrayList<String>()

        authenticSourceDAO.findAll().forEach {
            val authenticSource = Json.decodeFromString<AuthenticSource>(it.authenticSource)
            if (authenticSource.openIdClientId == openIdClientId) {
                authenticSources.add(it.authenticSource)
            }
        }
        return authenticSources
    }

    override fun update(id: String, authenticSource: String): String {
        val parsed = com.eviden.bds.rd.uself.agent.models.entities.authenticSource.AuthenticSource(
            id = id,
            authenticSource = authenticSource
        )
        return authenticSourceDAO.save(parsed).authenticSource
    }
}
