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

package com.eviden.bds.rd.uself.agent.services.repositories.tm.vcregistry

import com.eviden.bds.rd.uself.agent.models.entities.vc.VC
import com.eviden.bds.rd.uself.agent.models.entities.vc.VCDAO
import com.eviden.bds.rd.uself.common.models.openid.issuer.CredentialResponse
import com.eviden.bds.rd.uself.common.services.tm.vcrepository.VCRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class VCRepositoryImp(private val dao: VCDAO) : VCRepository {

    override fun findAll(): MutableList<ArrayList<CredentialResponse>> {
        return dao.findAll().map {
            Json.decodeFromString(it.credentialsResponse) as ArrayList<CredentialResponse>
        }.toMutableList()
    }

    override fun findByID(id: String): ArrayList<CredentialResponse> {
        val list = dao.findAllById(listOf(id))
        if (list.isEmpty()) {
            return arrayListOf()
        }
        return Json.decodeFromString(list.first().credentialsResponse)
    }
    override fun insert(id: String, credentialResponse: CredentialResponse) {
        dao.save(VC(id = id, credentialsResponse = Json.encodeToString(arrayListOf(credentialResponse))))
    }

    override fun delete(id: String) {
        dao.delete(VC(id = id))
    }

    override fun update(id: String, credentialResponse: CredentialResponse) {
        if (dao.findAllById(listOf(id)).isEmpty()) {
            dao.save(VC(id = id, credentialsResponse = Json.encodeToString(arrayListOf(credentialResponse))))
            return
        } else {
            val vc = dao.findAllById(listOf(id)).first()
            val list = Json.decodeFromString<ArrayList<CredentialResponse>>(vc.credentialsResponse)
            list.add(credentialResponse)
            dao.save(VC(id = id, credentialsResponse = Json.encodeToString(list)))
        }
    }
}
