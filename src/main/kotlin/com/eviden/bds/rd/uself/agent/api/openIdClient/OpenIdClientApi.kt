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

package com.eviden.bds.rd.uself.agent.api.openIdClient

import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Interface for the controller to store and obtain a JSON OpenId Client
 */
@Tag(
    name = "OpenId Client API",
    description = "OpenId Client API provides the functionality to store and obtain client information"
)
@SecurityRequirement(name = "bearerAuth")
interface OpenIdClientApi {

    /**
     * Interface that obtains the list of OpenId Clients
     */
    @Operation(
        summary = "Obtain a list of OpenId Clients",
        description = """This method provides the list of the OpenId Clients information""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "List of OpenId Clients",
                content = [Content(array = ArraySchema(schema = Schema(implementation = OpenIdClientData::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [Content(schema = Schema(implementation = Problem::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = Problem::class))]
            )
        ]
    )
    @GetMapping("/openid-client")
    fun getOpenIdClients(): ResponseEntity<List<OpenIdClientData>>

    /**
     * Interface that obtains the OpenId Client with the given id
     * @param id identifier of the OpenId Client
     */
    @Operation(
        summary = "Obtain an OpenId Client",
        description = """This method provides the OpenId Client information""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OpenId Client",
                content = [Content(schema = Schema(implementation = OpenIdClientData::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [Content(schema = Schema(implementation = Problem::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = Problem::class))]
            )
        ]
    )
    @GetMapping("/openid-client/{id}")
    fun getOpenIdClient(@PathVariable id: String): ResponseEntity<OpenIdClientData>

    /**
     * Interface that stores a new OpenId Client
     * @param openIdClientData OpenId Client to store( Types supported: DEFAULT and KEYCLOAK)
     */
    @Operation(
        summary = "Store an OpenId Client",
        description = """This method stores the OpenId Client information""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OpenId Client stored",
                content = [Content(schema = Schema(implementation = OpenIdClientData::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [Content(schema = Schema(implementation = Problem::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = Problem::class))]
            )
        ]
    )
    @PostMapping(path = ["/openid-client"], consumes = ["application/json"])
    fun postOpenIdClient(@RequestBody body: OpenIdClientData): ResponseEntity<OpenIdClientData>

    /**
     * Interface that updates an OpenId Client
     * @param id identifier of the OpenId Client
     * @param openIdClientData OpenId Client to update( Types supported: DEFAULT and KEYCLOAK)
     */
    @Operation(
        summary = "Update an OpenId Client",
        description = """This method updates the OpenId Client information""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OpenId Client updated",
                content = [Content(schema = Schema(implementation = OpenIdClientData::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [Content(schema = Schema(implementation = Problem::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = Problem::class))]
            )
        ]
    )
    @PutMapping(path = ["/openid-client/{id}"], consumes = ["application/json"])
    fun putOpenIdClient(@PathVariable id: String, @RequestBody body: OpenIdClientData): ResponseEntity<OpenIdClientData>

    /**
     * Interface that deletes an OpenId Client
     * @param id identifier of the OpenId Client
     */
    @Operation(
        summary = "Delete an OpenId Client",
        description = """This method deletes the OpenId Client information""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OpenId Client deleted",
                content = [Content(schema = Schema(implementation = OpenIdClientData::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = [Content(schema = Schema(implementation = Problem::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = Problem::class))]
            )
        ]
    )
    @DeleteMapping("/openid-client/{id}")
    fun deleteOpenIdClient(@PathVariable id: String)
}
