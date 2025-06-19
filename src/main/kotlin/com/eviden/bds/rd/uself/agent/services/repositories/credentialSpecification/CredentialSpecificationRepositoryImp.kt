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
package com.eviden.bds.rd.uself.agent.services.repositories.credentialSpecification

import com.eviden.bds.rd.uself.agent.models.entities.credentialsSpecification.CredentialSpecificationDAO
import com.eviden.bds.rd.uself.agent.models.entities.credentialsSpecification.CredentialsSpecification
import com.eviden.bds.rd.uself.common.models.exceptions.ErrorTypes
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import com.eviden.bds.rd.uself.common.services.credentialSpecification.repository.CredentialSpecificationRepository
import org.springframework.stereotype.Service

/**
 * This class is responsible for managing the Credential Specification repository.
 */
@Service
class CredentialSpecificationRepositoryImp(
    private val credentialSpecificationDAO: CredentialSpecificationDAO
) : CredentialSpecificationRepository {

    override fun findById(id: String): String? {
        val result = credentialSpecificationDAO.findById(id)
        if (result.isEmpty) {
            throw OIDCException(ErrorTypes.INVALID_REQUEST , "Credentials Specification not found")
        } else {
            return result.get().credentialSpecification
        }
    }

    override fun insert(id: String, credentialSpecification: String): String {
        val parsed = CredentialsSpecification(id = id, credentialSpecification = credentialSpecification)
        return credentialSpecificationDAO.save(parsed).credentialSpecification
    }

    override fun findAll(): ArrayList<String> {
        val credentialSpec = ArrayList<String>()
        credentialSpecificationDAO.findAll().forEach {
            credentialSpec.add(it.credentialSpecification)
        }
        return credentialSpec
    }

    override fun delete(id: String) {
        credentialSpecificationDAO.deleteById(id)
    }

    override fun update(id: String, credentialSpecification: String): String {
        val parsed = CredentialsSpecification(id = id, credentialSpecification = credentialSpecification)
        return credentialSpecificationDAO.save(parsed).credentialSpecification
    }
}
