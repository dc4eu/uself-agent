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
@file:Suppress("TooGenericExceptionCaught")

package com.eviden.bds.rd.uself.agent.services.sse

import com.eviden.bds.rd.uself.agent.models.SSEEvent
import com.eviden.bds.rd.uself.agent.models.Status
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Primary
@Service
class SSEServiceImp : SSEService {
    private val tracker: Tracker = Tracker(this::class.java)
    companion object {
        private var streams = mutableMapOf<String, Sinks.Many<SSEEvent>>()
    }

    override fun pushEvents(event: SSEEvent) {
        runBlocking {
            tracker.debug("Event Published: $event")
            if (streams.keys.contains(event.id)) {
                val result = streams[event.id]!!.tryEmitNext(event)
                if (result.isFailure) {
                    // do something here, since emission failed
                    tracker.error("Error emitting the event: $event","")
                }
            } else {
                tracker.error("Stream not found: $event","")
            }
        }
    }

    override fun stream(id: String): Flux<SSEEvent> =
        runBlocking {
            if (!streams.containsKey(id)) {
                streams[id] = Sinks.many().multicast().onBackpressureBuffer()
                pushEvents(SSEEvent(id = id, status = Status.INIT, message = "Initializing stream"))
            }
            try {
                val result = streams[id]!!.asFlux().map { e -> e }
                return@runBlocking result
            } catch (e: Exception) {
                tracker.error("Error creating stream: $id", e)
                return@runBlocking Flux.empty()
            }
        }

}
