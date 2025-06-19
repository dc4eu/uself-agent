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

package com.eviden.bds.rd.uself.agent.models.entities.activity

import com.eviden.bds.rd.uself.common.models.openid.tracker.Activity
import com.eviden.bds.rd.uself.common.models.openid.tracker.TrackerType
import jakarta.persistence.*
import java.util.Date

@Entity(name = "activityEntry")
data class ActivityEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(columnDefinition = "TEXT")
    val actionName: String = "default action",

    @Column(columnDefinition = "TEXT")
    val actionDetail: String = "default action detail",
    val dateTime: String = Date().time.toString(),
    val className: String = "default class",
    val trackerType: String = TrackerType.INFO.name
) {
    fun toData(): Activity {
        return Activity(
            id = id,
            actionName = actionName,
            actionDetail = actionDetail,
            dateTime = dateTime,
            className = className,
            trackerType = trackerType
        )
    }
}

fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        actionName = actionName,
        actionDetail = actionDetail,
        dateTime = dateTime,
        className = className,
        trackerType = trackerType
    )
}
