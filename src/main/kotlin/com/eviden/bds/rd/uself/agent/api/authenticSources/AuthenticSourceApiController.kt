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

package com.eviden.bds.rd.uself.agent.api.authenticSources

import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.services.authenticSource.AuthenticSourceService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * Class that implements the AuthenticSourceApi interface
 */
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class AuthenticSourceApiController : AuthenticSourceApi, KoinComponent {
    private val tracker = Tracker(this::class.java)
    private val authenticSourceService: AuthenticSourceService by inject(AuthenticSourceService::class.java)
    private val authenticSourceURL = " /authentic-source"

    /**
     * Method that obtains a certain JSON Authentic Source
     * @param id the id of the JSON Authentic Source
     * @return the JSON Authentic Source
     */
    override fun getAuthenticSource(id: String): ResponseEntity<AuthenticSource> {
        tracker.infoGET("$authenticSourceURL/$id", "Obtain an Authentic Source")
        val result = authenticSourceService.getAuthenticSource(id)

        tracker.infoRESP200("$authenticSourceURL/$id", result)

        return ResponseEntity.ok(result)
    }

    /**
     * Interface of a method that creates a JSON Authentic Source
     * @param bearerToken the token to access to create a Authentic Source
     * @param body the Authentic Source to store
     * @return the id of the Authentic Source
     */
    override fun postAuthenticSource(
        //   @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: AuthenticSource
    ): ResponseEntity<AuthenticSource> {
        tracker.infoPOST(authenticSourceURL, body)
        val result = authenticSourceService.postAuthenticSource(body)
        tracker.infoRESP200(authenticSourceURL, result)
        return ResponseEntity.ok(result)
    }

    override fun deleteAuthenticSource(id: String): ResponseEntity<Unit> {
        tracker.infoDELETE("$authenticSourceURL/$id", "Delete an Authentic Source")
        authenticSourceService.deleteAuthenticSource(id)
        tracker.infoRESP200("$authenticSourceURL/$id", "Authentic Source deleted")
        return ResponseEntity.ok().build()
    }

    override fun updateAuthenticSource(id: String, body: AuthenticSource): ResponseEntity<AuthenticSource> {
        tracker.infoPUT("$authenticSourceURL/$id", body)
        val result = authenticSourceService.updateAuthenticSource(id, body)
        tracker.infoRESP200("$authenticSourceURL/$id", result)
        return ResponseEntity.ok(result)
    }

    /**
     * Method that obtains a JSON list of Authentic Source
     * @return the JSON list of Authentic Sources
     */
    override fun getAuthenticSources(): ResponseEntity<ArrayList<AuthenticSource>> {
        tracker.infoGET(authenticSourceURL, "Obtain a list of Authentic Sources")
        val result = authenticSourceService.getAuthenticSources()
        tracker.infoRESP200(authenticSourceURL, result)
        return ResponseEntity.ok(result)
    }
}
