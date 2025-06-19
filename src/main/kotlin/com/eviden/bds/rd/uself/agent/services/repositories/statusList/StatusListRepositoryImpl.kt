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

package com.eviden.bds.rd.uself.agent.services.repositories.statusList

import com.eviden.bds.rd.uself.agent.models.entities.status.StatusDAO
import com.eviden.bds.rd.uself.agent.models.entities.status.StatusData
import com.eviden.bds.rd.uself.common.models.STATUS_TYPE
import com.eviden.bds.rd.uself.common.services.status.repository.BitString
import com.eviden.bds.rd.uself.common.services.status.repository.StatusListRepository
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class StatusListRepositoryImpl(private val statusDao: StatusDAO) : StatusListRepository {

    override fun getEncodedStatusList(type: String): String {
        val statusData = getOrInitializeStatusData()
        return when (type) {
            STATUS_TYPE.REVOCATION -> statusData.revocationEncodedList
            STATUS_TYPE.SUSPENSION -> statusData.suspensionEncodedList
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }

    override fun revoke(i: Int): Boolean {
        val statusData = getOrInitializeStatusData()
        val bitString = BitString(statusData.revocationEncodedList)
        bitString.invalid(i)
        statusData.revocationEncodedList = bitString.encodeBits()
        saveStatusData(statusData)
        return true
    }

    override fun suspend(i: Int): Boolean {
        val statusData = getOrInitializeStatusData()
        val bitString = BitString(statusData.suspensionEncodedList)
        bitString.invalid(i)
        statusData.suspensionEncodedList = bitString.encodeBits()
        saveStatusData(statusData)
        return true
    }

    override fun restore(i: Int): Boolean {
        val statusData = getOrInitializeStatusData()
        val bitString = BitString(statusData.suspensionEncodedList)
        bitString.restore(i)
        statusData.suspensionEncodedList = bitString.encodeBits()
        saveStatusData(statusData)
        return true
    }

    override fun getValidIndex(type: String): Int {
        val statusData = getOrInitializeStatusData()
        when (type) {
            STATUS_TYPE.REVOCATION -> {
                var randomIndex: Int
                val revokationUsedIndices = statusData.revocationUsedBitmask
                do {
                    randomIndex = Random.nextInt(0, BitString.REGISTRY_MIN_SIZE_IN_KB - 1)
                } while (revokationUsedIndices[randomIndex])
                revokationUsedIndices[randomIndex] = true
                statusData.revocationUsedBitmask = revokationUsedIndices
                statusDao.save(statusData)
                return randomIndex
            }
            STATUS_TYPE.SUSPENSION -> {
                var randomIndex: Int
                val suspensionUsedIndices = statusData.suspensionUsedBitmask
                do {
                    randomIndex = Random.nextInt(0, BitString.REGISTRY_MIN_SIZE_IN_KB - 1)
                } while (suspensionUsedIndices[randomIndex])
                suspensionUsedIndices[randomIndex] = true
                statusData.suspensionUsedBitmask = suspensionUsedIndices
                statusDao.save(statusData)
                return randomIndex
            }
            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }

    override fun isRevoked(i: Int): Boolean {
        val statusData = getOrInitializeStatusData()
        val bitString = BitString(statusData.revocationEncodedList)
        return bitString.isValid(i)
    }

    override fun isSuspended(i: Int): Boolean {
        val statusData = getOrInitializeStatusData()
        val bitString = BitString(statusData.suspensionEncodedList)
        return bitString.isValid(i)
    }

    private fun getOrInitializeStatusData(): StatusData {
        return statusDao.findById("1").orElseGet {
            StatusData(
                revocationUsedBitmask = BooleanArray(BitString.REGISTRY_MIN_SIZE_IN_KB),
                suspensionUsedBitmask = BooleanArray(BitString.REGISTRY_MIN_SIZE_IN_KB),
                revocationEncodedList = BitString().encodeBits(),
                suspensionEncodedList = BitString().encodeBits()
            ).also { statusDao.save(it) }
        }
    }

    private fun saveStatusData(statusData: StatusData) {
        statusDao.save(statusData)
    }
}
