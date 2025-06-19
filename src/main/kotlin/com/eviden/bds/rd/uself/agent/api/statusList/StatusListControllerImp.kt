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

package com.eviden.bds.rd.uself.agent.api.statusList

import com.danubetech.verifiablecredentials.credentialstatus.StatusList2021Entry
import com.eviden.bds.rd.uself.common.models.STATUS_TYPE
import com.eviden.bds.rd.uself.common.services.status.StatusListService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.web.bind.annotation.*

@RestController
//@CrossOrigin(origins = ["*"], maxAge = 3600)
class StatusListControllerImp : StatusListApi, KoinComponent {
    private val tracker: Tracker = Tracker(this::class.java)
    private val statusUrl = "/status/v1"
    private val statusListService: StatusListService by inject(StatusListService::class.java)
    private val suspensionUrl = "/status/v2"

    override fun status(): String {
        tracker.infoGET(statusUrl, "Obtain the revocation status")
        val status = statusListService.getStatusListVerifiableCredential(STATUS_TYPE.REVOCATION)
        tracker.infoRESP200(statusUrl, status)
        return status
    }

    override fun revoke(@PathVariable("statusListIndex") statusListIndex: String): Boolean {
        tracker.infoGET("$statusUrl/revoke/$statusListIndex", "Revoke the status")

        val entry = StatusList2021Entry
            .builder()
            .statusListIndex(statusListIndex.toString())
            .build()
        val response = statusListService.revokeStatusListEntry(entry)
        tracker.infoRESP200(statusUrl, response.toString())
        return response
    }

    override fun suspensionStatus(): String {
        tracker.infoGET(suspensionUrl, "Obtain the suspension status")
        val status = statusListService.getStatusListVerifiableCredential(STATUS_TYPE.SUSPENSION)
        tracker.infoRESP200(suspensionUrl, status)
        return status
    }

    override fun suspend(statusListIndex: String): Boolean {
        tracker.infoGET("$suspensionUrl/suspend/$statusListIndex", "Suspend the status")
        val entry = StatusList2021Entry
            .builder()
            .statusListIndex(statusListIndex.toString())
            .build()
        val response = statusListService.suspendStatusListEntry(entry)
        tracker.infoRESP200(suspensionUrl, response.toString())
        return response
    }

    override fun restore(statusListIndex: String): Boolean {
        tracker.infoGET("$suspensionUrl/restore/$statusListIndex", "Restore the status")
        val entry = StatusList2021Entry
            .builder()
            .statusListIndex(statusListIndex)
            .build()
        val response = statusListService.restoreStatusListEntry(entry)
        tracker.infoRESP200(suspensionUrl, response.toString())
        return response
    }
}
