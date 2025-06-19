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
@file:Suppress("NoWildcardImports")

package com.eviden.bds.rd.uself.agent.api.tm

import com.eviden.bds.rd.uself.agent.services.openid.conf.OpenIdConf
import com.eviden.bds.rd.uself.common.models.ebsi.registries.TrustedRegistryEntry
import com.eviden.bds.rd.uself.common.services.tm.ebsi.RegistryType
import com.eviden.bds.rd.uself.common.services.tm.ebsi.TMEBSI
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import foundation.identity.did.DIDDocument
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/tm")
@SecurityRequirement(name = "bearerAuth")
class TMControllerImp(private val openIdConf: OpenIdConf) : TMController, KoinComponent {

    private val tracker: Tracker = Tracker(this::class.java)
    private val tmUrl = "/tm"

    private val tmEBSI: TMEBSI by inject(TMEBSI::class.java)

    @GetMapping("/register-did")
    override fun registerDID(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = false, value = "vc") vc: String?
    ): DIDDocument {
        tracker.infoGET("$tmUrl/register-did", "Register a DID {did=$did, vc=$vc}")
        val issuer = getIssuer(did)
        val didDoc = tmEBSI.registerDID(issuer, vc)

        tracker.infoRESP200("$tmUrl/register-did", "${didDoc.toJson(true)}")
        return didDoc
    }

    @GetMapping("/register-TIR")
    override fun registerTIR(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry {
        tracker.infoGET("$tmUrl/register-TIR", "Register a TIR {did=$did, vc=$vc}")
        val issuer = getIssuer(did)
        val tir = tmEBSI.registerTR(RegistryType.TIR, issuer, vc)

        tracker.infoRESP200("$tmUrl/register-TIR", tir)
        return tir
    }

    @GetMapping("/register-TAO")
    override fun registerTAO(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry {
        tracker.infoGET("$tmUrl/register-TAO", "Register TAO {did=$did, vc=$vc}")
        val issuer = getIssuer(did)
        val tir = tmEBSI.registerTR(RegistryType.TAO, issuer, vc)

        tracker.infoRESP200("$tmUrl/register-TAO", tir)
        return tir
    }

    @GetMapping("/register-RootTAO")
    override fun registerRootTAO(
        @RequestParam(required = false, value = "did") did: String?,
        @RequestParam(required = false, value = "vc") vc: String?
    ): TrustedRegistryEntry {
        tracker.infoGET("$tmUrl/register-RootTAO", "Register Root TAO {did=$did, vc=$vc}")
        val issuer = getIssuer(did)
        val tir = tmEBSI.registerTR(RegistryType.ROOT_TAO, issuer, vc)

        tracker.infoRESP200("$tmUrl/register-RootTAO", tir)
        return tir
    }

    @GetMapping("/revoke-TIR")
    override fun revokeTIR(
        @RequestParam(required = false, value = "did") did: String
    ) {
        tracker.infoGET("$tmUrl/revoke-TIR", "Revoke TIR {did=$did}")
        tmEBSI.revokeTR(openIdConf.issuerDID(), did)
        tracker.infoRESP200("$tmUrl/revoke-TIR", "Revoked $did")
    }

    @GetMapping("/compliance-qualification")
    override fun getComplianceQualification(
        @RequestParam(required = false, value = "did") did: String?
    ): String {
        tracker.infoGET("$tmUrl/getComplianceQualification", "Get Compliance Qualification {did=$did}")
        val issuer = getIssuer(did)
        val vc = tmEBSI.getComplianceQualification(issuer)

        tracker.infoRESP200("$tmUrl/getComplianceQualification", vc.toJson())
        return vc.toJson()
    }

    private fun getIssuer(did: String?): String {
        return if (did.isNullOrEmpty()) {
            openIdConf.issuerDID()
        } else {
            did
        }
    }

    @GetMapping(path = ["/call-back"])
    override fun callBack() {
        tracker.infoGET("$tmUrl/call-back", "Call Back")
    }
}
