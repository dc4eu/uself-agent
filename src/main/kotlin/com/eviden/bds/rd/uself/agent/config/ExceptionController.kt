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

package com.eviden.bds.rd.uself.agent.config

import com.eviden.bds.rd.uself.agent.models.ErrorExecution
import com.eviden.bds.rd.uself.agent.models.Problem
import com.eviden.bds.rd.uself.common.models.exceptions.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

@RestControllerAdvice
class ExceptionController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler
    fun handleOIDCException(ex: OIDCException): ResponseEntity<ErrorExecution> {
        val error = ErrorExecution(
            error = ex.type.toString(),
            errorDescription = ex.message.toString()
        )
        logger.debug("Response:\n ${Json.encodeToString(error)}")
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleException400(ex: Exception400): ResponseEntity<Problem> {
        val problem = Problem(
            type = "400: BadRequest",
            title = "No valid input parameters",
            status = HttpStatus.UNAUTHORIZED.value().toString(),
            detail = ex.message,
            instance = getStackTrace(ex)
        )
        logger.debug("Response:\n ${Json.encodeToString(problem)}")
        return ResponseEntity(problem, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleException401(ex: Exception401): ResponseEntity<Problem> {
        val problem = Problem(
            type = "401: Unauthorized",
            title = "Unauthorized Access",
            status = HttpStatus.UNAUTHORIZED.value().toString(),
            detail = ex.message,
            instance = getStackTrace(ex)
        )
        logger.debug("Response:\n ${Json.encodeToString(problem)}")
        return ResponseEntity(problem, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler
    fun handleException404(ex: Exception404): ResponseEntity<Problem> {
        val problem = Problem(
            type = "404: Not Found",
            title = "Not Found",
            status = HttpStatus.NOT_FOUND.value().toString(),
            detail = ex.message,
            instance = getStackTrace(ex)
        )
        logger.debug("Response:\n ${Json.encodeToString(problem)}")
        return ResponseEntity(problem, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleException500(ex: Exception500): ResponseEntity<Problem> {
        val problem = Problem(
            type = "500: InternalServerError",
            title = "Internal Server Error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value().toString(),
            detail = ex.message,
            instance = getStackTrace(ex)
        )
        logger.debug("Response:\n ${Json.encodeToString(problem)}")
        return ResponseEntity(problem, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler
    fun handleExceptionValidating(ex: ExceptionValidating): ResponseEntity<Void> {
        val error = ErrorExecution(
            error = "access_denied",
            errorDescription = ex.message.toString()
        )
        val response = "openid://?error=access_denied&error_description=${error.errorDescription}"
        logger.debug("Response:\n $response")
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response)).build()
    }
}
