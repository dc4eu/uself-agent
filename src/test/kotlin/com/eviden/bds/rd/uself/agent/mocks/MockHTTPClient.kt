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

package com.eviden.bds.rd.uself.agent.mocks

import com.eviden.bds.rd.uself.common.models.ErrorExecution
import com.eviden.bds.rd.uself.common.models.exceptions.Exception400
import com.eviden.bds.rd.uself.common.models.exceptions.Exception401
import com.eviden.bds.rd.uself.common.models.exceptions.Exception500
import com.eviden.bds.rd.uself.common.services.httpClient.HTTPClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class MockHTTPClient(engine: HttpClientEngine) : HTTPClient {
    override val client = HttpClient(engine)

    override fun filterResponse(response: HttpResponse): String = runBlocking {
        when (response.status) {
            HttpStatusCode.OK -> {
                return@runBlocking response.bodyAsText()
            }

            HttpStatusCode.Found -> {
                return@runBlocking response.headers["location"]!!
            }

            HttpStatusCode.BadRequest -> {
                val error = response.body<ErrorExecution>()
                throw Exception400(error.errorDescription)
            }

            HttpStatusCode.Unauthorized -> {
                val error = response.body<ErrorExecution>()
                throw Exception401(error.errorDescription)
            }

            HttpStatusCode.InternalServerError -> {
                val error = response.body<ErrorExecution>()
                throw Exception500(error.errorDescription)
            }

            else -> error("Unhandled status: ${response.status}")
        }
    }
}
