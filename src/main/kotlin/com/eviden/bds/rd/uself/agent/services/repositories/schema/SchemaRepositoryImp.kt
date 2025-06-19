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
package com.eviden.bds.rd.uself.agent.services.repositories.schema

import com.eviden.bds.rd.uself.agent.models.entities.schema.Schema
import com.eviden.bds.rd.uself.agent.models.entities.schema.SchemaDAO
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.schema.repository.SchemaRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

/**
 * This class is responsible for managing the schema repository.
 */
@Service
class SchemaRepositoryImp(private val schemaDAO: SchemaDAO) : SchemaRepository {

    /**
     * This method is responsible for finding the schema by id.
     * @param id The id.
     * @return The schema.
     */
    override fun findById(id: String): String? {
        return schemaDAO.findById(id).getOrNull()?.schema
    }

    /**
     * This method is responsible for saving the schema.
     * @param schemaRequest The schema request.
     * @return The schema.
     */
    override fun insert(schema: String): String {
        val schemaBody = Schema(id = createUUID(), schema = schema)
        return schemaDAO.save(schemaBody).id
    }
}
