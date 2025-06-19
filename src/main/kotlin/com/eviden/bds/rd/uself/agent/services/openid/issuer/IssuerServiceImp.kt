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
@file:Suppress("NoWildcardImports", "UnusedPrivateMember", "MaxLineLength", "LongMethod")

package com.eviden.bds.rd.uself.agent.services.openid.issuer

import com.danubetech.verifiablecredentials.VerifiableCredential
import com.eviden.bds.rd.uself.agent.models.SSEEvent
import com.eviden.bds.rd.uself.agent.models.Status
import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession
import com.eviden.bds.rd.uself.agent.services.openid.auth.ext.SignedJWTExt.parseJWT
import com.eviden.bds.rd.uself.agent.services.openid.conf.OpenIdConf
import com.eviden.bds.rd.uself.agent.services.rclientsession.RClientSessionService
import com.eviden.bds.rd.uself.agent.services.sse.SSEService
import com.eviden.bds.rd.uself.common.models.exceptions.ErrorTypes
import com.eviden.bds.rd.uself.common.models.exceptions.Exception403
import com.eviden.bds.rd.uself.common.models.exceptions.OIDCException
import com.eviden.bds.rd.uself.common.models.openid.issuer.*
import com.eviden.bds.rd.uself.common.services.Utils.calculateSubAccountMSI
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.credentialPattern.CredentialPatternFactory
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService
import com.eviden.bds.rd.uself.common.services.issuer.IssuerMng
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.http.client.utils.URIBuilder
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class IssuerServiceImp(
    private val openIdConf: OpenIdConf,
    private val sseService: SSEService,
    private val rClientSessionService: RClientSessionService

) : IssuerService, KoinComponent {
    private val issuerMetadata by lazy { openIdConf.issuerMetadata() }
    private val jwtSignerService: JWTSignerService by inject(JWTSignerService::class.java)
    private val issuerMng: IssuerMng by inject(IssuerMng::class.java)

    private fun getIssuerDID(): String {
        return openIdConf.issuerDID()
    }
    override fun initiate(session: HttpSession, initiateRequest: CredentialOfferRequest): URI{
        // this is executed for VCI (only) and it must take into consideration 2 possible options: In Time and PreAuth
        // add the nonce to the request if it is null
        initiateRequest.nonce = initiateRequest.nonce ?: createUUID()
        val code = signCredentialOffer(initiateRequest)
        val credentialOffer = when (initiateRequest.credentialType.contains("PreAuth") || initiateRequest.credentialType.contains("PID")) {
            true -> {
                CredentialOfferResponse(
                    credentialIssuer = issuerMetadata.credentialIssuer,
                    credentials = arrayListOf(
                        CredentialPatternFactory.get(initiateRequest.credentialType).getCredentialSupported()
                    ),
                    grants = CredentialOfferGrants(
                        preAuthorizedCode = CredentialOfferGrantsPreAuthorizedCode(
                            preAuthorizedCode = code,
                            userPinRequired = true
                        )
                    )
                )
            }
            false -> {
                CredentialOfferResponse(
                    credentialIssuer = issuerMetadata.credentialIssuer,
                    credentials = arrayListOf(
                        CredentialPatternFactory.get(initiateRequest.credentialType).getCredentialSupported()
                    ),
                    grants = CredentialOfferGrants(
                        authorizationCode = CredentialOfferGrantsAuthorizationCode(
                            issuerState = code
                        )
                    )
                )
            }
        }
        var userInfo: Map<String, Any>? = null
        initiateRequest.bearerToken?.let {
            if (!it.contains("null")) {
                userInfo = SignedJWT.parse(initiateRequest.bearerToken?.replace("Bearer ", "")).payload.toJSONObject()
            }
        }
        val credentialOfferId = createUUID()
        rClientSessionService.saveSession(
            RClientSession(
                id = createUUID(),
                sessionId = initiateRequest.nonce!!,
                nonce = initiateRequest.nonce!!,
                clientId = initiateRequest.clientId,
                code = code,
                userInfo = userInfo,
                credOfferId = credentialOfferId,
                credentialOfferResponse = Json.encodeToString(credentialOffer)
            )
        )

        val result = URIBuilder()
            .addParameter(
                "credential_offer_uri",
                "${issuerMetadata.credentialIssuer}/offers/$credentialOfferId"
            )
            .build()

        return result
    }

    private fun signCredentialOffer(initiateRequest: CredentialOfferRequest): String {
        val claimsMap = mutableMapOf<String, Any>(
            JWTClaimNames.ISSUER to issuerMetadata.credentialIssuer,
            JWTClaimNames.AUDIENCE to issuerMetadata.authorizationServer,
            JWTClaimNames.ISSUED_AT to Date.from(Instant.now()),
            JWTClaimNames.EXPIRATION_TIME to Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),

        )
        claimsMap["nonce"] = initiateRequest.nonce!!
        initiateRequest.clientId?.let {
            claimsMap[JWTClaimNames.SUBJECT] = initiateRequest.clientId!!
            claimsMap["client_id"] = initiateRequest.clientId!!
        }

        if (initiateRequest.credentialType.contains("PreAuth") || initiateRequest.credentialType.contains("pid")) {
            claimsMap["authorization_details"] = CredentialPatternFactory.get(initiateRequest.credentialType).getCredentialSupported()
        } else {
            claimsMap["credential_types"] = getCredentialTypes(initiateRequest.credentialType)
        }

        return jwtSignerService.signAndVerify(getIssuerDID(), claimsMap).serialize()
    }

    private fun getCredentialTypes(credentialType: String): ArrayList<String> {
        return CredentialPatternFactory.getCredSpec(credentialType)!!.types
    }

    override fun getCredentialOfferByID(session: HttpSession, id: String): CredentialOfferResponse {
        // return sessionService.getCredentialOffer(id)
        // return credentialOfferSessionService.get(id)
        val repose = rClientSessionService.findByCredOfferId(id).firstOrNull()?.credentialOfferResponse
        return Json.decodeFromString(repose!!)
            ?: throw OIDCException(ErrorTypes.UNAUTHORIZED_CLIENT, "Credential Offer not found")
    }

    override fun postCredential(session: HttpSession, credentialRequest: CredentialRequest): CredentialResponse {
        //  Verification of the Bearer Token
        verifyAccessToken(credentialRequest.bearerToken)
        // Obtain the clientId
        val (did, jwt) = credentialRequest.proof.jwt?.parseJWT() ?:throw OIDCException( ErrorTypes.INVALID_REQUEST, "Invalid JWT")
        // We check if the DID is a sub-account MSI
        // If so the DID is not anchored into the system therefore we can't verify the jwt
        // because we don't have the public key available
        if (!calculateSubAccountMSI(getIssuerDID(), did)) {
            jwtSignerService.verify(did, jwt)
        }
        val cNonce = jwt.jwtClaimsSet.getStringClaim("nonce")

        val rSession = rClientSessionService.findByCNonce(cNonce).firstOrNull() ?: throw OIDCException( ErrorTypes.INVALID_REQUEST, "Client session not found for cNonce: $cNonce")

        val credential = issuerMng.issueVerifiableCredential(
            getIssuerDID(),
            did,
            credentialRequest.types.last(),
            rSession.userInfo
        )
        sseService.pushEvents(
            SSEEvent(
                id = rSession.sessionId,
                status = Status.ISSUED_CREDENTIAL,
                message = credential.credential
            )
        )
        return credential
    }

    private fun verifyAccessToken(bearerToken: String?) {
        bearerToken ?: throw Exception403("Bearer Token not provided")
        val (did, jwt) = bearerToken.parseJWT()
        jwtSignerService.verify(did, jwt)
    }

    private fun signCredential(credential: VerifiableCredential): String {
        val set = JWTClaimsSet.Builder()
            .issuer(getIssuerDID())
            .subject(credential.credentialSubject.id.toString())
            .issueTime(credential.issuanceDate)
            .expirationTime(credential.expirationDate)
            .notBeforeTime(credential.issuanceDate)
            .claim("jti", credential.id)
            .claim("vc", credential.toMap())
            .build()
        return jwtSignerService.signAndVerify(getIssuerDID(), set.toJSONObject()).serialize()
    }
}
