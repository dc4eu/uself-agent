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
@file:Suppress("MaxLineLength", "ArgumentListWrapping")

package com.eviden.bds.rd.uself.agent.api.tm

import com.eviden.bds.rd.uself.common.models.ebsi.registries.TrustedRegistryEntry
import foundation.identity.did.DIDDocument
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
// @Hidden
@Tag(name = "Trust Model API", description = "Trust Model API")
interface TMController {
    @Operation(summary = "Register a DID", description = "Registers a Decentralized Identifier (DID)")
    @GetMapping("/tm/register-did")
    fun registerDID(
        @Parameter(description = "The DID to register", required = false)
        @RequestParam(required = false, value = "did") did: String?,
        @Parameter(description = "The VerifiableAuthorisationToOnboard Verifiable Credential obtained from EBSI for authorise the operation", required = false)
        @RequestParam(required = false, value = "vc") vc: String?
    ): DIDDocument

    @Operation(summary = "Register a TIR", description = "Registers a Trusted Identity Registry (TIR)")
    @GetMapping("/register-TIR")
    fun registerTIR(
        @Parameter(description = "The DID to register", required = false)
        @RequestParam(required = false, value = "did") did: String?,
        @Parameter(description = "The VerifiableAuthorisationToOnboard Verifiable Credential obtained from EBSI for authorise the operation", required = false)
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry

    @Operation(summary = "Register a TAO", description = "Registers a Trust Anchor Organization (TAO)")
    @GetMapping("/register-TAO")
    fun registerTAO(
        @Parameter(description = "The DID to register", required = false)
        @RequestParam(required = false, value = "did") did: String?,
        @Parameter(description = "The VerifiableAuthorisationToOnboard Verifiable Credential obtained from EBSI for authorise the operation", required = false)
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry

    @Operation(summary = "Register a Root TAO", description = "Registers a Root Trust Anchor Organization (Root TAO)")
    @GetMapping("/register-RootTAO")
    fun registerRootTAO(
        @Parameter(description = "The DID to register", required = false)
        @RequestParam(required = false, value = "did") did: String?,
        @Parameter(description = "The VerifiableAuthorisationToOnboard Verifiable Credential obtained from EBSI for authorise the operation", required = false)
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry

    @Operation(summary = "Get Compliance Qualification", description = "Gets the compliance qualification for a DID")
    @GetMapping("/compliance-qualification")
    fun getComplianceQualification(@RequestParam(required = false, value = "did") did: String?): String

    @Operation(summary = "Revoke a TIR", description = "Revokes a Trusted Identity Registry (TIR)")
    @GetMapping("/revoke-TIR")
    fun revokeTIR(
        @Parameter(description = "The DID to revoke", required = true)
        @RequestParam(required = false, value = "did") did: String
    )

    @Operation(summary = "Callback", description = "Callback endpoint")
    @GetMapping(path = ["/tm/call-back"])
    fun callBack()
}
