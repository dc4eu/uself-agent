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

package com.eviden.bds.rd.uself.agent.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
enum class Status {
    INIT, PENDING, VALIDATE_EXP,
    VALIDATE_STATUS, VALIDATE_DID,
    VALIDATE_TIR, AUTHENTICATED,
    AUTHORIZED, ISSUED_CREDENTIAL,
    SUCCESS, ERROR,
    INIT_VERIFICATION,
    VP_HEADER_TRUE, VP_HEADER_FALSE,
    VP_EXP_TRUE, VP_EXP_FALSE,
    VP_IAT_TRUE, VP_IAT_FALSE,
    VP_NBF_TRUE, VP_NBF_FALSE,
    VP_CONTEXT_TRUE, VP_CONTEXT_FALSE,
    VP_TYPE_TRUE, VP_TYPE_FALSE,
    VP_JTI_TRUE, VP_JTI_FALSE,
    VP_ISSUER_TRUE, VP_ISSUER_FALSE,
    VC_HEADER_TRUE, VC_HEADER_FALSE,
    VC_EXP_TRUE, VC_EXP_FALSE,
    VC_IAT_TRUE, VC_IAT_FALSE,
    VC_NBF_TRUE, VC_NBF_FALSE,
    VC_PERIOD_TRUE, VC_PERIOD_FALSE,
    VC_CONTEXT_TRUE, VC_CONTEXT_FALSE,
    VC_TYPE_TRUE, VC_TYPE_FALSE,
    VC_JTI_TRUE, VC_JTI_FALSE,
    VC_SUBJECT_TRUE, VC_SUBJECT_FALSE,
    VC_ISSUER_TRUE, VC_ISSUER_FALSE,
    VC_STATUS_LIST_TRUE, VC_STATUS_LIST_FALSE,
    VP_SIGNATURE_TRUE, VP_SIGNATURE_FALSE,
    VC_SIGNATURE_TRUE, VC_SIGNATURE_FALSE,
    PRESENTATION_DEFINITION_TRUE, PRESENTATION_DEFINITION_FALSE,
    TI_VALIDATION_TRUE, TI_VALIDATION_FALSE,
    TAO_VALIDATION_TRUE, TAO_VALIDATION_FALSE,
    RTAO_VALIDATION_TRUE, RTAO_VALIDATION_FALSE,
    VERIFICATION_RESULT
}

@Serializable
data class SSEEvent(
    @SerialName("nonce") val nonce: String? = null,
    @SerialName("redirect_uri") val redirectUri: String? = null,

    @SerialName("id") val id: String? = null,
    @SerialName("status")val status: Status? = null,
    @SerialName("message") val message: String? = null
)
