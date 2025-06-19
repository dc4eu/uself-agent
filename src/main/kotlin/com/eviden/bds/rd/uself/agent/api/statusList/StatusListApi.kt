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

package com.eviden.bds.rd.uself.agent.api.statusList

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Tag(
    name = "Status List",
    description = "Status list API provides the functionality to manage status lists, including revoking, suspending, and restoring statuses following the StatusLst2021 standard"
)
@SecurityRequirement(name = "bearerAuth")
interface StatusListApi {

    @GetMapping("/status/v1")
    fun status(): String

    @GetMapping("/status/v1/revoke/{statusListIndex}")
    fun revoke(@PathVariable("statusListIndex") statusListIndex: String): Boolean

    @GetMapping("/status/v2")
    fun suspensionStatus(): String

    @GetMapping("/status/v2/suspend/{statusListIndex}")
    fun suspend(@PathVariable("statusListIndex") statusListIndex: String): Boolean

    @GetMapping("/status/v2/restore/{statusListIndex}")
    fun restore(@PathVariable("statusListIndex") statusListIndex: String): Boolean
}
