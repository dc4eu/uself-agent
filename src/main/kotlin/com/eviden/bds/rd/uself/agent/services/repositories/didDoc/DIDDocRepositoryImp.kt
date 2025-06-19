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

package com.eviden.bds.rd.uself.agent.services.repositories.didDoc

import com.eviden.bds.rd.uself.agent.models.entities.didDoc.DIDDoc
import com.eviden.bds.rd.uself.agent.models.entities.didDoc.DIDDocDAO
import com.eviden.bds.rd.uself.common.services.did.repository.DIDDocRepository
import org.springframework.stereotype.Service

@Service
class DIDDocRepositoryImp(private val didDocDAO: DIDDocDAO) : DIDDocRepository {

    override fun findAll(): List<String> {
        return didDocDAO.findAll().map { it.didDocJson }
    }

    override fun findByDID(did: String): String? {
        val didDoc = didDocDAO.findById(did)
        return if (didDoc.isEmpty) null else didDoc.get().didDocJson
    }

    override fun insert(did: String, didDocJson: String) {
        didDocDAO.save(DIDDoc(id = did, didDocJson = didDocJson))
    }

    override fun delete(did: String) {
        didDocDAO.delete(DIDDoc(id = did, didDocJson = ""))
    }

    override fun update(did: String, didDocJson: String) {
        didDocDAO.save(DIDDoc(id = did, didDocJson = didDocJson))
    }
}
