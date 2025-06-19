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
package com.eviden.bds.rd.uself.agent.mocks

import com.eviden.bds.rd.uself.common.models.ISSUER_TIR
import com.nimbusds.jwt.SignedJWT

/**
 * Generates mocks of TIR, TAO and ROOT TAO
 * The function tirString generates the response of the TIR Registry
 */
object TIRUtils {

    const val didTI = ISSUER_TIR.did
    const val didTAO = "did:ebsi:zthvUe28tbxARthBGWXwFmk"
    const val didRTAO = "did:ebsi:zZeKyEJfUTGwajhNyNX928z"

    const val didDocTAO = """
        {"@context":["https://www.w3.org/ns/did/v1","https://w3id.org/security/suites/jws-2020/v1"],"id":"did:ebsi:zthvUe28tbxARthBGWXwFmk","controller":["did:ebsi:zthvUe28tbxARthBGWXwFmk"],"verificationMethod":[{"type":"JsonWebKey2020","id":"did:ebsi:zthvUe28tbxARthBGWXwFmk#14841d4ed32d4fe3ba1bbe4e37732f30","controller":"did:ebsi:zthvUe28tbxARthBGWXwFmk","publicKeyJwk":{"kty":"EC","crv":"secp256k1","kid":"14841d4ed32d4fe3ba1bbe4e37732f30","x":"CSIzEsSt8DtsJNU6Gf7JAc-hTONQPWBXEZ9MEx7nrFg","y":"qFHGZLZwXDtUypc7Rgjx2u_QmxFDPpJGbfiCge-yC90","alg":"ES256K"}},{"type":"JsonWebKey2020","id":"did:ebsi:zthvUe28tbxARthBGWXwFmk#0e53d29fabb747f7a9514813eeb4e0e9","controller":"did:ebsi:zthvUe28tbxARthBGWXwFmk","publicKeyJwk":{"kty":"EC","crv":"P-256","kid":"0e53d29fabb747f7a9514813eeb4e0e9","x":"2o44Ggkn528LaAiNmvAskp6fDDQ8tl7iRE8MfFwX8SM","y":"DPP2ABUABExmd1WvoSSjUMW4ZHrt0l04q79mR7osH0c","alg":"ES256"}}],"authentication":["did:ebsi:zthvUe28tbxARthBGWXwFmk#14841d4ed32d4fe3ba1bbe4e37732f30","did:ebsi:zthvUe28tbxARthBGWXwFmk#0e53d29fabb747f7a9514813eeb4e0e9"],"assertionMethod":["did:ebsi:zthvUe28tbxARthBGWXwFmk#0e53d29fabb747f7a9514813eeb4e0e9","did:ebsi:zthvUe28tbxARthBGWXwFmk#14841d4ed32d4fe3ba1bbe4e37732f30"],"capabilityInvocation":["did:ebsi:zthvUe28tbxARthBGWXwFmk#14841d4ed32d4fe3ba1bbe4e37732f30"]}
    """

    const val didDocRTAO = """
        {"@context":["https://www.w3.org/ns/did/v1","https://w3id.org/security/suites/jws-2020/v1"],"id":"did:ebsi:zZeKyEJfUTGwajhNyNX928z","controller":["did:ebsi:zZeKyEJfUTGwajhNyNX928z"],"verificationMethod":[{"type":"JsonWebKey2020","id":"did:ebsi:zZeKyEJfUTGwajhNyNX928z#bcc388ae4a81427daf7e6c8f3e2ba609","controller":"did:ebsi:zZeKyEJfUTGwajhNyNX928z","publicKeyJwk":{"kty":"EC","crv":"secp256k1","kid":"bcc388ae4a81427daf7e6c8f3e2ba609","x":"jg43rtSdcLLgO62IbiCwU3nSf77HKlmHL0hh9TBUmqI","y":"nv0yi0Swef5ql_47S7ESG_-fXTEEeE1XHyMF_tF0Fs0","alg":"ES256K"}},{"type":"JsonWebKey2020","id":"did:ebsi:zZeKyEJfUTGwajhNyNX928z#556010596ee248f38a8a431aa57fc7e0","controller":"did:ebsi:zZeKyEJfUTGwajhNyNX928z","publicKeyJwk":{"kty":"EC","crv":"P-256","kid":"556010596ee248f38a8a431aa57fc7e0","x":"uiCjIOJm2Sk3EbRv9KF0jwLMfSWzhsGtIZ7_CwuEs0A","y":"yOoM2dH8wHFV6Orf1zyxvn-c__KfOvSyJ599iJ8PIns","alg":"ES256"}}],"authentication":["did:ebsi:zZeKyEJfUTGwajhNyNX928z#bcc388ae4a81427daf7e6c8f3e2ba609","did:ebsi:zZeKyEJfUTGwajhNyNX928z#556010596ee248f38a8a431aa57fc7e0"],"assertionMethod":["did:ebsi:zZeKyEJfUTGwajhNyNX928z#556010596ee248f38a8a431aa57fc7e0","did:ebsi:zZeKyEJfUTGwajhNyNX928z#bcc388ae4a81427daf7e6c8f3e2ba609"],"capabilityInvocation":["did:ebsi:zZeKyEJfUTGwajhNyNX928z#bcc388ae4a81427daf7e6c8f3e2ba609"]}
    """

    val sigTAO = """
        {"kty":"EC","d":"xSaZRfqZ9e26bRYGSdO3TW7bNeWu3kE-FEk9G-32RJM","crv":"P-256","kid":"did:ebsi:zthvUe28tbxARthBGWXwFmk","x":"2o44Ggkn528LaAiNmvAskp6fDDQ8tl7iRE8MfFwX8SM","y":"DPP2ABUABExmd1WvoSSjUMW4ZHrt0l04q79mR7osH0c","alg":"ES256"}
    """.trim()

    val sigRTAO = """
        {"kty":"EC","d":"YuiEzvIp14z725fAbZ79kSn-qB_FcMoyDzvFfAGyJXU","crv":"P-256","kid":"did:ebsi:zZeKyEJfUTGwajhNyNX928z","x":"uiCjIOJm2Sk3EbRv9KF0jwLMfSWzhsGtIZ7_CwuEs0A","y":"yOoM2dH8wHFV6Orf1zyxvn-c__KfOvSyJ599iJ8PIns","alg":"ES256"}
    """.trim()
    //TODO make this configurable for different types of credentials
    val claimsTI = mutableMapOf(
        "iat" to 1725607413,
        "jti" to "urn:uuid:37bd5bdb-a6c9-488c-b8ef-b23fa8494cae",
        "nbf" to 1725607413,
        "exp" to 1789833600,
        "sub" to didTI,
        "vc" to mutableMapOf(
            "@context" to listOf("https://www.w3.org/2018/credentials/v1"),
            "id" to "urn:uuid:37bd5bdb-a6c9-488c-b8ef-b23fa8494cae",
            "type" to listOf(
                "VerifiableCredential",
                "VerifiableAttestation",
                "VerifiableAccreditation",
                "VerifiableAccreditationToAttest"
            ),
            "issuer" to didTAO,
            "issuanceDate" to "2024-09-06T07:23:33Z",
            "issued" to "2024-09-06T07:23:33Z",
            "validFrom" to "2024-09-06T07:23:33Z",
            "expirationDate" to "2026-09-19T16:00:00Z",
            "credentialSubject" to mutableMapOf(
                "id" to didTI,
                "accreditedFor" to listOf(
                    mutableMapOf(
                        "schemaId" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v2/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
                        "types" to listOf("VerifiableCredential", "VerifiableAttestation", "eu.europa.ec.eudi.pid"),
                        "limitJurisdiction" to "https://publications.europa.eu/resource/authority/atu/EUR"
                    )
                ),
                "reservedAttributeId" to "751d95a5de9dfb24bb3669a03f1070a31d3f0f893e8ac801ed47bb713646ef3e"
            ),
            "credentialSchema" to mutableMapOf(
                "id" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v3/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
                "type" to "FullJsonSchemaValidator2021"
            ),
            "termsOfUse" to mutableMapOf(
                "id" to "https://api-pilot.ebsi.eu/trusted-issuers-registry/v5/issuers/did:ebsi:zZeKyEJfUTGwajhNyNX928z/attributes/cf89e47e260eda53bb199096ca97347790c84d2c8a5f2ce3f411eaba14d0003e",
                "type" to "IssuanceCertificate"
            )
        ),
        "iss" to didTAO
    )

    val claimsTAO = mutableMapOf(
        "iat" to 1725607413,
        "jti" to "urn:uuid:37bd5bdb-a6c9-488c-b8ef-b23fa8494caf",
        "nbf" to 1725607413,
        "exp" to 1789833600,
        "sub" to didTAO,
        "vc" to mutableMapOf(
            "@context" to listOf("https://www.w3.org/2018/credentials/v1"),
            "id" to "urn:uuid:37bd5bdb-a6c9-488c-b8ef-b23fa8494caf",
            "type" to listOf(
                "VerifiableCredential",
                "VerifiableAttestation",
                "VerifiableAccreditation",
                "VerifiableAccreditationToAttest"
            ),
            "issuer" to didRTAO,
            "issuanceDate" to "2024-09-06T07:23:33Z",
            "issued" to "2024-09-06T07:23:33Z",
            "validFrom" to "2024-09-06T07:23:33Z",
            "expirationDate" to "2026-09-19T16:00:00Z",
            "credentialSubject" to mutableMapOf(
                "id" to didTAO,
                "accreditedFor" to listOf(
                    mutableMapOf(
                        "schemaId" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v2/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
                        "types" to listOf("VerifiableCredential", "VerifiableAttestation", "VerifiableAccreditation","VerifiableAccreditationToAccredit"),
                        "limitJurisdiction" to "https://publications.europa.eu/resource/authority/atu/EUR"
                    )
                ),
                "reservedAttributeId" to "751d95a5de9dfb24bb3669a03f1070a31d3f0f893e8ac801ed47bb713646ef3e"
            ),
            "credentialSchema" to mutableMapOf(
                "id" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v3/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
                "type" to "FullJsonSchemaValidator2021"
            ),
            "termsOfUse" to mutableMapOf(
                "id" to "https://api-pilot.ebsi.eu/trusted-issuers-registry/v5/issuers/did:ebsi:zZeKyEJfUTGwajhNyNX928z/attributes/cf89e47e260eda53bb199096ca97347790c84d2c8a5f2ce3f411eaba14d0003e",
                "type" to "IssuanceCertificate"
            )
        ),
        "iss" to didRTAO
    )

    val claimsRTAO = mutableMapOf(
        "iat" to 1725607413,
        "jti" to "urn:uuid:bfa61bbc-6988-4521-9ee3-8b0720aaa767",
        "nbf" to 1725607413,
        "exp" to 1789833600,
        "sub" to didRTAO,
        "vc" to mutableMapOf(
            "@context" to listOf("https://www.w3.org/2018/credentials/v1"),
            "id" to "urn:uuid:bfa61bbc-6988-4521-9ee3-8b0720aaa767",
            "type" to listOf(
                "VerifiableCredential",
                "VerifiableAttestation",
                "VerifiableAccreditation",
                "VerifiableAccreditationToAttest"
            ),
            "issuer" to didRTAO,
            "issuanceDate" to "2024-09-06T07:23:33Z",
            "issued" to "2024-09-06T07:23:33Z",
            "validFrom" to "2024-09-06T07:23:33Z",
            "expirationDate" to "2026-09-19T16:00:00Z",
            "credentialSubject" to mutableMapOf(
                "id" to didRTAO,
                "accreditedFor" to listOf(
                    mutableMapOf(
                        "schemaId" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v3/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
                        "types" to listOf("VerifiableCredential", "VerifiableAttestation", "VerifiableAuthorisationForTrustChain")
                    )
                ),
                "reservedAttributeId" to "cf89e47e260eda53bb199096ca97347790c84d2c8a5f2ce3f411eaba14d0003e"
            ),
            "credentialSchema" to mutableMapOf(
                "id" to "https://api-pilot.ebsi.eu/trusted-schemas-registry/v3/schemas/zjVFNvbEBPAr3a724DttioZpgZmNr75BBtRzZqk7pkDe",
                "type" to "FullJsonSchemaValidator2021"
            )
        ),
        "iss" to didRTAO
    )

    fun tirString(
        jwtTI: SignedJWT?,
        jwtTAO: SignedJWT?,
        jwtRTAO: SignedJWT?
    ): Triple<String?, String?, String?> {
        val tiString = jwtTI?.let {
            """
        {"did": "$didTI",
        "attributes": [
            {
                "hash": "96e01cedbd6227ba5da8703314f5ec56195e588d49648734c7f617932efff427",
                "tao": "$didTAO",
                "rootTao": "$didRTAO",
                "issuerType": "TI",
                "body": "${it.serialize()}"
            }
        ]
        } """.trimIndent()
        }

        val taoString = jwtTAO?.let {
            """
        {"did": "$didTAO",
        "attributes": [
            {
                "hash": "96e01cedbd6227ba5da8703314f5ec56195e588d49648734c7f617932efff427",
                "tao": "$didTAO",
                "rootTao": "$didRTAO",
                "issuerType": "TAO",
                "body": "${it.serialize()}"
            }
        ]
        } """.trimIndent()
        }

        val rtaoString = jwtRTAO?.let {
            """
        {"did": "$didRTAO",
        "attributes": [
            {
                "hash": "96e01cedbd6227ba5da8703314f5ec56195e588d49648734c7f617932efff427",
                "tao": "$didRTAO",
                "rootTao": "$didRTAO",
                "issuerType": "RootTAO",
                "body": "${it.serialize()}"
            }
        ]
        } """.trimIndent()
        }

        return Triple(tiString, taoString, rtaoString)
    }

    fun proxiesString(): String {
        val proxies = """
            {
              "items": [
                {
                  "href": "https://api-pilot.ebsi.eu/trusted-issuers-registry/v5/issuers/did:ebsi:zZeKyEJfUTGwajhNyNX928z/proxies/0x408db59ec66f14efe30eed366ad57a7f6863fbbb18edd3d3bf15837dc3a494a6",
                  "proxyId": "0x408db59ec66f14efe30eed366ad57a7f6863fbbb18edd3d3bf15837dc3a494a6"
                }
              ],
              "total": 1
            }
        """.trimIndent()
        return proxies
    }

}
