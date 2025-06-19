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

package com.eviden.bds.rd.uself.agent.services.repositories.tm.treregistry

import com.eviden.bds.rd.uself.agent.models.entities.tre.TRE
import com.eviden.bds.rd.uself.agent.models.entities.tre.TREDAO
import com.eviden.bds.rd.uself.common.models.ebsi.registries.TrustedRegistryEntry
import com.eviden.bds.rd.uself.common.services.tm.trerepository.TRERepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service

@Single
@Service
class TRERepositoryImp(private val dao: TREDAO) : TRERepository {

    override fun findAll(): List<TrustedRegistryEntry> {
        return dao.findAll().map { Json.decodeFromString(it.tre) }
    }

    override fun findByDID(did: String): TrustedRegistryEntry? {
        val list = dao.findAllById(listOf(did))
        if (list.isEmpty()) {
            return null
        }
        return Json.decodeFromString(list.first().tre)
    }
    override fun insert(did: String, tre: TrustedRegistryEntry) {
        dao.save(TRE(id = did, tre = Json.encodeToString(tre)))
    }

    override fun delete(did: String) {
        dao.delete(TRE(id = did))
    }

    override fun update(did: String, tre: TrustedRegistryEntry) {
        dao.save(TRE(id = did, tre = Json.encodeToString(tre)))
    }
}
