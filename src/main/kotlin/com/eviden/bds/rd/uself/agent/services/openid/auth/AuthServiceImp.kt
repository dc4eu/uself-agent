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
@file:Suppress(
    "NoWildcardImports",
    "TooManyFunctions",
    "SwallowedException",
    "CyclomaticComplexMethod",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UNCHECKED_CAST",
    "LongMethod"
)

package com.eviden.bds.rd.uself.agent.services.openid.auth

import com.danubetech.verifiablecredentials.jwt.FromJwtConverter
import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential
import com.danubetech.verifiablecredentials.jwt.JwtVerifiablePresentation
import com.eviden.bds.rd.uself.agent.models.AuthMessage
import com.eviden.bds.rd.uself.agent.models.SSEEvent
import com.eviden.bds.rd.uself.agent.models.Status
import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.AuthorisationRequestExt.getResponse
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.AuthorisationRequestExt.signAuthRequest
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.SignedJWTExt.parseJWT
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.TokenResponseExt.signAccessToken
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.TokenResponseExt.signIdToken
import com.eviden.bds.rd.uself.agent.services.openid.conf.OpenIdConf
import com.eviden.bds.rd.uself.agent.services.openid.verifier.VerifierService
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.agent.services.sse.SSEService
import com.eviden.bds.rd.uself.common.models.*
import com.eviden.bds.rd.uself.common.models.exceptions.ErrorTypes
import com.eviden.bds.rd.uself.common.models.exceptions.ExceptionValidating
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import com.eviden.bds.rd.uself.common.models.exceptions.*
import com.eviden.bds.rd.uself.common.models.openid.auth.*
import com.eviden.bds.rd.uself.common.models.openid.verifier.ValidationRule
import com.eviden.bds.rd.uself.common.services.Utils
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.Utils.getPinCode
import com.eviden.bds.rd.uself.common.services.credentialPattern.CredentialPatternFactory
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService
import com.eviden.bds.rd.uself.common.services.kms.KMSService
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import com.nimbusds.jwt.PlainJWT
import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.http.client.utils.URIBuilder
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AuthServiceImp(
    private val openIdConf: OpenIdConf,
    private val sseService: SSEService,
    private val rClientSessionService: RClientSessionService,
    private val verifierService: VerifierService
) : AuthService, KoinComponent {

    private val authMetadata by lazy { openIdConf.authMetadata() }
    private val issuerMetadata by lazy { openIdConf.issuerMetadata() }
    private val issuerDID by lazy { openIdConf.issuerDID() }

    private val kmsService: KMSService by inject(KMSService::class.java)
    private val jwtSignerService: JWTSignerService by inject(JWTSignerService::class.java)
    private val openIdClientService: OpenIdClientService by inject(OpenIdClientService::class.java)

    override fun getMetadata(): OpenIdConfiguration {
        return authMetadata
    }

    override fun getJWKs(session: HttpSession): Jwks {
        return Jwks(keys = kmsService.getAll())
    }

    override fun getAuthorize(session: HttpSession, authReq: AuthorisationRequest): String {
        var rClientSession: RClientSession? = null
        // If the issuerState is not null then this is VCI, so we need to retrieve the previous session
        if (authReq.issuerState != null) {
            // then this is VCI
            val issuerState = SignedJWT.parse(authReq.issuerState)
            val nonce = issuerState.payload.toJSONObject()["nonce"].toString()
            rClientSession = rClientSessionService.findByNonce(nonce).firstOrNull()
        } else {
            // we need to create the new session
            val id = authReq.nonce ?: createUUID()
            rClientSession = RClientSession(id = createUUID(), sessionId = id, nonce = id)
        }
        check(rClientSession != null) { "Client session not found" }

        authReq.nonce = authReq.nonce ?: rClientSession.nonce
        authReq.state = authReq.state ?: createUUID()
        rClientSession.nonce = authReq.nonce
        rClientSession.state = authReq.state
        rClientSession.authorizationDetails = Json.encodeToString(authReq.authorizationDetails)
        rClientSession.clientId = authReq.clientId
        rClientSession.codeChallenge = authReq.codeChallenge
        rClientSession.redirectURI = authReq.redirectUri
        rClientSessionService.saveSession(rClientSession)

        // clientSessionService.add(authReq.clientId, authReq)

        // If the client is one of the OpenIDClient then we redirect to the GUI
        // in this case we can do a factory pattern to redirect to the correct GUI
        // (e.g. keycloak, IDaaS, etc.)
        // val openIDClient = openIdClientFactory.get(authReq.clientId)
        val openIDClient = openIdClientService.getOpenIdClientInstance(authReq.clientId)

        if (openIDClient != null) {
            // clientSessionService.add(authReq.nonce!!, authReq)
            return openIDClient.getClientAuthorize(authReq)
        } else {
            val (flow, credentialType) = validateAndExtractCredentialType(authReq)
            val authResponse = generateAuthResponse(authReq, flow, credentialType)
            rClientSession.requestID = authResponse.requestUri?.split("/")?.last()
            rClientSession.request = authResponse.request
            rClientSession.presentationDefinition = authResponse.presentationDefinition
            rClientSession.validationRule = Json.encodeToString(CredentialPatternFactory.getCredSpec(credentialType)?.validationRule)
            rClientSessionService.saveSession(rClientSession)

            if (authReq.clientMetadata != null) {
                val clientMetadata = authReq.clientMetadata!!
                if (clientMetadata.authorizationEndpoint != "") {
                    val authorizationEndpoint = clientMetadata.authorizationEndpoint
                    return authResponse.getResponse(authReq.redirect, authorizationEndpoint)
                }
            }
            return authResponse.getResponse(authReq.redirect, authReq.redirectUri)
        }
    }

    private fun generateAuthResponse(
        authRequest: AuthorisationRequest,
        flow: String,
        credentialType: String
    ): AuthorisationRequest {
        val requestUriID = createUUID()
        val authResponse = AuthorisationRequest(
            // To identify the issuer and trust mechanism
            iss = issuerDID,
            // iss = authMetadata.issuer,
            // clientId = authRequest.clientId,
            // this identify the issuer and allows to obtain the metadata from my server
            clientId = authMetadata.issuer, // TODO check if clientId is authRequest.clientId or authMetadata.issuer
            responseMode = RESPONSE_MODE.DIRECT_POST,
            scope = SCOPE.OPEN_ID,
            nonce = authRequest.nonce,
            state = authRequest.state,
            requestUri = "${authMetadata.issuer}/request_uri/$requestUriID",
            redirectUri = getRedirectURI(credentialType),
            responseType = getResponseType(flow, credentialType),
        )
        if (authResponse.responseType == RESPONSE_TYPE.VP_TOKEN) {
            val presDef = getPresentationDefinition(flow, credentialType)
            authResponse.presentationDefinition = presDef
        }
        // For the auth answer
        authResponse.request = authResponse.signAuthRequest(jwtSignerService, issuerDID).serialize()

//        // For storing added information for further use
//        authResponse.authorizationDetails = authRequest.authorizationDetails
//        authResponse.issuerState = authRequest.issuerState
//        clientSessionService.add(requestUriID, authResponse, authRequest.redirectUri)

        return authResponse
    }

    private fun getPresentationDefinition(flow: String, credentialType: String): String {
        return Json.encodeToString(CredentialPatternFactory.get(credentialType).getPresentationDefinition(flow))
    }

    private fun getRedirectURI(credentialType: String): String {
        return CredentialPatternFactory.get(credentialType).getRedirectURI()
    }

    /**
     * Extracts the credential ID from the given scope string.
     *
     * @param scope The scope string containing potential credential IDs.
     * @return The credential ID if found and unique, otherwise null.
     * A credential is searched for among the stored credentials using CredentialPatternFactory.getCredIds.
     */
    private fun getCredIdInScope(scope: String): String? {
        val scopesParams = scope.split(" ").toTypedArray()
        val credentialIds = CredentialPatternFactory.getCredIds()
        val matchingCredentials = scopesParams.filter { it in credentialIds }
        return if (matchingCredentials.size == 1) matchingCredentials[0] else null
    }

    /**
     * Extracts the credential ID from the authorization details of the given authorization request.
     *
     * @param authReq The authorization request containing potential credential IDs.
     * @return The credential ID if found, otherwise null.
     * A credential is searched for among the stored credentials using CredentialPatternFactory.getCredIds.
     */
    private fun getCredIdInAuthDetails(authReq: AuthorisationRequest): String? {
        val credentialIds = CredentialPatternFactory.getCredIds()
        // We only consider the first element in AuthDetails
        authReq.authorizationDetails?.firstOrNull()?.types?.lastOrNull()?.takeIf { it in credentialIds }?.let { return it }
        if (!authReq.request.isNullOrEmpty()) {
            val jwt = SignedJWT.parse(authReq.request)
            val authDetails = jwt.jwtClaimsSet.getListClaim("authorization_details") as? List<*>
            val detailString = authDetails?.firstOrNull() as? Map<String, Any>
            val types = detailString?.get("types") as? List<String>
            types?.lastOrNull()?.takeIf { it in credentialIds }?.let { return it }
        }
        return null
    }

    private fun validateAndExtractCredentialType(authRequest: AuthorisationRequest): Pair<String, String> {
        val scopesParams = authRequest.scope.split(" ").toTypedArray()
        if (!scopesParams.contains(SCOPE.OPEN_ID)) {
            throw Exception400("The scope must include openid.")
        }
        getCredIdInAuthDetails(authRequest)?.let { return FLOW.VCI to it }
        getCredIdInScope(authRequest.scope)?.let { return FLOW.VP to it }
        throw Exception400("The credential ID is required in the Authorization Details for VCI or in the scope for VP.")
    }

    private fun getResponseType(flow: String, credentialType: String): String {
        val credSpec = CredentialPatternFactory.getCredSpec(credentialType)
        val responseType = when (flow) {
            FLOW.VP -> credSpec!!.presentationDefinition?.let { RESPONSE_TYPE.VP_TOKEN } ?: RESPONSE_TYPE.ID_TOKEN
            FLOW.VCI -> {
                if (credSpec?.credentialIdsForIssuance.isNullOrEmpty()) {
                    RESPONSE_TYPE.ID_TOKEN
                } else {
                    RESPONSE_TYPE.VP_TOKEN
                }
            }
            else -> throw IllegalArgumentException("Flow for ResponseType not supported")
        }
        return responseType
    }

    override fun getRequestURI(session: HttpSession, id: String): String? {
        return rClientSessionService.findByRequestID(id).firstOrNull()?.request ?: throw Exception400("Authorization request not found")
    }

    override fun postDirectPost(session: HttpSession, authDirectPost: AuthDirectPost): String {
        val (did, jwt) = when {
            authDirectPost.idToken != null -> authDirectPost.idToken!!.parseJWT()
            authDirectPost.vpToken != null -> authDirectPost.vpToken!!.parseJWT()
            else -> throw Exception400("Both id_token and vp_token cannot be null")
        }

        val rClientSession = rClientSessionService.findByState(authDirectPost.state!!).firstOrNull()
            ?: throw Exception400("Client session not found")
        try {
            // Validate State
            authDirectPost.state?.let {
                check(rClientSession.state == it) { throw Exception400("State value is not correct $it") }
            }
            // Validate Nonce
            check(
                jwt.payload.toJSONObject()["nonce"].toString() == "" ||
                    jwt.payload.toJSONObject()["nonce"].toString() == rClientSession.nonce
            ) {
                throw Exception400("Nonce value is not correct")
            }
            // Validate Presentation
            authDirectPost.vpToken?.let { vpToken ->

                sseService.pushEvents(
                    SSEEvent(
                        id = rClientSession.sessionId,
                        status = Status.INIT_VERIFICATION,
                        message = vpToken
                    )
                )

                val presentationDefinition = rClientSession.presentationDefinition?.let {
                    Json.decodeFromString<PresentationDefinition>(it)
                }
                val validationRule = rClientSession.validationRule?.let {
                    Json.decodeFromString<ValidationRule>(it)
                }
                val verReport = verifierService.verify(
                    jwt,
                    presentationDefinition!!,
                    rClientSession.sessionId!!,
                    validationRule!!
                )
                check(verReport.validationResult.result) {
                    sseService.pushEvents(
                        SSEEvent(
                            id = rClientSession.sessionId,
                            status = Status.VERIFICATION_RESULT,
                            message = Json.encodeToString(verReport)
                        )
                    )
                    //  throw Exception400(Json.encodeToString(verReport))
                    throw OIDCException(ErrorTypes.ACCESS_DENIED, verReport.validationResult.message)
                }
                sseService.pushEvents(
                    SSEEvent(
                        id = rClientSession.sessionId,
                        status = Status.VERIFICATION_RESULT,
                        message = Json.encodeToString(verReport)
                    )
                )
                storeUserInfo(rClientSession, vpToken)
            }

            rClientSession.code = createUUID()
            rClientSessionService.saveSession(rClientSession)

            return getRequestURI(rClientSession)
        } catch (e: ExceptionValidating) {
            return (rClientSession.redirectURI ?: "") + URIBuilder()
                .addParameter("error", "invalid_request")
                .addParameter("error_description", e.message)
                .addParameter("state", authDirectPost.state)
                .build()
                .toASCIIString()
        } catch (e: OIDCException) {
            return (rClientSession.redirectURI ?: "") + URIBuilder()
                .addParameter("error", e.type.name)
                .addParameter("error_description", e.message)

                .build()
                .toASCIIString()
        }
    }

    private fun getRequestURI(rClientSession: RClientSession): String {
        sseService.pushEvents(
            SSEEvent(
                id = rClientSession.sessionId,
                status = Status.AUTHENTICATED,
                message = Json.encodeToString(AuthMessage(code = rClientSession.code!!, state = rClientSession.state!!))
            )
        )
        val entry = if (rClientSession.redirectURI.isNullOrEmpty()) {
            END_POINT.OPEN_ID
        } else {
            rClientSession.redirectURI
        }
        return entry + URIBuilder()
            .addParameter("code", rClientSession.code)
            .addParameter("state", rClientSession.state)
            .build()
            .toASCIIString()
    }

    private fun storeUserInfo(session: RClientSession, vpToken: String) {
        val vp = JwtVerifiablePresentation.fromCompactSerialization(vpToken)
        val vp2 = FromJwtConverter.fromJwtVerifiablePresentation(vp)
        val vcJwt = vp2.jsonObject["verifiableCredential"] as ArrayList<*>
        if (vcJwt.size > 0) {
            val vc = JwtVerifiableCredential.fromCompactSerialization(vcJwt.first().toString())
            val userInfo = vc.payloadObject.credentialSubject.toMap()
            session.userInfo = userInfo
        }
    }

    override fun postDirectPostPassport(session: HttpSession, authDirectPost: AuthDirectPost): String {
        val jwt = SignedJWT.parse(authDirectPost.idToken!!)

        val rClientSession = rClientSessionService.findByState(authDirectPost.state!!).firstOrNull()
            ?: throw Exception400("Client session not found")
        try {
            // Validate State
            authDirectPost.state?.let {
                check(rClientSession.state == it) { throw ExceptionValidating("State value is not correct $it") }
            }
            // Validate Nonce
            check(
                jwt.payload.toJSONObject()["nonce"].toString() == rClientSession.nonce
            ) { throw ExceptionValidating("Nonce value is not correct") }

            // Validations finished
            // generating code and storing it
            rClientSession.code = createUUID()
            rClientSession.userInfo = jwt.payload.toJSONObject()
            rClientSessionService.saveSession(rClientSession)

            return getRequestURI(rClientSession)
        } catch (e: ExceptionValidating) {
            return (rClientSession.redirectURI ?: "") + URIBuilder()
                .addParameter("error", "invalid_request")
                .addParameter("error_description", e.message)
                .addParameter("state", rClientSession.state)
                .build()
                .toASCIIString()
        }
    }

    override fun postToken(session: HttpSession, tokenRequest: TokenRequest): TokenResponse {
        // check if this is OpenID for VCI or VP

        // if it is a preAuthCode this is a request for a PreAuthorize VCI
        tokenRequest.preAuthorizedCode?.let {
            return preAuthTokenResponse(tokenRequest)
        }

        // if it is not a preAuthCode this is a request for an Authorize VCI or VP
        val code = tokenRequest.code
        check(code != null) { throw Exception400("Code is required") }
        val rClientSession = rClientSessionService.findByCode(code).firstOrNull() ?: throw Exception400("Client session not found with that code")

        //  val clientSession = clientSessionService.get(it)
        val tokenResponse = defaultTokenResponse(rClientSession, tokenRequest)

        // check if  we have a previous request associated
        // I think this mechanism should be go to /direct_post
//        rClientSession.nonce.let {
//            try {
//                val originalCS = rClientSessionRepo.findByNonce(rClientSession.nonce!!)
//                // We store the tokenResponse for the nested request
//                tokenResponseSessionService.add(tokenResponse)
//                val redirectUri =
//                    URIBuilder(
//                        URI(
//                            """
//                                https://devtest2-auth.my.evidian.com/pxpadmin/trust_auth/login/client_name/Uself
//                            """.trim()
//                        )
//                    )
//                        .addParameter("state", originalCS.state)
//                        .addParameter("code", tokenRequest.code)
//                        .build()
//                        .toASCIIString()
//                val sseEvent =
//                    SSEEvent(
//                        id = rClientSession.nonce!!,
//                        nonce = rClientSession.nonce!!,
//                        redirectUri = redirectUri,
//                        status = Status.SUCCESS
//                    )
//                sseService.pushEvents(sseEvent)
//            } catch (e: Exception400) {
//                log.debug("No previous request associated")
//            }
//        }

        sseService.pushEvents(
            SSEEvent(
                id = rClientSession.sessionId,
                status = Status.AUTHORIZED,
                message = "Request received"
            )
        )

        return tokenResponse
    }
    private fun preAuthTokenResponse(tokenRequest: TokenRequest): TokenResponse {
        val nonce = SignedJWT.parse(tokenRequest.preAuthorizedCode!!).jwtClaimsSet.getClaim("nonce")
        val cNonce = createUUID()

        val rSession = rClientSessionService.findByNonce(nonce.toString()).firstOrNull() ?: throw Exception400("Client session not found for  nonce: $nonce")

        // Verify the pin code
        val pinCode = tokenRequest.userPin
        check(pinCode != null) { "User Pin is required" }
        check(pinCode == getPinCode(nonce.toString())) {
            sseService.pushEvents(
                SSEEvent(
                    id = rSession.sessionId,
                    status = Status.ERROR,
                    message = "User Pin is not correct"
                )
            )
            // throw Exception400("User Pin is not correct")
            throw OIDCException(ErrorTypes.INVALID_REQUEST, "User Pin is not correct")
        }

        val exp = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)).time
        val tokenResponse = TokenResponse(
            issuer = authMetadata.issuer,
            audience = issuerMetadata.credentialIssuer,
            clientId = tokenRequest.clientId ?: openIdConf.issuerDID(),
            claims = AccessTokenClaims(
                clientId = tokenRequest.clientId ?: openIdConf.issuerDID(),
                cNonce = cNonce,
                cNonceExpiresIn = exp,
                userInfo = rSession.userInfo
            ),
            tokenType = TOKEN_TYPE.BEARER,
            cNonce = cNonce,
            cNonceExpiresIn = exp,
            expiresIn = exp
        )

        tokenResponse.signAccessToken(jwtSignerService, issuerDID)
        tokenResponse.signIdToken(jwtSignerService, issuerDID, rSession.userInfo)

        rSession.cNonce = cNonce
        rClientSessionService.saveSession(rSession)
        sseService.pushEvents(
            SSEEvent(
                id = rSession.sessionId,
                status = Status.AUTHORIZED,
                message = "Request received"
            )
        )
        return tokenResponse
    }

    private fun defaultTokenResponse(
        rClientSession: RClientSession,
        tokenRequest: TokenRequest
    ): TokenResponse {
        val cNonce = createUUID()
        val exp = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)).time
        val tokenResponse = TokenResponse(
            issuer = authMetadata.issuer,
            audience = issuerMetadata.credentialIssuer,
            clientId = tokenRequest.clientId ?: openIdConf.issuerDID(),
            claims = AccessTokenClaims(
                clientId = tokenRequest.clientId ?: openIdConf.issuerDID(),
                cNonce = cNonce,
                cNonceExpiresIn = exp,
                userInfo = rClientSession.userInfo
            ),
            tokenType = TOKEN_TYPE.BEARER,
            cNonce = cNonce,
            cNonceExpiresIn = exp,
            expiresIn = exp
        )
        when (tokenRequest.grantType) {
            // InTime Flow
            GRAN_TYPE.AUTH_CODE -> {
                rClientSession.codeChallenge?.let {
                    if (it != Utils.hashStringBase64(tokenRequest.codeVerifier!!)
                    ) {
                        throw Exception400("Not valid code challenge")
                    }
                }
                tokenResponse.claims!!.authorizationDetails = Json.decodeFromString(rClientSession.authorizationDetails!!)
            }
            // PreAuth Flow
            GRAN_TYPE.PRE_AUTH_CODE -> { // TODO compare preAuthCode in token request with the one from clientSession
                tokenResponse.claims!!.authorizationDetails = Json.decodeFromString(rClientSession.authorizationDetails!!)
            }

            else -> throw Exception400("Not Valid grantType")
        }
        tokenResponse.signAccessToken(jwtSignerService, issuerDID)
        tokenResponse.signIdToken(jwtSignerService, issuerDID, rClientSession.userInfo)

        rClientSession.cNonce = cNonce
        rClientSessionService.saveSession(rClientSession)
        return tokenResponse
    }

    override fun callback(session: HttpSession, paramMap: MutableMap<String, String>) {
        val alreadyCallBack = session.getAttribute("callback")
        if (alreadyCallBack == null) {
            if (paramMap["code"] == null) {
                throw Exception400("Code is required")
            }
            val rClientSession = rClientSessionService.findByCode(paramMap["code"]!!).firstOrNull()
                ?: throw Exception400("Client session not found")
            session.setAttribute("callback", true)
            sseService.pushEvents(
                SSEEvent(
                    id = rClientSession.sessionId,
                    status = Status.AUTHORIZED,
                    message = Json.encodeToString(paramMap)
                )
            )
        }
    }
}
