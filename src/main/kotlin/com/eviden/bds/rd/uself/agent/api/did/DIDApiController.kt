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

package com.eviden.bds.rd.uself.agent.api.did

import com.eviden.bds.rd.uself.common.services.did.DIDService
import com.eviden.bds.rd.uself.common.services.did.method.DIDMethodType
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import foundation.identity.did.DIDDocument
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/did")
/**
 * REST controller for handling DID (Decentralized Identifier) related operations.
 */
class DIDApiController: DIDApi, KoinComponent {

    private val tracker: Tracker = Tracker(this::class.java)
    private val didGenerateUrl = "/did/generate-DIDDoc"
    private val didResolveUrl = "/did/resolve-DID"
    private val didGetAllUrl = "/did/getAll"
    private val didGetDIDDocUrl = "/did/*/did.json"

    private val didService: DIDService by inject(DIDService::class.java)

    /**
     * Generates a DID Document.
     *
     * @param did The predefined DID, if any.
     * @param type The type of DID method.
     * @return The generated DID Document.
     */
    @GetMapping("/generate-DIDDoc")
    override fun generateDIDDoc(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = true, value = "type") type: DIDMethodType
    ): DIDDocument {
        tracker.infoGET("$didGenerateUrl?did=$did&type=$type")
        val didDoc = didService.generateDIDDoc(type, did)
        tracker.infoRESP200("$didGenerateUrl?did=$did&type=$type", "${didDoc.toJson(true)}")
        return didDoc
    }

    /**
     * Resolves a DID to its DID Document.
     *
     * @param did The DID to resolve.
     * @return The resolved DID Document.
     */
    @GetMapping("/resolve-DID")
    override fun resolveDID(@RequestParam(required = true, value = "did") did: String): DIDDocument {
        tracker.infoGET("$didResolveUrl?did=$did")
        val didDoc = didService.resolveDID(did)
        tracker.infoRESP200("$didResolveUrl?did=$did", "${didDoc.toJson(true)}")
        return didDoc
    }

    /**
     * Retrieves all DID Documents.
     *
     * @return A list of all DID Documents.
     */
    @GetMapping("/getAll")
    override fun getAll(): List<String> {
        tracker.infoGET(didGetAllUrl)
        val response= didService.getAll()
        tracker.infoRESP200(didGetAllUrl, response)
        return response
    }

    /**
     * Retrieves the DID Document for the current request URI.
     *
     * @return The resolved DID Document.
     */
    @GetMapping("/*/did.json")
    override fun getDIDDocument(): DIDDocument {
        val uri = ServletUriComponentsBuilder.fromCurrentRequest().build()
        tracker.debug("uri: $uri")

        val port = uri.port.let { if (it == -1) "" else "%3A$it" }
        val path = uri.path?.replace("/did.json", "")?.replace("/", ":")
        tracker.debug("did:web:${uri.host}$port$path")
        tracker.infoGET(didGetDIDDocUrl)
        val didDoc = didService.resolveDID("did:web:${uri.host}$port$path")
        tracker.infoRESP200(didGetDIDDocUrl, "${didDoc.toJson(true)}")
        return didDoc
    }
}
