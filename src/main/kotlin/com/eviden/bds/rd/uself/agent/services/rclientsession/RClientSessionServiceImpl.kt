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
@file:Suppress("TooManyFunctions", "MagicNumber")
package com.eviden.bds.rd.uself.agent.services.rclientsession

import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession
import com.eviden.bds.rd.uself.agent.services.repositories.rclientsession.RClientSessionRepository
import com.eviden.bds.rd.uself.common.models.exceptions.ErrorTypes
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import org.springframework.stereotype.Service

@Service
class RClientSessionServiceImpl(
    private val repository: RClientSessionRepository
) : RClientSessionService {

    override fun saveSession(session: RClientSession): RClientSession {
        if (session.nonce != null && repository.existsByNonce(session.nonce!!) && !repository.existsById(session.id)) {
            throw OIDCException( ErrorTypes.INVALID_REQUEST, "Nonce value must be unique.")
        }
        if (session.state != null && repository.existsByState(session.state!!) && !repository.existsById(session.id)) {
            throw OIDCException( ErrorTypes.INVALID_REQUEST, "State value must be unique.")
        }
        if (session.cNonce != null && repository.existsByCNonce(session.cNonce!!) && !repository.existsById(session.id)) {
            throw OIDCException( ErrorTypes.INVALID_REQUEST, "CNonce value must be unique.")
        }
        if (session.code != null && repository.existsByCode(session.code!!) && !repository.existsById(session.id)) {
            throw OIDCException( ErrorTypes.INVALID_REQUEST, "Code value must be unique.")
        }

        // Set expiration time to 10 mins
        if(!repository.existsById(session.id)) session.expiration = 600

        return repository.save(session)
    }

    override fun findByNonce(nonce: String): List<RClientSession> = repository.findByNonce(nonce)

    override fun findByClientId(clientId: String): List<RClientSession> = repository.findByClientId(clientId)

    override fun findByCNonce(cNonce: String): List<RClientSession> = repository.findByCNonce(cNonce)

    override fun findByState(state: String): List<RClientSession> = repository.findByState(state)

    override fun findByCode(code: String): List<RClientSession> = repository.findByCode(code)

    override fun findByCredOfferId(credOfferId: String): List<RClientSession> = repository.findByCredOfferId(credOfferId)

    override fun findByRequestID(requestID: String): List<RClientSession> = repository.findByRequestID(requestID)

    override fun existsByNonce(nonce: String): Boolean = repository.existsByNonce(nonce)

    override fun existsByState(state: String): Boolean = repository.existsByState(state)

    override fun deleteSession(session: RClientSession) = repository.delete(session)

    override fun deleteByNonce(nonce: String) = repository.deleteByNonce(nonce)

    override fun deleteByState(state: String) = repository.deleteByState(state)

    override fun deleteByClientId(clientId: String) = repository.deleteByClientId(clientId)

    override fun deleteAll() = repository.deleteAll()
}
