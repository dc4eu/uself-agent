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
package com.eviden.bds.rd.uself.agent.services.repositories.activity

import com.eviden.bds.rd.uself.agent.models.entities.activity.ActivityDAO
import com.eviden.bds.rd.uself.agent.models.entities.activity.ActivityEntity
import com.eviden.bds.rd.uself.agent.models.entities.activity.toEntity
import com.eviden.bds.rd.uself.common.models.openid.tracker.Activity
import com.eviden.bds.rd.uself.common.models.openid.tracker.TrackerType
import com.eviden.bds.rd.uself.common.services.tracker.repository.ActivityRepository
import org.springframework.stereotype.Service

@Service
class ActivityRepositoryImpl(private val activityDAO: ActivityDAO) : ActivityRepository {
    override fun getActivities(): List<Activity> {
        return activityDAO.findAll().map { it.toData() }
    }

    override fun getAllActivitiesStream(): List<Activity> {
        TODO("Method not implemented only use for mobile application actuaÑslly")
    }

    override fun getActivityStream(id: Int): Activity? {
        TODO("Method not implemented only use for mobile application actually")
    }

    /**
     * Insert activity in the the Database
     * @param action the action that the user has done
     * @param actionDetail the detail of the action
     * @param className the class name where the action was done
     * @param trackerType the type of the action
     * @return the activity inserted
     */
    override fun insertActivity(
        action: String,
        actionDetail: String,
        className: String,
        trackerType: TrackerType
    ) {
        val activity = ActivityEntity(
            actionName = action,
            actionDetail = actionDetail,
            className = className,
            trackerType = trackerType.name
        )
        activityDAO.save(activity)
    }

    /**
     * Delete activity from the the Database
     * @param id the id of the activity to delete
     */
    override fun deleteActivity(id: Int) {
        activityDAO.deleteById(id)
    }

    override fun updateActivity(activity: Activity) {
        activityDAO.save(activity.toEntity())
    }
}
