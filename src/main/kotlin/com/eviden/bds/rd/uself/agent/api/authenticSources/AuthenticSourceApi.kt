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

package com.eviden.bds.rd.uself.agent.api.authenticSources

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Interface for the controller to store and  obtain a JSON Authentic Sources
 */
@Tag(
    name = "Authentic Sources API",
    description = "Authentic Source API provides the functionality to store and obtain a the user information "
)
@SecurityRequirement(name = "bearerAuth")
interface AuthenticSourceApi {

    /**
     * Interface that obtains the list of Authentic Source
     */
    @Operation(
        summary = "Obtain a list of Authentic Sources",
        description = """This method provides the list of the Authentic Sources information""",
        tags = ["Authentic Sources API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = AuthenticSource::class)
                        )
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
    @GetMapping(path = ["/authentic-source"], produces = ["application/json"])
    fun getAuthenticSources(): ResponseEntity<ArrayList<AuthenticSource>>

    /**
     * Interface that obtains a certain Authentic Source
     * @param id the id of the Authentic Source
     */
    @Operation(
        summary = "Obtain an Authentic Source",
        description = """This method obtain an Authentic Source information""",
        tags = ["Authentic Sources API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AuthenticSource::class)
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
    @GetMapping(path = ["/authentic-source/{id}"], produces = ["application/json"])
    fun getAuthenticSource(@PathVariable id: String): ResponseEntity<AuthenticSource>

    /**
     * Interface of a method that creates an Authentic Source
     * @param bearerToken the token to access to create an Authentic Source
     * @param body the Authentic Source to store
     */
    @Operation(
        summary = "Creation of a JSON Authentic Source",
        description = """This method creates a JSON Authentic Source""",
        tags = ["Authentic Sources API"]
    )
    @PostMapping(path = ["/authentic-source"], consumes = ["application/json"])
    fun postAuthenticSource(
        //       @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: AuthenticSource
    ): ResponseEntity<AuthenticSource>

    /**
     * Interface of a method that deletes an Authentic Source
     * @param id the id of the Authentic Source
     * @return the id of the Authentic Source
     * @throws Exception if the Authentic Source is being used by a Credential Specification
     * @throws Exception if the Authentic Source is not found
     */
    @Operation(
        summary = "Delete an Authentic Source",
        description = """This method deletes an Authentic Source""",
        tags = ["Authentic Sources API"]
    )
    @DeleteMapping(path = ["/authentic-source/{id}"])
    fun deleteAuthenticSource(@PathVariable id: String): ResponseEntity<Unit>

    /**
     * Interface of a method that updates an Authentic Source
     * @param id the id of the Authentic Source
     * @param body the Authentic Source to store
     */
    @Operation(
        summary = "Update an Authentic Source",
        description = """This method updates an Authentic Source""",
        tags = ["Authentic Sources API"]
    )
    @PutMapping(path = ["/authentic-source/{id}"], consumes = ["application/json"])
    fun updateAuthenticSource(
        @PathVariable id: String,
        @RequestBody body: AuthenticSource
    ): ResponseEntity<AuthenticSource>
}
