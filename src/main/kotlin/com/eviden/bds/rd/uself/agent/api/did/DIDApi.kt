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

import com.eviden.bds.rd.uself.common.services.did.method.DIDMethodType
import foundation.identity.did.DIDDocument
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "DID API", description = "DID API")
interface DIDApi {

    @GetMapping("/did/generate-DIDDoc")
    fun generateDIDDoc(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = true, value = "type") type: DIDMethodType
    ): DIDDocument

    @GetMapping("/did/resolve-DID")
    fun resolveDID(
        @RequestParam(required = true, value = "did") did: String
    ): DIDDocument

    @GetMapping("/did/getAll")
    fun getAll(): List<String>

    @GetMapping("/did/*/did.json")
    fun getDIDDocument(): DIDDocument
}
