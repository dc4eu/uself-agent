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

package com.eviden.bds.rd.uself.agent.api.credentialSpecification

import com.eviden.bds.rd.uself.common.models.credentialSpecification.CredentialSpec
import com.eviden.bds.rd.uself.common.services.credentialSpecification.CredentialSpecificationService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * Class that implements the CredentialsSpecificationApi interface
 */
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class CredentialSpecificationApiController : CredentialSpecificationApi, KoinComponent {
    private val tracker: Tracker = Tracker(this::class.java)

    private val credentialSpecURL = " /credential-specification"

    private val credentialSpecificationService: CredentialSpecificationService by inject(
        CredentialSpecificationService::class.java
    )

    /**
     * Method that obtains a certain JSON Credentials Specification
     * @param id the id of the JSON Credentials Specification
     * @return the JSON Credentials Specification
     */
    override fun getCredentialSpecification(id: String): ResponseEntity<CredentialSpec> {
        tracker.infoGET("$credentialSpecURL/$id", "Obtain a Credentials Specification")
        val result = credentialSpecificationService.getCredentialSpecification(id)
        tracker.infoRESP200("$credentialSpecURL/$id", result)
        return ResponseEntity.ok(result)
    }

    /**
     * Interface of a method that creates a JSON Credentials Specification
     * @param bearerToken the token to access to create a Credentials Specification
     * @param body the Credentials Specification to store
     * @return the id of the Credentials Specification
     */
    override fun postCredentialSpecification(
        //   @RequestHeader("Authorization") bearerToken: String,
        @RequestBody body: CredentialSpec
    ): ResponseEntity<CredentialSpec> {
        tracker.infoPOST(credentialSpecURL, body)
        val result = credentialSpecificationService.postCredentialSpecification(body)
        tracker.infoRESP200(credentialSpecURL, result)
        return ResponseEntity.ok(result)
    }

    override fun updateCredentialSpecification(id: String, body: CredentialSpec): ResponseEntity<CredentialSpec> {
        tracker.infoPUT("$credentialSpecURL/$id", body)
        val result = credentialSpecificationService.updateCredentialSpecification(id, body)
        tracker.infoRESP200("$credentialSpecURL/$id", result)
        return ResponseEntity.ok(result)
    }

    override fun deleteCredentialSpecification(id: String): ResponseEntity<Unit> {
        tracker.infoDELETE("$credentialSpecURL/$id", "Delete a Credentials Specification")
        credentialSpecificationService.deleteCredentialSpecification(id)
        tracker.infoRESP200("$credentialSpecURL/$id", "Credentials Specification deleted")
        return ResponseEntity.ok().build()
    }

    /**
     * Method that obtains a JSON list of Credentials Specification
     * @return the JSON list of Credentials Specifications
     */
    override fun getCredentialSpecifications(): ResponseEntity<ArrayList<CredentialSpec>> {
        tracker.infoGET(credentialSpecURL, "Obtain a list of Credentials Specifications")
        val result = credentialSpecificationService.getCredentialSpecifications()
        tracker.infoRESP200(credentialSpecURL, result)
        return ResponseEntity.ok(result)
    }
}
