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

package com.eviden.bds.rd.uself.agent.models.entities.status

import com.eviden.bds.rd.uself.common.services.status.repository.BitString
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.Convert

@Entity
data class StatusData(
    @Id
    val id: String = "1",

    @Convert(converter = BooleanArrayConverter::class)
    @Column(length = BitString.REGISTRY_MIN_SIZE_IN_KB)
    var revocationUsedBitmask: BooleanArray = BooleanArray(BitString.REGISTRY_MIN_SIZE_IN_KB),

    @Convert(converter = BooleanArrayConverter::class)
    @Column(length = BitString.REGISTRY_MIN_SIZE_IN_KB)
    var suspensionUsedBitmask: BooleanArray = BooleanArray(BitString.REGISTRY_MIN_SIZE_IN_KB),

    @Column
    var revocationEncodedList : String = "",

    @Column
    var suspensionEncodedList : String = "",
)
