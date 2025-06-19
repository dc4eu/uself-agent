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

package com.eviden.bds.rd.uself.agent.api.issuedCredentials

import com.eviden.bds.rd.uself.agent.models.Problem
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
 * Interface for the controller to store and  obtain a JSON Issued Credentials
 */
@Tag(
    name = "Issued Credentials API",
    description = "Issued Credential API provides the functionality to store and obtain a the user information "
)
@SecurityRequirement(name = "bearerAuth")
interface IssuedCredentialsApi {

    /**
     * Interface that obtains the list of Issued Credential
     */
    @Operation(
        summary = "Obtain a list of Issued Credentials",
        description = """This method provides the list of the Issued Credentials information""",
        tags = ["Issued Credentials API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "text/plain",
//                        array = ArraySchema(
//                            schema = Schema(implementation = String::class)
//                        )
                    )
                ]
            ), ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = "text/plain",
                        schema = Schema(implementation = Problem::class)
                    )
                ]
            )
        ]
    )
    // @CrossOrigin(originPatterns = ["*"])
    @GetMapping(path = ["/issued-credential"], produces = ["text/plain"])
    fun getIssuedCredentials(): ResponseEntity<String>

    /**
     * Interface that obtains a certain Issued Credential
     * @param id the id of the Issued Credential
     */
    @Operation(
        summary = "Obtain an Issued Credential",
        description = """This method obtain an Issued Credential information""",
        tags = ["Issued Credentials API"]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = String::class)
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
    @GetMapping(path = ["/issued-credential/{id}"], produces = ["application/json"])
    fun getIssuedCredential(@PathVariable id: String): ResponseEntity<String>

    /**
     * Interface of a method that creates an Issued Credential
     * @param bearerToken the token to access to create an Issued Credential
     * @param body the Issued Credential to store
     */
    @Operation(
        summary = "Creation of a JSON Issued Credential",
        description = "This method creates a JSON Issued Credential",
        tags = ["Issued Credentials API"]
    )
    @PostMapping(path = ["/issued-credential"], consumes = ["application/json"])
    fun postIssuedCredential(@RequestBody body: String): ResponseEntity<String>

    /**
     * Interface of a method that creates an Issued Credential
     * @param bearerToken the token to access to create an Issued Credential
     * @param body the Issued Credential to store
     */
    @Operation(
        summary = "Creation of a JSON Issued Credential",
        description = "This method updates a JSON Issued Credential",
        tags = ["Issued Credentials API"]
    )
    @PutMapping(path = ["/issued-credential"], consumes = ["application/json"])
    fun putIssuedCredential(@RequestBody body: String): ResponseEntity<String>

    /**
     * Interface of a method that deletes an Issued Credential
     * @param id the id of the Issued Credential
     */
    @Operation(
        summary = "Delete an Issued Credential",
        description = "This method deletes an Issued Credential",
        tags = ["Issued Credentials API"]
    )
    @DeleteMapping("/issued-credential/{id}")
    fun deleteIssuedCredential(@PathVariable id: String): ResponseEntity<String>
}
