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

@file:Suppress("MaxLineLength", "NoWildcardImports", "LongParameterList")

package com.eviden.bds.rd.uself.agent.api.schema

import SchemaRequest
import com.eviden.bds.rd.uself.agent.models.Problem
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Interface for the controller to store and  obtain a JSON schema
 */
// @Hidden
@Tag(name = "Schema API", description = "Schema API")
@SecurityRequirement(name = "bearerAuth")
interface SchemaController {
    /**
     * Interface that obtains a certain JSON Schema
     * @param id the id of the JSON Schema
     */
    @Operation(
        summary = "Obtain a JSON Schema",
        description = """This method obtain a JSON Schema based on the id generated when was created""",
        tags = ["Schema API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SchemaRequest::class)
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    @GetMapping(path = ["/schema/{id}"], produces = ["application/json"])
    fun getSchema(@PathVariable id: String): ResponseEntity<String>

    /**
     * Interface of a method that creates a JSON schema
     * @param bearerToken the token to access to create a schema
     * @param body the schema to store
     */
    @Operation(
        summary = "Creation of a JSON Schema",
        description = """This method creates a JSON Schema""",
        tags = ["Schema API"]
    )
    @PostMapping(path = ["/schema"], consumes = ["application/json"])
    fun postSchema(
        @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: SchemaRequest
    ): ResponseEntity<String>
}
