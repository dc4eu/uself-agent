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

package com.eviden.bds.rd.uself.agent.services.repositories.kms

import com.eviden.bds.rd.uself.agent.models.entities.keys.Keys
import com.eviden.bds.rd.uself.agent.models.entities.keys.KeysDAO
import com.eviden.bds.rd.uself.common.services.kms.repository.KMSRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class KMSRepositoryImp(private val keysDAO: KeysDAO) : KMSRepository {

    override fun findAll(): List<String> {
        return keysDAO.findAll().map { it.jwkString }
    }

    override fun findByKid(kid: String): String? = keysDAO.findById(kid).getOrNull()?.jwkString

    override fun insert(kid: String, key: String) {
        keysDAO.save(Keys(id = kid, jwkString = key))
    }

    override fun delete(kid: String) {
        keysDAO.delete(Keys(id = kid, jwkString = ""))
    }

    override fun update(kid: String, key: String) {
        keysDAO.save(Keys(id = kid, jwkString = key))
    }
}
