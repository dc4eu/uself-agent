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

package com.eviden.bds.rd.uself.agent.services.repositories.openIdClient

import com.eviden.bds.rd.uself.agent.models.entities.openIdClient.OpenIdClient
import com.eviden.bds.rd.uself.agent.models.entities.openIdClient.OpenIdClientDAO
import com.eviden.bds.rd.uself.common.services.openIdClients.repository.ClientRepository
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service

@Service
@Single
class OpenIdClientRepositoryImp(private val openIdClientDAO: OpenIdClientDAO) : ClientRepository {

    override fun get(id: String): String? {
        val result = openIdClientDAO.findById(id)
        return if (result.isEmpty) {
            null
        } else {
            result.get().openIdClientData
        }
    }

    override fun add(id: String, client: String) {
        val parsed = OpenIdClient(id = id, openIdClientData = client)
        openIdClientDAO.save(parsed).openIdClientData
    }

    override fun getAll(): Map<String, String> {
        val openIdClients = mutableMapOf<String, String>()
        openIdClientDAO.findAll().forEach {
            openIdClients[it.id] = it.openIdClientData
        }
        return openIdClients
    }

    override fun remove(id: String) {
        openIdClientDAO.deleteById(id)
    }

    override fun update(id: String, client: String) {
        val parsed = OpenIdClient(id = id, openIdClientData = client)
        openIdClientDAO.save(parsed).openIdClientData
    }
}
