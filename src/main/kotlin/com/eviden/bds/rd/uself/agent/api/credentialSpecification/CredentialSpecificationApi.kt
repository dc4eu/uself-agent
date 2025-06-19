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

package com.eviden.bds.rd.uself.agent.api.credentialSpecification

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.credentialSpecification.CredentialSpec
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
 * Interface for the controller to store and  obtain a JSON Credentials Specifications
 */
@Tag(
    name = "Credentials Specifications API",
    description = "Credentials Specifications API provides the functionality to store and obtain a the user information "
)
@SecurityRequirement(name = "bearerAuth")
interface CredentialSpecificationApi {



    /**
     * Interface that obtains a certain Credentials Specification
     * @param id the id of the Credentials Specification
     */
    @Operation(
        summary = "Obtain a Credentials Specification",
        description = """This method obtain a Credentials Specifications information""",
        tags = ["Credentials Specifications API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CredentialSpec::class)
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
    @GetMapping(path = ["/credential-specification/{id}"], produces = ["application/json"])
    fun getCredentialSpecification(@PathVariable id: String): ResponseEntity<CredentialSpec>

    /**
     * Interface that obtains a all Credentials Specification
     */
    @Operation(
        summary = "Obtain a list of Credentials Specification",
        description = """This method obtain a list of  Credentials Specifications """,
        tags = ["Credentials Specifications API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CredentialSpec::class)
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
    @GetMapping(path = ["/credential-specification"], produces = ["application/json"])
    fun getCredentialSpecifications(): ResponseEntity<ArrayList<CredentialSpec>>

    /**
     * Interface of a method that creates an Credentials Specification
     * @param bearerToken the token to access to create an Credentials Specification
     * @param body the Credentials Specification to store
     */
    @Operation(
        summary = "Creation of a JSON Credentials Specification",
        description = """This method creates a JSON Credentials Specification""",
        tags = ["Credentials Specifications API"]
    )
    @PostMapping(path = ["/credential-specification"], consumes = ["application/json"])
    fun postCredentialSpecification(
        //       @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: CredentialSpec
    ): ResponseEntity<CredentialSpec>

    /**
     * Interface of a method that updates an Credentials Specification
     * @param id the id of the Credentials Specification
     * @param body the Credentials Specification to store
     */
    @Operation(
        summary = "Update a JSON Credentials Specification",
        description = """This method updates a JSON Credentials Specification""",
        tags = ["Credentials Specifications API"]
    )
    @PutMapping(path = ["/credential-specification/{id}"], consumes = ["application/json"])
    fun updateCredentialSpecification(
        @PathVariable id: String,
        @RequestBody body: CredentialSpec
    ): ResponseEntity<CredentialSpec>

    /**
     * Interface of a method that deletes an Credentials Specification
     * @param id the id of the Credentials Specification
     */
    @Operation(
        summary = "Delete a JSON Credentials Specification",
        description = """This method deletes a JSON Credentials Specification""",
        tags = ["Credentials Specifications API"]
    )
    @DeleteMapping(path = ["/credential-specification/{id}"])
    fun deleteCredentialSpecification(@PathVariable id: String): ResponseEntity<Unit>

}
