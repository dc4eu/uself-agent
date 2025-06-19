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
@file:Suppress("LongParameterList")

package com.eviden.bds.rd.uself.agent.mocks

import com.danubetech.verifiablecredentials.CredentialSubject
import com.danubetech.verifiablecredentials.VerifiableCredential
import com.danubetech.verifiablecredentials.credentialstatus.StatusList2021Entry
import com.eviden.bds.rd.uself.common.models.*
import com.eviden.bds.rd.uself.common.models.AUTH_DETAILS_TYPE.OPENID_CREDENTIAL
import com.eviden.bds.rd.uself.common.models.exceptions.Exception400
import com.eviden.bds.rd.uself.common.models.openid.auth.*
import com.eviden.bds.rd.uself.common.models.openid.issuer.*
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.eviden.bds.rd.uself.common.services.credentialPattern.CredentialPatternFactory
import com.eviden.bds.rd.uself.common.services.crypto.jwtsigner.JWTSignerService.Companion.SIG_TYPE_JWT
import com.eviden.bds.rd.uself.common.services.toJWTClaimsSet
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

object WalletMock {

    /**
     * Generates a credential offer response based on the provided parameters.
     * This function supports both pre-authorized and authorization code grants.
     */
    fun generateCredentialOfferResponseExpected(
        credentialType: String,
        nonce: String,
        key: String,
        did: String,
        preAuth: Boolean
    ): CredentialOfferResponse {
        val grants = if (preAuth) {
            CredentialOfferGrants(
                preAuthorizedCode = CredentialOfferGrantsPreAuthorizedCode(
                    preAuthorizedCode = getIssuerState(nonce, credentialType, key, did, true),
                    userPinRequired = true
                )
            )
        } else {
            CredentialOfferGrants(
                authorizationCode = CredentialOfferGrantsAuthorizationCode(
                    issuerState = getIssuerState(nonce, credentialType, key, did, false)
                )
            )
        }

        return CredentialOfferResponse(
            credentialIssuer = "http://localhost/issuer",
            credentials = arrayListOf(
                CredentialPatternFactory.get(credentialType).getCredentialSupported()
            ),
            grants = grants
        )
    }

    /**
     * Generates the issuer state JWT based on the provided parameters. This is needed for the credential offer response.
     * This function supports both pre-authorized and authorization code grants.
     */
    private fun getIssuerState(
        nonce: String,
        credentialType: String,
        key: String,
        did: String,
        preAuth: Boolean
    ): String {
        val claimsMap = mutableMapOf<String, Any>(
            JWTClaimNames.ISSUER to "http://localhost/auth",
            JWTClaimNames.AUDIENCE to "http://localhost/auth",
            JWTClaimNames.ISSUED_AT to Date.from(Instant.now()),
            JWTClaimNames.EXPIRATION_TIME to Date.from(Instant.now().plus(1, ChronoUnit.HOURS))
        )
        claimsMap["nonce"] = nonce
        if (preAuth) {
            claimsMap["authorization_details"] = CredentialPatternFactory.get(credentialType).getCredentialSupported()
        } else {
            claimsMap["credential_types"] = CredentialPatternFactory.getCredSpec(credentialType)!!.types
        }
        return signJWT(key, did, jwtClaimsSetConverter(claimsMap)).serialize()
    }

    /**
     * Generates an authorization request based on the provided parameters. This will generate the request needed for
     * the /authorize endpoint
     * This function supports VCI and VP flows.
     */
    fun generateAuthRequest(
        holder: String,
        state: String,
        nonce: String,
        credentialType: String,
        codeChallenge: String,
        flow: String
    ): AuthorisationRequest {
        var authDetails: ArrayList<AuthorizationDetails>? = null
        if (flow == FLOW.VCI) {
            authDetails = arrayListOf(
                AuthorizationDetails(
                    type = OPENID_CREDENTIAL,
                    format = FORMAT.JWT_VC,
                    locations = arrayListOf("http://localhost/issuer"),
                    types = CredentialPatternFactory.getCredSpec(credentialType)!!.types
                )
            )
        }
        var scope = SCOPE.OPEN_ID
        if (flow == FLOW.VP) scope = "openid $credentialType"

        return AuthorisationRequest(
            scope = scope,
            clientId = holder,
            responseType = RESPONSE_TYPE.CODE,
            redirectUri = "http://client.example.com",
            state = state,
            authorizationDetails = authDetails,
            clientMetadata = ClientMetadata(
                authorizationEndpoint = "http://client.example.com",
                responseTypesSupported = arrayListOf("vp_token", "id_token"),
                vpFormatsSupported = CredentialFormats(
                    jwtVp = Format(algValuesSupported = arrayListOf("ES256")),
                    jwtVc = Format(algValuesSupported = arrayListOf("ES256"))
                )
            ),
            nonce = nonce,
            codeChallenge = codeChallenge,
            redirect = null
        )
    }

    /**
     * Generates an expected authorization response based on the provided parameters. This is the expected response
     * for the /authorize endpoint. It assumes the response mode is VP_TOKEN
     * This function supports VCI and VP flows.
     */
    fun generateAuthResponseExpected(
        nonce: String,
        state: String,
        key: String,
        did: String,
        credentialType: String,
        flow: String
    ): String {
        val claimsMap = mutableMapOf<String, Any>(
            "iss" to did,
            "client_id" to "http://localhost/auth",
            "response_type" to RESPONSE_TYPE.VP_TOKEN,
            "response_mode" to RESPONSE_MODE.DIRECT_POST,
            "redirect_uri" to CredentialPatternFactory.get(credentialType).getRedirectURI(),
            "scope" to SCOPE.OPEN_ID,
            "nonce" to nonce,
            "state" to state,
            "aud" to "http://localhost/auth",
            "iat" to Date.from(Instant.now()),
            "exp" to Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS)),
            "presentation_definition" to CredentialPatternFactory.get(credentialType).getPresentationDefinition(flow)
        )
        return signJWT(key, did, jwtClaimsSetConverter(claimsMap)).serialize()
    }

    /**
     * Generates a direct post request for authentication based on the provided parameters.
     * This function supports VCI and VP flows.
     * This is what the wallet sends after receiving the authorization response. It assumes
     * VP_TOKEN, generating the mock of the VP based on the provided parameters and the presentation submission.
     */
    fun getDirectPostRequest(
        nonce: String,
        state: String,
        credentialType: String,
        didHolder: String,
        didIssuer: String,
        keyHolder: String,
        keyIssuer: String,
        subjectClaims: Map<String, Any>,
        flow: String
    ): AuthDirectPost {
        val now = Date.from(Instant.now())
        val expireIn = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS))

        val signedVCList = when (flow) {
            FLOW.VCI -> {
                CredentialPatternFactory.getCredSpec(credentialType)!!.credentialIdsForIssuance!!.map { issuance ->
                    val claims = generateCredential(issuance, now, expireIn, didIssuer, didHolder, subjectClaims)
                    signJWT(keyIssuer, didIssuer, claims).serialize()
                }.toCollection(ArrayList())
            }
            FLOW.VP -> {
                val claims = generateCredential(credentialType, now, expireIn, didIssuer, didHolder, subjectClaims)
                arrayListOf(signJWT(keyIssuer, didIssuer, claims).serialize())
            }

            else -> {
                throw Exception400("Invalid flow")
            }
        }

        val jti = "urn:uuid:${UUID.randomUUID()}"
        val vp = mutableMapOf(
            "@context" to arrayListOf("https://www.w3.org/2018/credentials/v1"),
            "id" to jti,
            "type" to arrayListOf("VerifiablePresentation"),
            "holder" to didHolder,
            "verifiableCredential" to signedVCList
        )

        val claims = JWTClaimsSet.Builder()
            .issuer(didHolder)
            .audience("http://localhost/auth")
            .subject(didHolder)
            .issueTime(now)
            .notBeforeTime(now)
            .expirationTime(expireIn)
            .jwtID(jti)
            .claim("nonce", nonce)
            .claim("state", state)
            .claim("vp", vp)
            .build()

        val directPost = AuthDirectPost(
            vpToken = signJWT(keyHolder, didHolder, claims).serialize(),
            presentationSubmission = getPresentationSubmission(),
            state = state
        )
        return directPost
    }

    fun getDirectPostRequestForPassport(
        nonce: String,
        state: String,
        credentialType: String,
        didHolder: String,
        didIssuer: String,
        keyHolder: String,
        keyIssuer: String,
        passportClaims: Map<String, Any>
    ): AuthDirectPost {
        val now = Date.from(Instant.now())
        val expireIn = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS))

        val signedVCList =
            CredentialPatternFactory.getCredSpec(credentialType)?.credentialIdsForIssuance?.map { issuance ->
                val claims = generateCredential(issuance, now, expireIn, didIssuer, didHolder, passportClaims)
                signJWT(keyIssuer, didIssuer, claims).serialize()
            } ?: listOf(
                signJWT(
                    keyIssuer,
                    didIssuer,
                    generateCredential(credentialType, now, expireIn, didIssuer, didHolder, passportClaims)
                ).serialize()
            )

        val jti = "urn:uuid:${UUID.randomUUID()}"
        val vp = mutableMapOf(
            "@context" to arrayListOf("https://www.w3.org/2018/credentials/v1"),
            "id" to jti,
            "type" to arrayListOf("VerifiablePresentation"),
            "holder" to didHolder,
            "verifiableCredential" to signedVCList
        )

        val claimsBuilder = JWTClaimsSet.Builder()
            .issuer(didHolder)
            .audience("http://localhost/auth")
            .subject(didHolder)
            .issueTime(now)
            .notBeforeTime(now)
            .expirationTime(expireIn)
            .jwtID(jti)
            .claim("nonce", nonce)
            .claim("state", state)

        // Add all passportClaims to the idToken
        passportClaims.forEach { (key, value) ->
            claimsBuilder.claim(key, value)
        }

        val claims = claimsBuilder.build()

        return AuthDirectPost(
            idToken = signJWT(keyHolder, didHolder, claims).serialize(),
            presentationSubmission = getPresentationSubmission(),
            state = state
        )
    }

    private fun getProperties(now: Date): Map<String, Any> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val properties = mutableMapOf<String, Any>(
            "issued" to dateFormat.format(now),
            "validFrom" to dateFormat.format(now)
        )

        properties["credentialSchema"] = mutableMapOf<String, Any>(
            "id" to CREDENTIAL_SCHEMA.ID,
            "type" to CREDENTIAL_SCHEMA.TYPE
        )
        properties["termsOfUse"] = mutableMapOf<String, Any>(
            "id" to TERMS_OF_USE.URL,
            "type" to TERMS_OF_USE.TYPE
        )
        val initIndex = "1"
        val revokedStatusListEntry = StatusList2021Entry.builder()
            .id(URI("http://localhost/status/v1#$initIndex"))
            .statusListIndex(initIndex)
            .statusListCredential(URI("http://localhost:9996/status/v1"))
            .statusPurpose("revocation")
            .build()

        val suspendedExpectedStatusListEntry = StatusList2021Entry.builder()
            .id(URI("http://localhost/status/v2#$initIndex"))
            .statusListIndex(initIndex)
            .statusListCredential(URI("http://localhost:9996/status/v2"))
            .statusPurpose("suspension")
            .build()
        properties["credentialStatus"] = arrayOf( revokedStatusListEntry.jsonObject,
            suspendedExpectedStatusListEntry.jsonObject)
        return properties
    }

    private fun getPresentationSubmission(): String {
        return Json.encodeToString(
            PresentationSubmission(
                id = "holder-wallet-qualification-presentation",
                definitionId = "holder-wallet-qualification-presentation",
                descriptorMap = arrayListOf(
                    DescriptorMap(
                        id = "same-device-in-time-credential",
                        path = "$",
                        format = "jwt_vp",
                        pathNested = PathNested(
                            id = "same-device-in-time-credential",
                            format = "jwt_vc",
                            path = "$.verifiableCredential[0]"
                        )
                    )
                )
            )
        )
    }

    /**
     * Generates a mock of a credential based on the provided parameters. It can be used when issuing a credential or
     * presenting one.
     */
    private fun generateCredential(
        credentialType: String,
        now: Date,
        expireIn: Date,
        didIssuer: String,
        didHolder: String,
        subjectClaims: Map<String, Any?>
    ): JWTClaimsSet {
        val verifiableCredential = VerifiableCredential.builder()
            .types(CredentialPatternFactory.getCredSpec(credentialType)!!.types)
            .id(URI("vc:uself:agent${createUUID()}"))
            .issuer(URI.create(didIssuer))
            .issuanceDate(now)
            .expirationDate(expireIn)
        verifiableCredential.properties(getProperties(now))
        val credentialSubject = CredentialSubject.builder()
            .id(URI.create(didHolder))
            .claims(subjectClaims)
            .build()
        verifiableCredential.credentialSubject(credentialSubject)
        return verifiableCredential.build().toJWTClaimsSet()
    }

    fun getIssueCredentialRequest(
        credentialType: String,
        nonce: String,
        didHolder: String,
        keyHolder: String
    ): CredentialRequest {
        val now = Date.from(Instant.now())
        val expireIn = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS))

        val claimSet = JWTClaimsSet.Builder()
            .issuer(didHolder)
            .audience("http://localhost/issuer")
            .issueTime(now)
            .expirationTime(expireIn)
            .claim("nonce", nonce)
            .build()

        return CredentialRequest(
            format = "jwt_vc",
            types = arrayListOf("VerifiableCredential", credentialType),
            proof = CredentialRequestProof(
                proofType = "jwt",
                jwt = signJWT(keyHolder, didHolder, claimSet, "openid4vci-proof+jwt").serialize()
            )

        )
    }

    /**
     * Generates the response of the credential issuance based on the provided parameters.
     */
    fun getCredentialResponseExpected(
        credentialType: String,
        didIssuer: String,
        didHolder: String,
        keyIssuer: String,
        subjectClaims: Map<String, Any?>
    ): CredentialResponse {
        val now = Date.from(Instant.now())
        val expireIn = Date.from(Instant.now().plusSeconds(DEFAULT_EXP_SECONDS))

        val credential = generateCredential(credentialType, now, expireIn, didIssuer, didHolder, subjectClaims)

        return CredentialResponse(
            format = "jwt_vc",
            credential = signJWT(keyIssuer, didIssuer, credential).serialize()
        )
    }

    /**
     * Signs a JWT with the provided parameters.
     */
    fun signJWT(key: String, id: String, claims: JWTClaimsSet, type: String? = SIG_TYPE_JWT): SignedJWT {
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(id)
                .type(JOSEObjectType(type))
                .build(),
            claims
        )
        jwt.sign(ECDSASigner(JWK.parse(key).toECKey()))
        return jwt
    }

    fun jwtClaimsSetConverter(claims: Map<String, Any>): JWTClaimsSet {
        val builder = JWTClaimsSet.Builder()
        claims.forEach { (key, value) -> builder.claim(key, value) }
        return builder.build()
    }
}
