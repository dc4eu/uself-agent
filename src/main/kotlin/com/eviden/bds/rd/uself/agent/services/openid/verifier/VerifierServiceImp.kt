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
@file:Suppress("LongMethod")

package com.eviden.bds.rd.uself.agent.services.openid.verifier

import com.eviden.bds.rd.uself.agent.models.SSEEvent
import com.eviden.bds.rd.uself.agent.models.Status
import com.eviden.bds.rd.uself.agent.services.sse.SSEService
import com.eviden.bds.rd.uself.common.models.openid.auth.PresentationDefinition
import com.eviden.bds.rd.uself.common.models.openid.verifier.ValidationPresentationResult
import com.eviden.bds.rd.uself.common.models.openid.verifier.ValidationReport
import com.eviden.bds.rd.uself.common.models.openid.verifier.ValidationResult
import com.eviden.bds.rd.uself.common.models.openid.verifier.ValidationRule
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.verifier.VerifierMng
import com.nimbusds.jwt.SignedJWT
import org.koin.java.KoinJavaComponent.inject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Implementation of the VerifierService interface.
 * This service is responsible for verifying various aspects of a signed JWT.
 *
 * @property sseService The SSE service used to push events.
 */
@Service
class VerifierServiceImp(
    private val sseService: SSEService
) : VerifierService {
    @Value("\${uself.schemaExceptions}")
    private lateinit var schemaExceptions: List<String>

    private val verifierMng: VerifierMng by inject(VerifierMng::class.java)

    /**
     * Verifies the signed JWT against the provided presentation definition and validation rule.
     *
     * @param signedJWT The signed JWT to verify.
     * @param presDef The presentation definition to use for verification.
     * @param sessionId The client ID associated with the verification.
     * @param validationRule The validation rule to apply.
     * @return A pair containing a boolean indicating success and a message.
     */
    override fun verify(
        signedJWT: SignedJWT,
        presDef: PresentationDefinition,
        sessionId: String,
        validationRule: ValidationRule
    ): ValidationReport {
        val validationPresentationResult = ValidationPresentationResult(
            validateVerifiablePresentation = verifierMng.validateVerifiablePresentation(signedJWT),
            validateCredentialsInPresentation = verifierMng.validateCredentialsInPresentation(signedJWT),
            verifyVerifiablePresentationSignature = verifierMng.verifyVerifiablePresentationSignature(signedJWT),
            verifyCredentialSignatures = verifierMng.verifyCredentialSignatures(signedJWT),
            validatePresentationDefinition = verifierMng.validatePresentationDefinition(signedJWT, presDef),
            validateIssuerRegistry = verifierMng.validateIssuerRegistry(signedJWT),
            validateCredentialSchema = verifierMng.validateCredentialSchema(signedJWT, schemaExceptions)
        )

        // Push events for each validation result
        pushValidationEvents(sessionId, validationPresentationResult)

        // Validate the results using the provided validation rule
        val (result, valResult) = validationRule.validateResults(validationPresentationResult)
        return ValidationReport(
            validationResult = ValidationResult(id = createUUID(), result = result, message = valResult),
            validationPresentationResult = validationPresentationResult
        )
    }

    /**
     * Pushes validation events to the SSE service.
     *
     * @param sessionId The client ID associated with the events.
     * @param validationPresentationResult The validation results to push.
     */
    private fun pushValidationEvents(sessionId: String, validationPresentationResult: ValidationPresentationResult) {
        validationPresentationResult.verifyVerifiablePresentationSignature.let {
            sseService.pushEvents(
                SSEEvent(
                    id = sessionId,
                    status = mapValidationToStatus(it.id, it.result),
                    message = it.message
                )
            )
        }

        validationPresentationResult.verifyCredentialSignatures.forEach { result ->
            result.forEach {
                sseService.pushEvents(
                    SSEEvent(
                        id = sessionId,
                        status = mapValidationToStatus(it.id, it.result),
                        message = it.message
                    )
                )
            }
        }

        validationPresentationResult.validateVerifiablePresentation.forEach { result ->
            sseService.pushEvents(
                SSEEvent(
                    id = sessionId,
                    status = mapValidationToStatus(result.id, result.result),
                    message = result.message
                )
            )
        }

        validationPresentationResult.validateCredentialsInPresentation.forEach { result ->
            result.forEach {
                sseService.pushEvents(
                    SSEEvent(
                        id = sessionId,
                        status = mapValidationToStatus(it.id, it.result),
                        message = it.message
                    )
                )
            }
        }

        validationPresentationResult.validatePresentationDefinition.let { result ->
            sseService.pushEvents(
                SSEEvent(
                    id = sessionId,
                    status = mapValidationToStatus("presentationDefinition", result.result),
                    message = result.message
                )
            )
        }

        validationPresentationResult.validateIssuerRegistry.forEach { result ->
            result.forEach {
                sseService.pushEvents(
                    SSEEvent(
                        id = sessionId,
                        status = mapValidationToStatus(it.id, it.result),
                        message = it.message
                    )
                )
            }
        }
    }

    /**
     * Maps a validation result to a status.
     *
     * @param id The ID of the validation.
     * @param result The result of the validation.
     * @return The corresponding status.
     */
    fun mapValidationToStatus(id: String, result: Boolean): Status {
        val statusMap = mapOf(
            "vpHeader" to Pair(Status.VP_HEADER_TRUE, Status.VP_HEADER_FALSE),
            "vpExp" to Pair(Status.VP_EXP_TRUE, Status.VP_EXP_FALSE),
            "vpIat" to Pair(Status.VP_IAT_TRUE, Status.VP_IAT_FALSE),
            "vpNbf" to Pair(Status.VP_NBF_TRUE, Status.VP_NBF_FALSE),
            "vpContext" to Pair(Status.VP_CONTEXT_TRUE, Status.VP_CONTEXT_FALSE),
            "vpType" to Pair(Status.VP_TYPE_TRUE, Status.VP_TYPE_FALSE),
            "vpJti" to Pair(Status.VP_JTI_TRUE, Status.VP_JTI_FALSE),
            "vpIssuer" to Pair(Status.VP_ISSUER_TRUE, Status.VP_ISSUER_FALSE),
            "vcHeader" to Pair(Status.VC_HEADER_TRUE, Status.VC_HEADER_FALSE),
            "vcExp" to Pair(Status.VC_EXP_TRUE, Status.VC_EXP_FALSE),
            "vcIat" to Pair(Status.VC_IAT_TRUE, Status.VC_IAT_FALSE),
            "vcNbf" to Pair(Status.VC_NBF_TRUE, Status.VC_NBF_FALSE),
            "vcPeriod" to Pair(Status.VC_PERIOD_TRUE, Status.VC_PERIOD_FALSE),
            "vcContext" to Pair(Status.VC_CONTEXT_TRUE, Status.VC_CONTEXT_FALSE),
            "vcType" to Pair(Status.VC_TYPE_TRUE, Status.VC_TYPE_FALSE),
            "vcJti" to Pair(Status.VC_JTI_TRUE, Status.VC_JTI_FALSE),
            "vcSubject" to Pair(Status.VC_SUBJECT_TRUE, Status.VC_SUBJECT_FALSE),
            "vcIssuer" to Pair(Status.VC_ISSUER_TRUE, Status.VC_ISSUER_FALSE),
            "vcStatusList" to Pair(Status.VC_STATUS_LIST_TRUE, Status.VC_STATUS_LIST_FALSE),
            "vpSignature" to Pair(Status.VP_SIGNATURE_TRUE, Status.VP_SIGNATURE_FALSE),
            "vcSignature" to Pair(Status.VC_SIGNATURE_TRUE, Status.VC_SIGNATURE_FALSE),
            "presentationDefinition" to Pair(Status.PRESENTATION_DEFINITION_TRUE, Status.PRESENTATION_DEFINITION_FALSE),
            "tiValidation" to Pair(Status.TI_VALIDATION_TRUE, Status.TI_VALIDATION_FALSE),
            "taoValidation" to Pair(Status.TAO_VALIDATION_TRUE, Status.TAO_VALIDATION_FALSE),
            "rtaoValidation" to Pair(Status.RTAO_VALIDATION_TRUE, Status.RTAO_VALIDATION_FALSE)
        )

        return statusMap[id]?.let { if (result) it.first else it.second } ?: Status.ERROR
    }
}
