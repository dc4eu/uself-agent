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

import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class OpenIdClientApiController : OpenIdClientApi, KoinComponent {
    private val tracker = Tracker(this::class.java)
    private val openIdClientURL = "/openid-client"
    private val openIdClientService: OpenIdClientService by inject(OpenIdClientService::class.java)

    /**
     * Method that obtains all JSON OpenId Clients
     * @return the list of JSON OpenId Clients
     */

    @CrossOrigin(origins = ["*"], maxAge = 3600)
    override fun getOpenIdClients(): ResponseEntity<List<OpenIdClientData>> {
        tracker.infoGET(openIdClientURL, "Obtain a list of OpenId Clients")
        val result = openIdClientService.getOpenIdClients()
        tracker.infoRESP200(openIdClientURL, result)
        return ResponseEntity.ok(result)
    }

    /**
     * Method that obtains a certain JSON OpenId Client
     * @param id the id of the JSON OpenId Client
     * @return the JSON OpenId Client
     */
    override fun getOpenIdClient(id: String): ResponseEntity<OpenIdClientData> {
        tracker.infoGET("{openIdClientURL}/$id", "Obtain an OpenId Client")
        val result = openIdClientService.getOpenIdClient(id)
        tracker.infoRESP200("{openIdClientURL}/$id", result)
        return ResponseEntity.ok(result)
    }

    /**
     * Method that creates a new JSON OpenId Client
     * @param openIdClientData the JSON OpenId Client to be created
     * @return the JSON OpenId Client created
     */
    override fun postOpenIdClient(
        @RequestBody body: OpenIdClientData
    ): ResponseEntity<OpenIdClientData> {
        tracker.infoPOST(openIdClientURL, body)
        val result = openIdClientService.createOpenIdClient(body)
        tracker.infoRESP200(openIdClientURL, result)
        return ResponseEntity.ok(result)
    }

    override fun putOpenIdClient(id: String, body: OpenIdClientData): ResponseEntity<OpenIdClientData> {
        tracker.infoPUT("{openIdClientURL}/$id", "Update an OpenId Client")
        val result = openIdClientService.updateOpenIdClient(id, body)
        tracker.infoRESP200("{openIdClientURL}/$id", result)
        return ResponseEntity.ok(result)
    }

    override fun deleteOpenIdClient(id: String) {
        tracker.infoDELETE("{openIdClientURL}$id", "Delete an OpenId Client")
        val result = openIdClientService.deleteOpenIdClient(id)
        tracker.infoRESP200("{openIdClientURL}/$id", result)
    }
}
