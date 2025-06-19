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
@file:Suppress("TooManyFunctions")
package com.eviden.bds.rd.uself.agent.services.rclientsession

import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession

interface RClientSessionService {
    // save methods
    fun saveSession(session: RClientSession): RClientSession

    //find methods
    fun findByNonce(nonce: String): List<RClientSession>

    fun findByClientId(clientId: String): List<RClientSession>

    fun findByCNonce(cNonce: String): List<RClientSession>

    fun findByState(state: String): List<RClientSession>

    fun findByCode(code: String): List<RClientSession>

    fun findByCredOfferId(credOfferId: String): List<RClientSession>

    fun findByRequestID(requestID: String): List<RClientSession>

    //exists methods
    fun existsByNonce(nonce: String): Boolean

    fun existsByState(state: String): Boolean

    //delete methods
    fun deleteSession(session: RClientSession)

    fun deleteByNonce(nonce: String)

    fun deleteByState(state: String)

    fun deleteByClientId(clientId: String)

    fun deleteAll()
}
