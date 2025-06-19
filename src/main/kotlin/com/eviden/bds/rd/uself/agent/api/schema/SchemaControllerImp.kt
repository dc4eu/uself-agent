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

@file:Suppress("LoggingSimilarMessage", "NoWildcardImports")

package com.eviden.bds.rd.uself.agent.api.schema

import SchemaRequest
import com.eviden.bds.rd.uself.common.services.schema.SchemaService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
/**
 * Class that implements the SchemaController interface
 * @param schemaService the service to store and obtain a JSON schema
 * @constructor creates a SchemaControllerImp
 */
class SchemaControllerImp: SchemaController, KoinComponent {

    private val tracker: Tracker = Tracker(this::class.java)
    private val schemaUrl = "/schema"
    private val schemaService: SchemaService by inject(SchemaService::class.java)

    /**
     * Method that obtains a certain JSON Schema
     * @param id the id of the JSON Schema
     * @return the JSON Schema
     */
    override fun getSchema(id: String) : ResponseEntity<String> {
        tracker.infoGET("$schemaUrl/$id", "Obtain a JSON Schema")
        val schema = schemaService.getSchema(id)
        tracker.infoRESP200("$schemaUrl/$id", schema)
        return ResponseEntity.ok(schema)
    }

    /**
     * Method that creates a JSON schema
     * @param bearerToken the token to access to create a schema
     * @param body the schema to store
     * @return the id of the schema
     */
    override fun postSchema(@RequestHeader("Authorization") bearerToken: String, @RequestBody body: SchemaRequest) : ResponseEntity<String> {

        tracker.infoPOST(schemaUrl, body)
        val savedSchema = schemaService.postSchema(bearerToken,body)
        tracker.infoRESP200(schemaUrl, savedSchema)
        return ResponseEntity.ok(savedSchema)
    }

}
