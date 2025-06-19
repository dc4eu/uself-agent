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

import com.eviden.bds.rd.uself.common.services.issuedCredentials.IssuedCredentialsService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder

/**
 * Class that implements the IssuedCredentialApi interface
 */
// @CrossOrigin(origins = ["*", "http://localhost:4200"], maxAge = 3600)
@RestController
class IssuedCredentialApiController : IssuedCredentialsApi, KoinComponent {

    private val tracker: Tracker = Tracker(this::class.java)
    private val issuedCredURL = "/issued-credential"
    private val issuedCredentialService: IssuedCredentialsService by inject(IssuedCredentialsService::class.java)

    /**
     * Method that obtains a certain JSON Issued Credential
     * @param id the id of the JSON Issued Credential
     * @return the JSON Issued Credential
     */
    override fun getIssuedCredential(id: String): ResponseEntity<String> {
        tracker.infoGET("$issuedCredURL/$id", "Obtain an Issued Credential")
        val result = issuedCredentialService.get(id)
        tracker.infoRESP200("$issuedCredURL/$id", result)

        return ResponseEntity.ok(result)
    }

    /**
     * Interface of a method that creates a JSON Issued Credential
     * @param bearerToken the token to access to create a Issued Credential
     * @param body the Issued Credential to store
     * @return the id of the Issued Credential
     */
    override fun postIssuedCredential(@RequestBody body: String): ResponseEntity<String> {
        tracker.infoPOST(issuedCredURL, body)
        val result = issuedCredentialService.create(body)

        tracker.infoRESP200(issuedCredURL, result)
        return ResponseEntity.ok(result)
    }

    /**
     * Interface of a method that creates a JSON Issued Credential
     * @param bearerToken the token to access to create a Issued Credential
     * @param body the Issued Credential to store
     * @return the id of the Issued Credential
     */
    override fun putIssuedCredential(@RequestBody body: String): ResponseEntity<String> {
        tracker.infoPUT(issuedCredURL, body)
        val result = issuedCredentialService.update(body)

        tracker.infoRESP200(issuedCredURL, result)
        return ResponseEntity.ok(result)
    }

    /**
     * Method that obtains a JSON list of Issued Credential
     * @return the JSON list of Issued Credentials
     */
    override fun getIssuedCredentials(): ResponseEntity<String> {
        tracker.infoGET(issuedCredURL, "Obtain a list of Issued Credentials")
        val result = issuedCredentialService.getAll()

        tracker.infoRESP200(issuedCredURL, result.toString())
        return ResponseEntity.ok(result.toString())
    }

    /**
     * Method that deletes a JSON Issued Credential
     * @param id the id of the JSON Issued Credential
     * @return the id of the Issued Credential
     */
    override fun deleteIssuedCredential(id: String): ResponseEntity<String> {
        tracker.infoDELETE("$issuedCredURL/$id", "Delete an Issued Credential")
        val decoded = URLDecoder.decode(id, Charsets.UTF_8)
        val result = issuedCredentialService.delete(decoded)

        tracker.infoRESP200("$issuedCredURL/$id", result)
        return ResponseEntity.ok(result)
    }
}
