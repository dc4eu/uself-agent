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
@file:Suppress("NoWildcardImports", "ClassNaming", "MaxLineLength")

package com.eviden.bds.rd.uself.agent.mocks

import com.danubetech.verifiablecredentials.VerifiableCredential
import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential
import com.eviden.bds.rd.uself.agent.config.WebSecurityConfig
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.ID_TOKEN_PASSPORT
import com.eviden.bds.rd.uself.agent.mocks.TestsHelper.NONCE
import com.eviden.bds.rd.uself.agent.models.entities.rclientsession.RClientSession
import com.eviden.bds.rd.uself.common.models.*
import com.eviden.bds.rd.uself.common.models.openid.auth.*
import com.eviden.bds.rd.uself.common.models.openid.issuer.*
import com.eviden.bds.rd.uself.common.services.Utils.createUUID
import com.nimbusds.jwt.SignedJWT
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.handler.HandlerMappingIntrospector
import java.util.*

class TestWebSecurityConfig : WebSecurityConfig() {
    @Bean(name = ["mvcHandlerMappingIntrospector"])
    fun mvcHandlerMappingIntrospector(): HandlerMappingIntrospector {
        return HandlerMappingIntrospector()
    }
}
object TestsHelper {

    const val REDIRECT_URI = END_POINT.OPEN_ID
    val ID_TOKEN = """
            eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtib2o3ZzlQZlhKeGJiczRLWWVneXI3RUxuRlZucERNemJKSkRETlpqYXZYNmp2dERtQUxNYlhBR1c2N3BkVGdGZWEyRnJHR1NGczhFanhpOTZvRkxHSGNMNFA2YmpMRFBCSkV2UlJIU3JHNExzUG5lNTJmY3p0Mk1XakhMTEpCdmhBQyN6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIn0.eyJub25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJpc3MiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJpYXQiOjE3MTE0NTQzMjQsImV4cCI6MTcxMTQ1NDYyNH0.Ih2jrQZ36tBoUbPnda1z4sjCeFQH7mxb5JH-1CIqolUKjKQLFvzXusmP5cc0utk3xTgBOZpNSMjFVZfa6aBkkQ
            """.trim()
    val ID_TOKEN_PASSPORT = """
eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJwYXNzcG9ydE51bWJlciI6IjVlOTEyZjlkLWEwNTYtNDhjYy05YmUwLWU2ODE2Y2JjZmFiMSIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImRhdGVPZkV4cGlyeSI6IjI1LzA1LzIwMjkiLCJwbGFjZU9mQmlydGgiOiJMb25kb24iLCJhZGRyZXNzIjoidGVzdCBzdHJlZXQgMTIzIiwiZ2VuZGVyIjoibWFsZSIsImdpdmVuTmFtZSI6IkphbWVzIiwiaXNzIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JwaXI2Y3pCem5SZzVtVTN1ZlBDbUpQQnhRRkNFTGdqdGh5UW9Bc0RQVDFmaGRtN1c4NmpDUUN1Y0RjZEdkdjJtM3U3RnRQaW9hUmo0TlBoSks5eW1HanM0cjhHYlAzcFdBWXNpWWJpaDRGV0FnUnJkRWQxcXlyZ2NaQUgxYjdzelk0IiwiZGF0ZU9mQmlydGgiOiIxMS8xMS8xOTgwIiwiZGF0ZU9mSXNzdWUiOiIyNi8wNS8yMDI0Iiwibm9uY2UiOiI3MjY4NjU5ODk4MDc1NDc5NzUzIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdC9hdXRoIiwibmJmIjoxNzQ4NTAzMTcwLCJuYXRpb25hbGl0eSI6IkJyaXRpc2giLCJjb3VudHJ5Q29kZSI6IkdCUiIsImZhbWlseU5hbWUiOiJCb25kIiwiYXV0aG9yaXR5IjoiQnJpdGlzaCBHb3Zlcm5tZW50Iiwic3RhdGUiOiI3MTQ5ODI0MjgzNDAxNjY1MDE1IiwiZXhwIjoxNzQ4NTAzNzcwLCJpYXQiOjE3NDg1MDMxNzAsImp0aSI6InVybjp1dWlkOmE0YTZlYjc4LTNlYTktNDJkNC1iMzIxLTQ2YjkxNjQ5NWQ1MyJ9.UHYHLoLIRTyBaGM3KMu8fHyM9V-WES-Ggpep8Jm0auaSldcKxXEzgiRJ6a1n41KlkMMAB859YnubgAEGy2runQ
    """.trim()
    const val STATE = "8e8a74d4-2ecc-4921-9099-b6a053d05549"
    const val KEYCLOAK_STATE = "8e8a74d4-2ecc-4921-9099-b6a053d05541"
    const val EPASSPORT_STATE = "95d86046-ef6d-40b5-8da0-1b27f2f9de4a"
    const val NONCE = "8661435939417066112"

    val TANGO_ISSUER_STATE = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsImNyZWRlbnRpYWxfdHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJDVFdhbGxldFNhbWVBdXRob3Jpc2VkSW5UaW1lIl0sImV4cCI6MTcxMjUzMTcyOSwiaWF0IjoxNzExNTMxNzI5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQifQ.X9hEgEuwkJVABBNzifmsT8gbh8Zcz6o0yUiURGFWiK6v2dAueTLBsLfOu6skgcM7R07cfqOiiSi2l-YaEXF5Dw
    """.trim()

    val ISSUER_STATE = """
        eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsImNyZWRlbnRpYWxfdHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJDVFdhbGxldFNhbWVBdXRob3Jpc2VkSW5UaW1lIl0sImV4cCI6MTcxMjUzMTcyOSwiaWF0IjoxNzExNTMxNzI5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQifQ.X9hEgEuwkJVABBNzifmsT8gbh8Zcz6o0yUiURGFWiK6v2dAueTLBsLfOu6skgcM7R07cfqOiiSi2l-YaEXF5Dw
    """.trim()

    val ISSUER_STATE_EPASSPORT = """
        eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvYXV0aCIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticDZxenRqc2JpMmFIZ1A2YU5CNFo0V2pjM0o3VTVMSlVINmlxVmo1YjY1enNnYmhoa01ZczdaenRYNHhxVjhBTmRrUDVrcmlFTlJTR0NkTEVKazUxN3hEbW9HbkRlb0ZaQUs5enh5ckVOSE1iV3g1MUZDNlF1S0I3bm5VUlZSa1VzZSIsImlzcyI6Imh0dHBzOi8vdXNlbGYtYWdlbnQuazhzLWNsdXN0ZXIudGFuZ28ucmlkLWludHJhc29mdC5ldS9pc3N1ZXIiLCJjcmVkZW50aWFsX3R5cGVzIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiZVBhc3Nwb3J0Il0sImV4cCI6MTczMzEzNTA5OSwiaWF0IjoxNzMzMTMxNDk5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnA2cXp0anNiaTJhSGdQNmFOQjRaNFdqYzNKN1U1TEpVSDZpcVZqNWI2NXpzZ2JoaGtNWXM3Wnp0WDR4cVY4QU5ka1A1a3JpRU5SU0dDZExFSms1MTd4RG1vR25EZW9GWkFLOXp4eXJFTkhNYld4NTFGQzZRdUtCN25uVVJWUmtVc2UifQ.JnVhCsge-E3YCzPNWpJGiUUgoTr9Q_SfMTiCehAwstCDXKmRgAIk3WV3wwBQSZfVLJbb3WZmo8i46SjDIy9xIQ
    """.trimIndent()

    const val CODE_CHALLENGE_TEST = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg"

    const val CODE_CHALLENGE = "BQTq_NqjzRUrDMRPq_8P8YPWc3DHm_kxAVBzitPo4B0"

    // "BQTq_NqjzRUrDMRPq_8P8YPWc3DHm_kxAVBzitPo4B0"
    val authDetails = arrayListOf(
        AuthorizationDetails(
            type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
            format = FORMAT.JWT_VC,
            locations = arrayListOf("issuerURL"),
            types = arrayListOf(
                CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                "CTWalletCrossInTime"
            )
        )
    )

    const val didDocKeyP256 = """
    {
      "@context": [
        "https://www.w3.org/ns/did/v1",
        "https://w3id.org/security/suites/jws-2020/v1"
      ],
      "id": "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP",
      "verificationMethod": [
        {
          "id": "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP#zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP",
          "type": "JsonWebKey2020",
          "controller": "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP",
          "publicKeyJwk": {
            "kty": "EC",
            "crv": "P-256",
            "x": "pBxD7lr2XGpDON73lcEL0hqMQDw2NirEunMrs4Ju5F4",
            "y": "4pf7ho7u6TuGCBqSdlcxG0OM6agOwzOAe9RvipWPIT4"
          }
        }
      ],
      "assertionMethod": [
        "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP#zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP"
      ],
      "authentication": [
        "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP#zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP"
      ],
      "capabilityInvocation": [
        "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP#zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP"
      ],
      "capabilityDelegation": [
        "did:key:zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP#zDnaebUauYBmTn64xCaiJFw6s4UYys6KwXATApG5NhyTbVzjP"
      ]
    }
    """

    const val didDocJwk = """
        {
        "@context": [
        "https://www.w3.org/ns/did/v1",
        "https://w3id.org/security/suites/jws-2020/v1"
        ],
        "id": "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9",
        "verificationMethod": [{
        "id": "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9#0",
        "type": "JsonWebKey2020",
        "controller": "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9",
        "publicKeyJwk": {
          "crv": "P-256",
          "kty": "EC",
          "x": "acbIQiuMs3i8_uszEjJ2tpTtRM4EU3yz91PH6CdH2V0",
          "y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"
        }
        }],
        "assertionMethod": [
        "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9#0"
        ],
        "authentication": [
        "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9#0"
        ],
        "capabilityInvocation": [
        "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9#0"
        ],
        "capabilityDelegation": [
        "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9#0"
        ]
        }
        """

    const val didDocKey = """{"@context":["https://www.w3.org/ns/did/v1","https://w3id.org/security/suites/jws-2020/v1"],"id":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW","verificationMethod":[{"id":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW","type":"JsonWebKey2020","controller":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW","publicKeyJwk":{"crv":"P-256","kty":"EC","x":"Sf2MSKpd6Q-k0pV1-OXshp6AI8Tzf2XP1oqmrd6T2sI","y":"w2YMb3uvS--MAam79Rp_bPexf7qi5Le7z2dJvdmJNxU"}}],"authentication":["did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW"],"assertionMethod":["did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW"],"capabilityDelegation":["did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW"],"capabilityInvocation":["did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbq8JwHdDnPH4qAhhvKSp31VZpUQ5JukjcmoiXs8iuReuYjagFkRdU5rySFhrBKzFDuSE6VFPh5PkqbXcyxRCbfWMa5yWBXS5yXxtpmYFXWyQUjMops3hnqzKnBentfsrUXW"]}"""

    const val didDocEBSI = """
        {
          "@context": [
            "https://www.w3.org/ns/did/v1",
            "https://w3id.org/security/suites/jws-2020/v1"
          ],
          "id": "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N",
          "controller": [
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N"
          ],
          "verificationMethod": [
            {
              "type": "JsonWebKey2020",
              "id": "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#067e923a529a422d9e02aac955468f93",
              "controller": "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N",
              "publicKeyJwk": {
                "kty": "EC",
                "crv": "secp256k1",
                "kid": "067e923a529a422d9e02aac955468f93",
                "x": "WiUNSLX1nuN6RVVWJwp5QAmPhBLqPdtjqLR03L0NCjE",
                "y": "AEmWe-vmxAqxiAFChQKWpN8v73m7ZioVn3ZB6e0XPrI",
                "alg": "ES256K"
              }
            },
            {
              "type": "JsonWebKey2020",
              "id": "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#008bbe5d11964456b9e48209e9b36878",
              "controller": "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N",
              "publicKeyJwk": {
                "kty": "EC",
                "crv": "P-256",
                "kid": "008bbe5d11964456b9e48209e9b36878",
                "x": "mmJ6SOXsPxh_Xd7HiVo54pPyxOBZ3jKFYXmOYDUNdWw",
                "y": "UgOId3u3Rld6-NFJgg_u9usdWJKawB4Y5aJ43xxQLGs",
                "alg": "ES256"
              }
            }
          ],
          "authentication": [
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#067e923a529a422d9e02aac955468f93",
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#008bbe5d11964456b9e48209e9b36878"
          ],
          "assertionMethod": [
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#008bbe5d11964456b9e48209e9b36878",
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#067e923a529a422d9e02aac955468f93"
          ],
          "capabilityInvocation": [
            "did:ebsi:zhaAe7CV8i9M1Y5XGcUoS1N#067e923a529a422d9e02aac955468f93"
          ]
        }
        """
    const val didDocWeb = """
        {
          "@context": [
            "https://www.w3.org/ns/did/v1",
            "https://w3id.org/security/suites/jws-2020/v1"
          ],
          "id": "did:web:w3c-ccg.gitlab.io:user:alice",
          "controller": [
            "did:web:w3c-ccg.gitlab.io:user:alice"
          ],
          "verificationMethod": [
            {
              "type": "JsonWebKey2020",
              "id": "did:web:w3c-ccg.gitlab.io:user:alice#5fcfd518fdf0466cbb9f0f072196d159",
              "controller": "did:web:w3c-ccg.gitlab.io:user:alice",
              "publicKeyJwk": {
                "kty": "EC",
                "crv": "secp256k1",
                "kid": "5fcfd518fdf0466cbb9f0f072196d159",
                "x": "L_WJ70M0NogjVl9jTVD9URqux-h7F0Mef18tdOILJIM",
                "y": "APNAgvHdF95ge4sSXlth8fK-4ziaVuDhJTcs2IhmKzY",
                "alg": "ES256K"
              }
            },
            {
              "type": "JsonWebKey2020",
              "id": "did:web:w3c-ccg.gitlab.io:user:alice#c54b3e3e11c04b82b1ad1a181227775c",
              "controller": "did:web:w3c-ccg.gitlab.io:user:alice",
              "publicKeyJwk": {
                "kty": "EC",
                "crv": "P-256",
                "kid": "c54b3e3e11c04b82b1ad1a181227775c",
                "x": "4ar_IDcoFFiPJZrcmqgnJ64aNbyEneFBtE97Yjstb3Y",
                "y": "_s9hU-OHTjlLN-rptW4Xuqqvf7Wm222mAkcaWOZDuGY",
                "alg": "ES256"
              }
            }
          ],
          "authentication": [
            "did:web:w3c-ccg.gitlab.io:user:alice#5fcfd518fdf0466cbb9f0f072196d159",
            "did:web:w3c-ccg.gitlab.io:user:alice#c54b3e3e11c04b82b1ad1a181227775c"
          ],
          "assertionMethod": [
            "did:web:w3c-ccg.gitlab.io:user:alice#c54b3e3e11c04b82b1ad1a181227775c",
            "did:web:w3c-ccg.gitlab.io:user:alice#5fcfd518fdf0466cbb9f0f072196d159"
          ],
          "capabilityInvocation": [
            "did:web:w3c-ccg.gitlab.io:user:alice#5fcfd518fdf0466cbb9f0f072196d159"
          ]
        }
        """
    val mockRClientSession = RClientSession(
        id = createUUID(),
        clientId = HOLDER_DID.did,
        authorizationDetails = Json.encodeToString(authDetails),
        state = STATE,
        code = STATE,
        nonce = NONCE,
        codeChallenge = CODE_CHALLENGE_TEST,
        userInfo = MOCKS_AUTH.IssueCredential.expectedCredential.credentialSubject.jsonObject
    )
    val clientSessionKeycloak =
        ClientSession(
            clientId = "keycloak-client",
            issuerState = ISSUER_STATE,
            authorizationDetails = authDetails,
            state = KEYCLOAK_STATE,
            code = STATE,
            nonce = "uyHiL1tOMHizr1w4gO496g",
            codeChallenge = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg",
            // request = authRequest.request
        )

    val clientSessionEPassport =
        ClientSession(
            clientId = "ePassport",
            issuerState = ISSUER_STATE_EPASSPORT,
            authorizationDetails = arrayListOf(
                AuthorizationDetails(
                    type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
                    format = FORMAT.JWT_VC,
                    locations = arrayListOf("http://localhost/issuer"),
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
                    )
                )
            ),
            state = EPASSPORT_STATE,
            code = "RXhjhR1RNT0FBaExIoptLmCyhPiebxygAOtg7aoEBrGH",
            nonce = "5583195896156095755",
            // request = authRequest.request
        )

    val clientSessionPresent =
        ClientSession(
            clientId = "present",
            issuerState = ISSUER_STATE,
            authorizationDetails = authDetails,
            state = STATE,
            code = STATE,
            nonce = "uyHiL1tOMHizr1w4gO496g",
            codeChallenge = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg",
            redirectURI = "http://localhost:8080/redirect",
            request = " openid://redirect?request_uri=https%3A%2F%2Fuself-agent.dev5.ari-bip.eu%2Fauth%2Frequest_uri%2F6033695429226360218"
            // request = authRequest.request
        )
    val JWKS_VALUE = """
                {"keys":[{"kty":"EC","crv":"P-256","alg":"ES256","x":"N7Fpbhd_R-3p-lMsaWcKFfUg7au6KxX8mSrnyTElNsI","y":"9FRC3snOiFoG4Z-yPSiFIvTbvc0ytB2L0Rp06dGKGe8","kid":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4#z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4"}]}
        """.trim()

    val bearerToken = """
            eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IkJKeTViQ1Mta1dGRUhmekczYWVZUWlGbmVOYnFDWHRkLVlaVmk3cmxpRGsifQ.eyJub25jZSI6ImI2MWRhNzFjLTI0YTctNDQ3MS1iYjdmLWFjNGU5MmFhNjU4ZSIsImNsYWltcyI6eyJhdXRob3JpemF0aW9uX2RldGFpbHMiOlt7InR5cGUiOiJvcGVuaWRfY3JlZGVudGlhbCIsImZvcm1hdCI6Imp3dF92YyIsImxvY2F0aW9ucyI6WyJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L2NvbmZvcm1hbmNlL3YzL2lzc3Vlci1tb2NrIl0sInR5cGVzIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiQ1RXYWxsZXRTYW1lSW5UaW1lIl19XSwiY19ub25jZSI6ImEwYjU3OWMzLWQzYzEtNDk4Mi1hYWM4LWE1MWJhN2YwY2E3MSIsImNfbm9uY2VfZXhwaXJlc19pbiI6ODY0MDAsImNsaWVudF9pZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCJ9LCJpc3MiOiJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L2NvbmZvcm1hbmNlL3YzL2F1dGgtbW9jayIsImF1ZCI6WyJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L2NvbmZvcm1hbmNlL3YzL2lzc3Vlci1tb2NrIl0sInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImlhdCI6MTY5MzkwMzg4MCwiZXhwIjoxNjkzOTkwMjgwfQ.mP0OsZ_eyyL7wWiWfzUltX2BEnLCh_SsGXUS4wwz5JR7v8Yyrxk3Hz9psT-2ywNviKzaDeYq-uNYFcXMAcYnXw
    """.trim()
    private val openid4vciProofJwt = """
        eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdWQiOiJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L2NvbmZvcm1hbmNlL3YzL2lzc3Vlci1tb2NrIiwiaWF0IjoxNjkzOTAzODgwLCJub25jZSI6ImEwYjU3OWMzLWQzYzEtNDk4Mi1hYWM4LWE1MWJhN2YwY2E3MSJ9.uheuqAlKXKqIfIAGzWPzZUO3kxkfKx8izw5XskVyAFFUs0F6sRjQD9D47LO3rHKE2DKnOOS8_y1FXXgo5yijVA
    """.trim()

    val sameInTimeCredentialRequest = CredentialRequest(
        format = FORMAT.JWT_VC,
        types = arrayListOf(
            CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
            CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
            CREDENTIAL_TYPES.EBSI_SAME_IN_TIME
        ),
        proof = CredentialRequestProof(
            proofType = PROOF_TYPE.JWT,
            jwt = openid4vciProofJwt
        )
    )
    val userInfoSameInTimeCredentialRequest = CredentialRequest(
        format = FORMAT.JWT_VC,
        types = arrayListOf(
            CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
            CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
            CREDENTIAL_TYPES.USER_INFO_SAME_IN_TIME
        ),
        proof = CredentialRequestProof(
            proofType = PROOF_TYPE.JWT,
            jwt = openid4vciProofJwt
        )
    )
    val sdCredentialRequest = CredentialRequest(
        format = FORMAT.SD_JWT,
        types = arrayListOf(
            CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
            CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
            CREDENTIAL_TYPES.SD_JWT_VC_EXAMPLE
        ),
        proof = CredentialRequestProof(
            proofType = PROOF_TYPE.JWT,
            jwt = openid4vciProofJwt
        )
    )

    val defaultAuthorisationRequest = AuthorisationRequest(
        scope = SCOPE.VER_TEST_VP_TOKEN,
        responseType = RESPONSE_TYPE.CODE,
        clientId = HOLDER_DID.did,
        redirectUri = END_POINT.OPEN_ID,
        state = STATE,
        nonce = NONCE,
        request = "test_request",
        authorizationDetails =
        arrayListOf(
            AuthorizationDetails(
                type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
                format = FORMAT.JWT_VC,
                locations = arrayListOf("issuerURL"),
                types = arrayListOf(
                    CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                    CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                    "CTWalletCrossInTime"
                )
            )
        ),
        clientMetadata = ClientMetadata(
            authorizationEndpoint = "openid://",
            responseTypesSupported = arrayListOf(RESPONSE_TYPE.VP_TOKEN, RESPONSE_TYPE.ID_TOKEN),
            vpFormatsSupported = CredentialFormats(
                jwtVp = Format(alg = arrayListOf("ES256")),
                jwtVc = Format(alg = arrayListOf("ES256"))
            )

        ),
        issuerState = "test_issuer_state",
        codeChallenge = "test_code_challenge",
        codeChallengeMethod = "null",
        redirect = false
    )
}

object MOCKS_OPENID_CLIENT {
    const val expectedAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJOeTVlRTg3Q08yQVFpc3hfNnlpX2hjWVF3OFhFT3JGZFBoejcwRmY4U3NvIn0.eyJleHAiOjE3MzIyMDEwMTIsImlhdCI6MTczMjIwMDk1MiwianRpIjoiNGVmMzg5YTgtNmJiNi00Yjc0LTlhZDctOTY4M2QyMjgwYzI0IiwiaXNzIjoiaHR0cHM6Ly9rZXljbG9hay5kZXY1LmFyaS1iaXAuZXUvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4YWUwZjY1Zi00NjA5LTQzYjgtYjA5Zi0xYmQyOGQ4YzhhOGYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJrZXljbG9hay1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiYmYwOThkNzItN2NjNC00ZjFhLTljOWMtMTBkOTdiODIxYzI0IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIvKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1tYXN0ZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZ3JvdXBzIHByb2ZpbGUgZW1haWwiLCJzaWQiOiJiZjA5OGQ3Mi03Y2M0LTRmMWEtOWM5Yy0xMGQ5N2I4MjFjMjQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJOZXcgVXNlciIsImdyb3VwcyI6WyJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXSwicHJlZmVycmVkX3VzZXJuYW1lIjoibmV3dXNlciIsImdpdmVuX25hbWUiOiJOZXciLCJmYW1pbHlfbmFtZSI6IlVzZXIiLCJlbWFpbCI6Im5ld3VzZXJAZXhhbXBsZS5jb20ifQ.vGS-6TQOOdKNiPyPz_nVZ2xDDfvvMdiDmnJXidbzjozySoCcZ014Qeu5pJ4JrwHrMcStSdPTxjFukvo0s-NmdJyrPqxU4EkUZtkh99AfUltkuSlN-ys0CEf6Z4HxQsfP8Oq109UNc0yhyjZwlZTdRGOY6uTBiwMbwqxvAw_n5jWl7lcFefkXR_PGHbcCDUDHMHzICdYHB0fZ2GKa65OAYK3Meqg0cVpu0mhUoXReGssCGteI_M6ZdexgFYwdGIWMHCNvywLu0ZSYEQ4uVqpjIzwm4n3uPInI-wssoFoflMx91JG2sMEFgDbrc3PPo9B_jbeUStoyLfPxpIiqsV8X_g"

    val mockAcessTokenResponse = "{\"access_token\":\"$expectedAccessToken\", \"token_type\":\"Bearer\"}"

    val expectedUserInfo = """
            {"sub":"8ae0f65f-4609-43b8-b09f-1bd28d8c8a8f","email_verified":false,"name":"New User","groups":["default-roles-master","offline_access","uma_authorization"],"preferred_username":"newuser","given_name":"New","family_name":"User","email":"newuser@example.com"}
    """.trimIndent()
}

object KEYS {
    const val didKeyPublic = """
        {"kty":"EC","crv":"P-256","kid":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbo6o8gKpzNFBh687xavq8GA8oSBTxRfbm82UBPzvHmiLN3faza6yfHRU7JwpZcToC97sxnrAvxmsTEiGXtq9Q6GrYiNUReAwv5RxmeakoGY45S1WE9CKQrcMCuUnxNrYDLx","x":"9pEXsg9tQK-NEhCqyZp4f4hEYzQnN5ucWpNWiNOJhmc","y":"DwU5Tblt3mJtQAsiJJNhQrE8uIn3v6Fdp0STwsfym5w","alg":"ES256"}
    """
    const val didKeyPrivate = """
        {"kty":"EC","d":"euGatcjojKD83aRDNtzakkUscD3bmdo1V9l8KI0j0Hw","crv":"P-256","kid":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbo6o8gKpzNFBh687xavq8GA8oSBTxRfbm82UBPzvHmiLN3faza6yfHRU7JwpZcToC97sxnrAvxmsTEiGXtq9Q6GrYiNUReAwv5RxmeakoGY45S1WE9CKQrcMCuUnxNrYDLx","x":"9pEXsg9tQK-NEhCqyZp4f4hEYzQnN5ucWpNWiNOJhmc","y":"DwU5Tblt3mJtQAsiJJNhQrE8uIn3v6Fdp0STwsfym5w","alg":"ES256"}
    """
    const val didEbsiPublic = """
        {
        "kty": "EC",
        "x": "53fxAncDcRfEW5R_mzgi80UrwG_R9elH1H3GORLKy0E",
        "y": "HvK8UdAIUgm8G4TaLB6z2ChYxe6xT2AWy_QksZxAW6c",
        "crv": "P-256"
      }
     """
    const val didEbsiPrivate = """
     {
        "kid": "5FN5QgR2jOw7QDqp_2GsRlzpAFib8l3UA34dFH7gY4I",
        "kty": "EC",
        "alg":"ES256",
        "crv": "P-256",
        "x": "53fxAncDcRfEW5R_mzgi80UrwG_R9elH1H3GORLKy0E",
        "y": "HvK8UdAIUgm8G4TaLB6z2ChYxe6xT2AWy_QksZxAW6c",
        "d": "zApC4UwuFW6redzQwfPAnL39P6dki5uwDH7M505qcDc"
      }
      """
    fun createBasicToken(username: String, password: String): String {
        val credentials = "$username:$password"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
        return "Basic $encodedCredentials"
    }
}

object MOCKS_VP {
    object Authorisation {
        val request = AuthorisationRequest(
            scope = "openid eu.europa.ec.eudi.pid",
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbo6o8gKpzNFBh687xavq8GA8oSBTxRfbm82UBPzvHmiLN3faza6yfHRU7JwpZcToC97sxnrAvxmsTEiGXtq9Q6GrYiNUReAwv5RxmeakoGY45S1WE9CKQrcMCuUnxNrYDLxz2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbo6o8gKpzNFBh687xavq8GA8oSBTxRfbm82UBPzvHmiLN3faza6yfHRU7JwpZcToC97sxnrAvxmsTEiGXtq9Q6GrYiNUReAwv5RxmeakoGY45S1WE9CKQrcMCuUnxNrYDLx
            """.trimIndent(),
            responseType = RESPONSE_TYPE.CODE,
            redirectUri = END_POINT.OPEN_ID,
            state = "8d8b6a3d-4bc0-4234-9a9a-ed1928815502",
            clientMetadata = ClientMetadata(
                authorizationEndpoint = "openid://",
                responseTypesSupported = arrayListOf(RESPONSE_TYPE.VP_TOKEN, RESPONSE_TYPE.ID_TOKEN),
                vpFormatsSupported = CredentialFormats(
                    jwtVp = Format(alg = arrayListOf("ES256")),
                    jwtVc = Format(alg = arrayListOf("ES256"))
                )
            ),
            nonce = "d527c191-6e1d-4c3d-9843-9eaf2005fba9",
            codeChallengeMethod = null,
            redirect = false
        )
        val expectedResult =
            """
         openid://?request_uri=https%3A%2F%2Ftadpole-internal-mammal.ngrok-free.app%2Fauth%2Frequest_uri%2F7055871168672907387
            """.trimIndent()
        val expectedRequestURI = """
            eyJraWQiOiJkaWQ6ZWJzaTp6amJKbkJwQVJUc2JzNE5QNVZuVDJyZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJpc3MiOiJkaWQ6ZWJzaTp6dFJvWXlKTmRHcjh0bUF0Vmg5Y2c5biIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsIm5vbmNlIjoiZDUyN2MxOTEtNmUxZC00YzNkLTk4NDMtOWVhZjIwMDVmYmE5IiwiY2xpZW50X2lkIjoiaHR0cHM6Ly90YWRwb2xlLWludGVybmFsLW1hbW1hbC5uZ3Jvay1mcmVlLmFwcC9hdXRoIiwicmVzcG9uc2VfbW9kZSI6ImRpcmVjdF9wb3N0IiwiYXVkIjoiaHR0cHM6Ly90YWRwb2xlLWludGVybmFsLW1hbW1hbC5uZ3Jvay1mcmVlLmFwcC9hdXRoIiwic2NvcGUiOiJvcGVuaWQiLCJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6ImhvbGRlci13YWxsZXQtcXVhbGlmaWNhdGlvbi1wcmVzZW50YXRpb24iLCJmb3JtYXQiOnsiand0X3ZwX2pzb24iOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdnAiOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdmNfanNvbiI6eyJhbGciOlsiRVMyNTYiXX0sImp3dF92YyI6eyJhbGciOlsiRVMyNTYiXX19LCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiI1MDIwMDkyNzU5MTc0NzM0ODc2IiwibmFtZSI6ImhvbGRlci13YWxsZXQtcXVhbGlmaWNhdGlvbi1wcmVzZW50YXRpb24gb2YgZXUuZXVyb3BhLmVjLmV1ZGkucGlkIiwicHVycG9zZSI6IlRoaXMgaXMgYSBwcmVzZW50YXRpb24gZGVmaW5pdGlvbiBmb3IgdGhlIGhvbGRlciB3YWxsZXQgcXVhbGlmaWNhdGlvbiIsImZvcm1hdCI6eyJqd3RfdmMiOnsiYWxnIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQudmMudHlwZSJdLCJmaWx0ZXIiOnsidHlwZSI6ImFycmF5IiwiY29udGFpbnMiOnsiY29uc3QiOiJldS5ldXJvcGEuZWMuZXVkaS5waWQifX19XX19XX0sInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aC9kaXJlY3RfcG9zdCIsInN0YXRlIjoiOGQ4YjZhM2QtNGJjMC00MjM0LTlhOWEtZWQxOTI4ODE1NTAyIiwiZXhwIjoxNzg5ODMzNjAwLCJpYXQiOjE3MjU2MDc0MTN9.WwEAOffirwMtq5MZORlrwep6xgW-1_Z2p7lXZDCdRurzd_82nkRfqWhBuvFSw8sz8BX0h-ys3h4WhuHtjnWM_Q
        """.trimIndent()
    }

    object DirectPost {
        const val state = "8d8b6a3d-4bc0-4234-9a9a-ed1928815502"
        const val nonce = "d527c191-6e1d-4c3d-9843-9eaf2005fba9"
        const val codeChallenge = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg"
        val request = AuthDirectPost(
            vpToken = """
              eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm82bzhnS3B6TkZCaDY4N3hhdnE4R0E4b1NCVHhSZmJtODJVQlB6dkhtaUxOM2ZhemE2eWZIUlU3SndwWmNUb0M5N3N4bnJBdnhtc1RFaUdYdHE5UTZHcllpTlVSZUF3djVSeG1lYWtvR1k0NVMxV0U5Q0tRcmNNQ3VVbnhOcllETHgiLCJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6ImQ1MjdjMTkxLTZlMWQtNGMzZC05ODQzLTllYWYyMDA1ZmJhOSIsInN0YXRlIjoiOGQ4YjZhM2QtNGJjMC00MjM0LTlhOWEtZWQxOTI4ODE1NTAyIiwidnAiOnsiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiXSwiaWQiOiJ1cm46dXVpZDoyOTdiMjlhOS00ODcyLTQ5Y2QtYTY3My1lN2UyYjIzOWE5YTciLCJ0eXBlIjpbIlZlcmlmaWFibGVQcmVzZW50YXRpb24iXSwiaG9sZGVyIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvNm84Z0twek5GQmg2ODd4YXZxOEdBOG9TQlR4UmZibTgyVUJQenZIbWlMTjNmYXphNnlmSFJVN0p3cFpjVG9DOTdzeG5yQXZ4bXNURWlHWHRxOVE2R3JZaU5VUmVBd3Y1UnhtZWFrb0dZNDVTMVdFOUNLUXJjTUN1VW54TnJZREx4IiwidmVyaWZpYWJsZUNyZWRlbnRpYWwiOlsiZXlKcmFXUWlPaUprYVdRNlpXSnphVHA2ZEZKdldYbEtUbVJIY2poMGJVRjBWbWc1WTJjNWJpTTFSazQxVVdkU01tcFBkemRSUkhGd1h6SkhjMUpzZW5CQlJtbGlPR3d6VlVFek5HUkdTRGRuV1RSSklpd2lkSGx3SWpvaVNsZFVJaXdpWVd4bklqb2lSVk15TlRZaWZRLmV5SnpkV0lpT2lKa2FXUTZhMlY1T25veVpHMTZSRGd4WTJkUWVEaFdhMmszU21KMWRVMXRSbGx5VjFCbldXOTVkSGxyVlZvelpYbHhhSFF4YWpsTFltODJiemhuUzNCNlRrWkNhRFk0TjNoaGRuRTRSMEU0YjFOQ1ZIaFNabUp0T0RKVlFsQjZka2h0YVV4T00yWmhlbUUyZVdaSVVsVTNTbmR3V21OVWIwTTVOM040Ym5KQmRuaHRjMVJGYVVkWWRIRTVVVFpIY2xscFRsVlNaVUYzZGpWU2VHMWxZV3R2UjFrME5WTXhWMFU1UTB0UmNtTk5RM1ZWYm5oT2NsbEVUSGdpTENKdVltWWlPakUzTWpVMk1EYzBNVE1zSW1semN5STZJbVJwWkRwbFluTnBPbnAwVW05WmVVcE9aRWR5T0hSdFFYUldhRGxqWnpsdUlpd2laWGh3SWpveE56ZzVPRE16TmpBd0xDSnBZWFFpT2pFM01qVTJNRGMwTVRNc0luWmpJanA3SWtCamIyNTBaWGgwSWpwYkltaDBkSEJ6T2k4dmQzZDNMbmN6TG05eVp5OHlNREU0TDJOeVpXUmxiblJwWVd4ekwzWXhJbDBzSW5SNWNHVWlPbHNpVm1WeWFXWnBZV0pzWlVOeVpXUmxiblJwWVd3aUxDSldaWEpwWm1saFlteGxRWFIwWlhOMFlYUnBiMjRpTENKbGRTNWxkWEp2Y0dFdVpXTXVaWFZrYVM1d2FXUWlYU3dpYVdRaU9pSjJZenAxYzJWc1pqcGhaMlZ1ZENNNE1UQXdNamd4Tnprd01EQXhNek16TmpVd0lpd2lhWE56ZFdWa0lqb2lNakF5TkMwd09TMHdObFF3TnpveU16b3pNMW9pTENKMllXeHBaRVp5YjIwaU9pSXlNREkwTFRBNUxUQTJWREEzT2pJek9qTXpXaUlzSW1OeVpXUmxiblJwWVd4VFkyaGxiV0VpT25zaWFXUWlPaUpvZEhSd2N6b3ZMMkZ3YVMxamIyNW1iM0p0WVc1alpTNWxZbk5wTG1WMUwzUnlkWE4wWldRdGMyTm9aVzFoY3kxeVpXZHBjM1J5ZVM5Mk1pOXpZMmhsYldGekwzb3pUV2RWUmxWcllqY3lNblZ4TkhnelpIWTFlVUZLYlc1T2JYcEVSbVZMTlZWRE9IZzRNMUZ2WlV4S1RTSXNJblI1Y0dVaU9pSkdkV3hzU25OdmJsTmphR1Z0WVZaaGJHbGtZWFJ2Y2pJd01qRWlmU3dpZEdWeWJYTlBabFZ6WlNJNmV5SnBaQ0k2SW1oMGRIQnpPaTh2WVhCcExYQnBiRzkwTG1WaWMya3VaWFV2ZEhKMWMzUmxaQzFwYzNOMVpYSnpMWEpsWjJsemRISjVMM1kxTDJsemMzVmxjbk12Wkdsa09tVmljMms2ZW5SU2IxbDVTazVrUjNJNGRHMUJkRlpvT1dObk9XNHZZWFIwY21saWRYUmxjeTlqTjJNek9UZzBZMkpsWXpVek1HUTRaR0U1TTJJeVl6VTJNR1poTmpnNVpqWXpOREJrT1dZMU5tTTRNemsyWkdVNVltTTVZMlU0WkdFd1lUWXdPVEpqSWl3aWRIbHdaU0k2SWtsemMzVmhibU5sUTJWeWRHbG1hV05oZEdVaWZTd2lhWE56ZFdWeUlqb2laR2xrT21WaWMyazZlblJTYjFsNVNrNWtSM0k0ZEcxQmRGWm9PV05uT1c0aUxDSnBjM04xWVc1alpVUmhkR1VpT2lJeU1ESTBMVEE1TFRBMlZEQTNPakl6T2pNeldpSXNJbVY0Y0dseVlYUnBiMjVFWVhSbElqb2lNakF5Tmkwd09TMHhPVlF4Tmpvd01Eb3dNRm9pTENKamNtVmtaVzUwYVdGc1UzVmlhbVZqZENJNmV5SnBaQ0k2SW1ScFpEcHJaWGs2ZWpKa2JYcEVPREZqWjFCNE9GWnJhVGRLWW5WMVRXMUdXWEpYVUdkWmIzbDBlV3RWV2pObGVYRm9kREZxT1V0aWJ6WnZPR2RMY0hwT1JrSm9OamczZUdGMmNUaEhRVGh2VTBKVWVGSm1ZbTA0TWxWQ1VIcDJTRzFwVEU0elptRjZZVFo1WmtoU1ZUZEtkM0JhWTFSdlF6azNjM2h1Y2tGMmVHMXpWRVZwUjFoMGNUbFJOa2R5V1dsT1ZWSmxRWGQyTlZKNGJXVmhhMjlIV1RRMVV6RlhSVGxEUzFGeVkwMURkVlZ1ZUU1eVdVUk1lQ0lzSW1SdlkzVnRaVzUwWDI1MWJXSmxjaUk2SWpFeU16UTFOamM0T1NJc0ltZHBkbVZ1WDI1aGJXVWlPaUpLYjJodUlpd2labUZ0YVd4NVgyNWhiV1VpT2lKRWIyVWlMQ0ppYVhKMGFGOWtZWFJsSWpvaU1UazVNQzB3TVMwd01TSXNJbUZuWlY5dmRtVnlYekU0SWpvaWRISjFaU0o5TENKamNtVmtaVzUwYVdGc1UzUmhkSFZ6SWpwN0luUjVjR1VpT2lKVGRHRjBkWE5NYVhOME1qQXlNVVZ1ZEhKNUlpd2lhV1FpT2lKb2RIUndjem92TDNSaFpIQnZiR1V0YVc1MFpYSnVZV3d0YldGdGJXRnNMbTVuY205ckxXWnlaV1V1WVhCd0wzTjBZWFIxY3k5Mk1TTXdJaXdpYzNSaGRIVnpUR2x6ZEVsdVpHVjRJam9pTUNJc0luTjBZWFIxYzB4cGMzUkRjbVZrWlc1MGFXRnNJam9pYUhSMGNITTZMeTkwWVdSd2IyeGxMV2x1ZEdWeWJtRnNMVzFoYlcxaGJDNXVaM0p2YXkxbWNtVmxMbUZ3Y0M5emRHRjBkWE12ZGpFaUxDSnpkR0YwZFhOUWRYSndiM05sSWpvaWNtVjJiMk5oZEdsdmJpSjlmU3dpYW5ScElqb2lkbU02ZFhObGJHWTZZV2RsYm5Rak9ERXdNREk0TVRjNU1EQXdNVE16TXpZMU1DSjkueDJqVkEyMGZjb3ZQb0toN2NYM3BUTjJBZ3M3VVpFakh0Z0Q4bTdkeDlvN3JoV2ZSMGdzcmF4OEt5N080czFZM2tZSC1OTUIyR0ZOb29KSThKTENfMlEiXX0sImp0aSI6InVybjp1dWlkOjI5N2IyOWE5LTQ4NzItNDljZC1hNjczLWU3ZTJiMjM5YTlhNyIsImlhdCI6MTcyNTYwNzQxMywiaXNzIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvNm84Z0twek5GQmg2ODd4YXZxOEdBOG9TQlR4UmZibTgyVUJQenZIbWlMTjNmYXphNnlmSFJVN0p3cFpjVG9DOTdzeG5yQXZ4bXNURWlHWHRxOVE2R3JZaU5VUmVBd3Y1UnhtZWFrb0dZNDVTMVdFOUNLUXJjTUN1VW54TnJZREx4Iiwic3ViIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvNm84Z0twek5GQmg2ODd4YXZxOEdBOG9TQlR4UmZibTgyVUJQenZIbWlMTjNmYXphNnlmSFJVN0p3cFpjVG9DOTdzeG5yQXZ4bXNURWlHWHRxOVE2R3JZaU5VUmVBd3Y1UnhtZWFrb0dZNDVTMVdFOUNLUXJjTUN1VW54TnJZREx4IiwiYXVkIjoiaHR0cHM6Ly90YWRwb2xlLWludGVybmFsLW1hbW1hbC5uZ3Jvay1mcmVlLmFwcC9hdXRoIiwibmJmIjoxNzI1NjA3NDEzLCJleHAiOjE3ODk4MzM2MDB9.OoVF0vsyG-zmhSj0QZLQSOKZH7PQ_-MZoeoPHWs8rkgJFSQrTJsm2gwkc7PgxmlXOJWWxzifyU6Ag4d2iN5Byg
              """.trim(),
            presentationSubmission = """
           "{\"id\":\"9a402f43-53a5-4599-a2e3-dc9b2d5deff0\",\"definition_id\":\"holder-wallet-qualification-presentation\",\"descriptor_map\":[{\"id\":\"5020092759174734876\",\"path\":\"${'$'}\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"5020092759174734876\",\"format\":\"jwt_vc\",\"path\":\"${'$'}.vp.verifiableCredential[0]\"}}]}"
           """.trim(),
            state = "8d8b6a3d-4bc0-4234-9a9a-ed1928815502"
        )
        val expectedResult =
            """
      openid://redirect?code=d527c191-6e1d-4c3d-9843-9eaf2005fba9&state=8d8b6a3d-4bc0-4234-9a9a-ed1928815502
            """.trimIndent()
    }

    object GetToken {
        val preAuthorizedCodeWithNonce = """
            eyJraWQiOiJkaWQ6ZWJzaTp6dFJvWXlKTmRHcjh0bUF0Vmg5Y2c5biIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiIiLCJhdXRob3JpemF0aW9uX2RldGFpbHMiOnsiZm9ybWF0Ijoiand0X3ZjIiwidHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJldS5ldXJvcGEuZWMuZXVkaS5waWQiXSwidHJ1c3RGcmFtZXdvcmsiOnsibmFtZSI6InVTZWxmIEFnZW50IElzc3VlciIsInR5cGUiOiJldS5ldXJvcGEuZWMuZXVkaS5waWQiLCJ1cmkiOiJldS5ldXJvcGEuZWMuZXVkaS5waWQgdGVzdGluZyJ9LCJzY29wZSI6bnVsbCwiY3J5cHRvZ3JhcGhpY0JpbmRpbmdNZXRob2RzU3VwcG9ydGVkIjpudWxsLCJjcmVkZW50aWFsU2lnbmluZ0FsZ1ZhbHVlc1N1cHBvcnRlZCI6bnVsbCwiZGlzcGxheSI6W3sibmFtZSI6ImV1LmV1cm9wYS5lYy5ldWRpLnBpZCIsImxvY2FsZSI6ImVuLUdCIiwibG9nbyI6bnVsbCwiZGVzY3JpcHRpb24iOiJldS5ldXJvcGEuZWMuZXVkaS5waWQgZm9yIERDNEVVIFByb2plY3QiLCJiYWNrZ3JvdW5kQ29sb3IiOm51bGwsImJhY2tncm91bmRJbWFnZSI6bnVsbCwidGV4dENvbG9yIjpudWxsfV0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjp7Im1hbmRhdG9yeSI6ZmFsc2UsInZhbHVlVHlwZSI6bnVsbCwiZGlzcGxheSI6W3sibmFtZSI6ImlkIiwibG9jYWxlIjoiZW4tR0IiLCJsb2dvIjpudWxsLCJkZXNjcmlwdGlvbiI6bnVsbCwiYmFja2dyb3VuZENvbG9yIjpudWxsLCJiYWNrZ3JvdW5kSW1hZ2UiOm51bGwsInRleHRDb2xvciI6bnVsbH1dfSwiZG9jdW1lbnRfbnVtYmVyIjp7Im1hbmRhdG9yeSI6ZmFsc2UsInZhbHVlVHlwZSI6bnVsbCwiZGlzcGxheSI6W3sibmFtZSI6ImRvY3VtZW50X251bWJlciIsImxvY2FsZSI6ImVuLUdCIiwibG9nbyI6bnVsbCwiZGVzY3JpcHRpb24iOm51bGwsImJhY2tncm91bmRDb2xvciI6bnVsbCwiYmFja2dyb3VuZEltYWdlIjpudWxsLCJ0ZXh0Q29sb3IiOm51bGx9XX0sImdpdmVuX25hbWUiOnsibWFuZGF0b3J5IjpmYWxzZSwidmFsdWVUeXBlIjpudWxsLCJkaXNwbGF5IjpbeyJuYW1lIjoiZ2l2ZW5fbmFtZSIsImxvY2FsZSI6ImVuLUdCIiwibG9nbyI6bnVsbCwiZGVzY3JpcHRpb24iOm51bGwsImJhY2tncm91bmRDb2xvciI6bnVsbCwiYmFja2dyb3VuZEltYWdlIjpudWxsLCJ0ZXh0Q29sb3IiOm51bGx9XX0sImZhbWlseV9uYW1lIjp7Im1hbmRhdG9yeSI6ZmFsc2UsInZhbHVlVHlwZSI6bnVsbCwiZGlzcGxheSI6W3sibmFtZSI6ImZhbWlseV9uYW1lIiwibG9jYWxlIjoiZW4tR0IiLCJsb2dvIjpudWxsLCJkZXNjcmlwdGlvbiI6bnVsbCwiYmFja2dyb3VuZENvbG9yIjpudWxsLCJiYWNrZ3JvdW5kSW1hZ2UiOm51bGwsInRleHRDb2xvciI6bnVsbH1dfSwiYmlydGhfZGF0ZSI6eyJtYW5kYXRvcnkiOmZhbHNlLCJ2YWx1ZVR5cGUiOm51bGwsImRpc3BsYXkiOlt7Im5hbWUiOiJiaXJ0aF9kYXRlIiwibG9jYWxlIjoiZW4tR0IiLCJsb2dvIjpudWxsLCJkZXNjcmlwdGlvbiI6bnVsbCwiYmFja2dyb3VuZENvbG9yIjpudWxsLCJiYWNrZ3JvdW5kSW1hZ2UiOm51bGwsInRleHRDb2xvciI6bnVsbH1dfSwiYWdlX292ZXJfMTgiOnsibWFuZGF0b3J5IjpmYWxzZSwidmFsdWVUeXBlIjpudWxsLCJkaXNwbGF5IjpbeyJuYW1lIjoiYWdlX292ZXJfMTgiLCJsb2NhbGUiOiJlbi1HQiIsImxvZ28iOm51bGwsImRlc2NyaXB0aW9uIjpudWxsLCJiYWNrZ3JvdW5kQ29sb3IiOm51bGwsImJhY2tncm91bmRJbWFnZSI6bnVsbCwidGV4dENvbG9yIjpudWxsfV19fSwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbCwiY2xhaW1zIjpudWxsLCJvcmRlciI6bnVsbH0sImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIiwiZXhwIjoxNzM4NTgwNjEyLCJpYXQiOjE3Mzg1NzcwMTIsIm5vbmNlIjoiNzQ1NDI4MTQ1Njg4ODY0NjUzNSIsImNsaWVudF9pZCI6IiJ9.qXeHCeAnGRTaCBL0hj2D4EITZVeuhNBgFrr5pz2ZJHhy4O01tQ8x7lyBrkRon3gQb0rFgjzBgn5WPn3_eu5OmQ
        """.trimIndent()
        val preAuthorizedCode = """
            eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdXRob3JpemF0aW9uX2RldGFpbHMiOnsidHlwZSI6Im9wZW5pZF9jcmVkZW50aWFsIiwiZm9ybWF0Ijoiand0X3ZjIiwibG9jYXRpb25zIjpudWxsLCJ0eXBlcyI6WyJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlZlcmlmaWFibGVBdHRlc3RhdGlvbiIsIkNUV2FsbGV0U2FtZVByZUF1dGhvcmlzZWRJblRpbWUiXSwiY3JlZGVudGlhbENvbmZpZ3VyYXRpb25JZCI6bnVsbCwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbH0sImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIiwiZXhwIjoxNzE3NDkzNDE5LCJpYXQiOjE3MTc0ODk4MTksImNsaWVudF9pZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCJ9.f4MpjBDpraopQ8krNwxJYW5TmbjhdEBosh1xuNZp1qpJOQfyPwAaQpdcQjqzlHhZB9cpldWg1cnHDsFWNswawQ
        """.trimIndent()

        val request = TokenRequest(
            grantType = GRAN_TYPE.AUTH_CODE,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4
                """.trim(),
            code = "d527c191-6e1d-4c3d-9843-9eaf2005fba9",
            codeVerifier = "password"
        )
        val expectedResult = TokenResponse(
            accessToken = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aCIsImNsYWltcyI6eyJhdXRob3JpemF0aW9uRGV0YWlscyI6bnVsbCwiY05vbmNlIjoiZDUyN2MxOTEtNmUxZC00YzNkLTk4NDMtOWVhZjIwMDVmYmE5IiwiY05vbmNlRXhwaXJlc0luIjoxNzExNTMyMTEyODUxLCJjbGllbnRJZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCJ9LCJleHAiOjE3MTE1MzIxMTIsImlhdCI6MTcxMTUzMTgxMiwibm9uY2UiOiJkNTI3YzE5MS02ZTFkLTRjM2QtOTg0My05ZWFmMjAwNWZiYTkifQ.PfRIvQZ60BszV3HcynM-7BJHOKm0v5K9PGcoOrTw8dIZXqVdjZmuZVaQVEJ_DrQD_gLbfjOyJbxh6xfe6efWVw
                """.trim(),
            idToken = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJleHAiOjE3MTE1MzIxMTIsImlhdCI6MTcxMTUzMTgxMiwibm9uY2UiOiJkNTI3YzE5MS02ZTFkLTRjM2QtOTg0My05ZWFmMjAwNWZiYTkifQ.J4pf5vIUxJY34m4v7iX6HeKwzIZi_6eJTiUtseeOB5aHcm-P1B9-7Hb0vjrHuCLt2Eb3JUrHirGtCf0AD29ynQ
                """.trim(),
            tokenType = TOKEN_TYPE.BEARER,
            expiresIn = 1711532112851,
            cNonce = "d527c191-6e1d-4c3d-9843-9eaf2005fba9",
            cNonceExpiresIn = 1711532112851
        )
    }
}

object MOCKS_AUTH {

    val credentialSubject: Map<String, FieldDescription> = mapOf(
        "id1" to FieldDescription(
            display = arrayListOf(
                Display(
                    name = "id1",
                    locale = "en-GB"
                )
            )
        )
    )

    object CredentialOffer {
        val request = CredentialOfferRequest(
            redirect = false,
            credentialType = CREDENTIAL_TYPES.EBSI_SAME_IN_TIME,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4
                """.trim()
        )
        val preAuthRequest = CredentialOfferRequest(
            redirect = false,
            credentialType = CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH
        )

        val requestEPassport = CredentialOfferRequest(
            redirect = false,
            credentialType = CREDENTIAL_TYPES.EBSI_SAME_IN_TIME,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4
                """.trim()
        )

        val authOffer = CredentialOfferResponse(
            credentialIssuer = "http://localhost/issuer",
            credentials = arrayListOf(
                CredentialSupported(
                    format = FORMAT.JWT_VC,
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.EBSI_SAME_IN_TIME
                    ),
                    trustFramework = TrustFramework(
                        name = "uSelf Agent Issuer",
                        type = CREDENTIAL_TYPES.EBSI_SAME_IN_TIME,
                        uri = "CTWalletSameAuthorisedInTime testing"
                    ),
                    display = arrayListOf(
                        Display(
                            name = "CTWalletSameAuthorisedInTime testing",
                            locale = "en-GB",
                            description = "This is an example of a Self-Issued Verifiable Credential"
                        )
                    ),
                    credentialSubject = mapOf(
                        "id1" to FieldDescription(
                            display = arrayListOf(
                                Display(
                                    name = "id1",
                                    locale = "en-GB"
                                )
                            )
                        )
                    )
                )
            ),
            grants = CredentialOfferGrants(
                authorizationCode = CredentialOfferGrantsAuthorizationCode(
                    issuerState = """
eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsImNyZWRlbnRpYWxfdHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJDVFdhbGxldFNhbWVBdXRob3Jpc2VkSW5UaW1lIl0sImV4cCI6MTcxODAyMzk4OSwiaWF0IjoxNzE4MDIwMzg5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQifQ.tO_ogh_joqBNnB8VacqhBA8khMQZGYAEBTGCHviHCq_4DXjswZMD9I4A87W7nbG1YQcuahn_3UlgQuPo66EJpg
                            """.trim()
                )
            )
        )

        val requestKeycloak = CredentialOfferRequest(
            redirect = false,
            credentialType = CREDENTIAL_TYPES.EBSI_SAME_IN_TIME,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4
                """.trim()
        )

        val passportAuthOffer = CredentialOfferResponse(
            credentialIssuer = "lhttp://ocalhost/issuer",
            credentials = arrayListOf(
                CredentialSupported(
                    format = FORMAT.JWT_VC,
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
                    ),
                    trustFramework = TrustFramework(
                        name = "uSelf Agent Issuer",
                        type = CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO,
                        uri = "ePassport testing"
                    ),
                    display = arrayListOf(
                        Display(
                            name = "ePassport testing",
                            locale = "en-GB",
                            description = "This is an example of a Self-Issued Verifiable Credential"
                        )
                    ),
                    credentialSubject = mapOf(
                        "id1" to FieldDescription(
                            display = arrayListOf(
                                Display(
                                    name = "id1",
                                    locale = "en-GB"
                                )
                            )
                        )
                    )
                )
            ),
            grants = CredentialOfferGrants(
                authorizationCode = CredentialOfferGrantsAuthorizationCode(
                    issuerState = """
eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsImNyZWRlbnRpYWxfdHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJDVFdhbGxldFNhbWVBdXRob3Jpc2VkSW5UaW1lIl0sImV4cCI6MTcxODAyMzk4OSwiaWF0IjoxNzE4MDIwMzg5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQifQ.tO_ogh_joqBNnB8VacqhBA8khMQZGYAEBTGCHviHCq_4DXjswZMD9I4A87W7nbG1YQcuahn_3UlgQuPo66EJpg
                            """.trim()
                )
            )
        )

        val employeeAuthOffer = CredentialOfferResponse(
            credentialIssuer = "http://localhost/issuer",
            credentials = arrayListOf(
                CredentialSupported(
                    format = FORMAT.JWT_VC,
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.TANGO_EMPLOYEE_CREDENTIAL
                    ),
                    trustFramework = TrustFramework(
                        name = "uSelf Agent Issuer",
                        type = "Acreditation",
                        uri = "ePassport testing"
                    ),
                    display = arrayListOf(
                        Display(
                            name = "ePassport testing",
                            locale = "en-GB",
                            description = "This is an example of a Self-Issued Verifiable Credential"
                        )
                    ),
                    credentialSubject = mapOf(
                        "id1" to FieldDescription(
                            display = arrayListOf(
                                Display(
                                    name = "id1",
                                    locale = "en-GB"
                                )
                            )
                        )
                    )
                )
            ),
            grants = CredentialOfferGrants(
                authorizationCode = CredentialOfferGrantsAuthorizationCode(
                    issuerState = "tracker=123456789"
                )
            )
        )

        val customerAuthOffer = CredentialOfferResponse(
            credentialIssuer = "lhttp://localhost/issuer",
            credentials = arrayListOf(
                CredentialSupported(
                    format = FORMAT.JWT_VC,
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.TANGO_CUSTOMER_CREDENTIAL
                    ),
                    trustFramework = TrustFramework(
                        name = "uSelf Agent Issuer",
                        type = "Acreditation",
                        uri = "ePassport testing"
                    ),
                    display = arrayListOf(
                        Display(
                            name = "ePassport testing",
                            locale = "en-GB",
                            description = "This is an example of a Self-Issued Verifiable Credential"
                        )
                    ),
                    credentialSubject = mapOf(
                        "id1" to FieldDescription(
                            display = arrayListOf(
                                Display(
                                    name = "id1",
                                    locale = "en-GB"
                                )
                            )
                        )
                    )
                )
            ),
            grants = CredentialOfferGrants(
                authorizationCode = CredentialOfferGrantsAuthorizationCode(
                    issuerState = "tracker=123456789"
                )
            )
        )

        val preAuthOffer = CredentialOfferResponse(
            credentialIssuer = "http://localhost/issuer",
            credentials = arrayListOf(
                CredentialSupported(
                    format = FORMAT.JWT_VC,
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH
                    ),
                    trustFramework = TrustFramework(
                        name = "uSelf Agent Issuer",
                        type = CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH,
                        uri = "CTWalletSamePreAuthorisedInTime testing"
                    ),
                    display = arrayListOf(
                        Display(
                            name = "CTWalletSamePreAuthorisedInTime testing",
                            locale = "en-GB",
                            description = "This is an example of a Self-Issued Verifiable Credential"
                        )
                    ),
                    credentialSubject = mapOf(
                        "id1" to FieldDescription(
                            display = arrayListOf(
                                Display(
                                    name = "id1",
                                    locale = "en-GB"
                                )
                            )
                        )
                    )
                )
            ),
            grants = CredentialOfferGrants(
                preAuthorizedCode = CredentialOfferGrantsPreAuthorizedCode(
                    preAuthorizedCode = """
                            eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdXRob3JpemF0aW9uX2RldGFpbHMiOnsidHlwZSI6Im9wZW5pZF9jcmVkZW50aWFsIiwiZm9ybWF0Ijoiand0X3ZjIiwibG9jYXRpb25zIjpudWxsLCJ0eXBlcyI6WyJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlZlcmlmaWFibGVBdHRlc3RhdGlvbiIsIkNUV2FsbGV0U2FtZVByZUF1dGhvcmlzZWRJblRpbWUiXSwiY3JlZGVudGlhbENvbmZpZ3VyYXRpb25JZCI6bnVsbCwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbH0sImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIiwiZXhwIjoxNzE3NDMxMjQ0LCJpYXQiOjE3MTc0Mjc2NDQsImNsaWVudF9pZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCJ9.1njOYmCqTfEgS73T2-rCLkuc-BLkSuIk21I_TzOfY-L6rRNkpcEBxVt0hHWcBWcGXXA1scdpMOGpySqW0Y5U8Q
                            """.trim(),
                    userPinRequired = true
                )
            )
        )
        val expectedResult = """
            openid-credential-offer://?credential_offer_uri=http%3A%2F%2Flocalhost%2Fissuer%2Foffers%2F
        """.trimIndent()
    }

    object Authorisation {
        val request = AuthorisationRequest(
            scope = SCOPE.OPEN_ID,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC
                """.trim(),
            responseType = RESPONSE_TYPE.CODE,
            redirectUri = END_POINT.OPEN_ID,
            state = TestsHelper.STATE,
            authorizationDetails = arrayListOf(
                AuthorizationDetails(
                    type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
                    format = FORMAT.JWT_VC,
                    locations = arrayListOf("http://localhost/issuer"),
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH
                    )
                )
            ),
            clientMetadata = ClientMetadata(
                authorizationEndpoint = "openid:",
                jwks_uri = "https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/jwks",
                redirectUris = arrayListOf(END_POINT.OPEN_ID)
            ),
            codeChallenge = "wPVb-KFGtYUnwUiUMUjAEplYjJ9htnSZS0haaWBCJtc",
            redirect = null
        )
        val expectedResult =
            """
        openid://?client_id=&redirect_uri=http%3A%2F%2Flocalhost%2Fauth%2Fdirect_post&response_type=vp_token&response_mode=direct_post&scope=openid&state=8e8a74d4-2ecc-4921-9099-b6a053d05549&nonce=8661435939417066112&request_uri=http%3A%2F%2Flocalhost%2Fauth%2Frequest_uri%2F9153157014685083226&request=eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzY29wZSI6Im9wZW5pZCIsImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInJlc3BvbnNlX3R5cGUiOiJpZF90b2tlbiIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aC9kaXJlY3RfcG9zdCIsInN0YXRlIjoiOGU4YTc0ZDQtMmVjYy00OTIxLTkwOTktYjZhMDUzZDA1NTQ5IiwiZXhwIjoxNzExNDU0NjIzLCJpYXQiOjE3MTE0NTQzMjMsIm5vbmNlIjoiODY2MTQzNTkzOTQxNzA2NjExMiIsImNsaWVudF9pZCI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aCIsInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.XebU_7pk5XG6XKuIoXD8rp2U24Rxuxp2XA4uuajpFj4S6hOzrbSFidLS4fHqB5N4oquzk4QZ5MB28w5QOzYVEA
            """.trimIndent()

        val requestEpassport = AuthorisationRequest(
            scope = SCOPE.OPEN_ID,
            clientId = """
                did:jwk:eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2Iiwia2lkIjoiUVRIb09rbVlQa2p0MUU1WjcxenpRVUdhX1dadEhFQ0FJak9UM214SXk3cyIsIngiOiIxMjBfc1BGOVFFalpoVF9GQV9NcFlaR0ZVbTl1STJ0Yks0MXVhWm44Q284IiwieSI6Im8tYmpzZF9ZTlRaSEhpVVZGdW44UjB5OFNhSGRGY1FPV3dTMkFxTDg0ZkEifQ
                """.trim(),
            responseType = RESPONSE_TYPE.CODE,
            redirectUri = END_POINT.OPEN_ID,
            state = TestsHelper.EPASSPORT_STATE,
            authorizationDetails = arrayListOf(
                AuthorizationDetails(
                    type = AUTH_DETAILS_TYPE.OPENID_CREDENTIAL,
                    format = FORMAT.JWT_VC,
                    locations = arrayListOf("http://localhost/issuer"),
                    types = arrayListOf(
                        CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                        CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                        CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
                    )
                )
            ),
            clientMetadata = ClientMetadata(
                authorizationEndpoint = "ePassport-reader:",
                jwks_uri = "https://api-conformance.ebsi.eu/conformance/v3/issuer-mock/jwks",
                redirectUris = arrayListOf(END_POINT.OPEN_ID),
                responseTypesSupported = arrayListOf(
                    RESPONSE_TYPE.VP_TOKEN,
                    RESPONSE_TYPE.ID_TOKEN
                )
            ),
            issuerState = """
                eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvYXV0aCIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticDZxenRqc2JpMmFIZ1A2YU5CNFo0V2pjM0o3VTVMSlVINmlxVmo1YjY1enNnYmhoa01ZczdaenRYNHhxVjhBTmRrUDVrcmlFTlJTR0NkTEVKazUxN3hEbW9HbkRlb0ZaQUs5enh5ckVOSE1iV3g1MUZDNlF1S0I3bm5VUlZSa1VzZSIsImlzcyI6Imh0dHBzOi8vdXNlbGYtYWdlbnQuazhzLWNsdXN0ZXIudGFuZ28ucmlkLWludHJhc29mdC5ldS9pc3N1ZXIiLCJjcmVkZW50aWFsX3R5cGVzIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiZVBhc3Nwb3J0Il0sImV4cCI6MTczMzEzNTA5OSwiaWF0IjoxNzMzMTMxNDk5LCJjbGllbnRfaWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnA2cXp0anNiaTJhSGdQNmFOQjRaNFdqYzNKN1U1TEpVSDZpcVZqNWI2NXpzZ2JoaGtNWXM3Wnp0WDR4cVY4QU5ka1A1a3JpRU5SU0dDZExFSms1MTd4RG1vR25EZW9GWkFLOXp4eXJFTkhNYld4NTFGQzZRdUtCN25uVVJWUmtVc2UifQ.JnVhCsge-E3YCzPNWpJGiUUgoTr9Q_SfMTiCehAwstCDXKmRgAIk3WV3wwBQSZfVLJbb3WZmo8i46SjDIy9xIQ
            """.trimIndent(),
            codeChallenge = "uNXYLptEK3xSsamQORrJQEty997NjcjQQ3OUduYEays",
            redirect = null
        )
        val expectedResultEpassport =
            """
 ePassport-reader://?client_id=did%3Ajwk%3AeyJrdHkiOiJFQyIsImNydiI6IlAtMjU2Iiwia2lkIjoiUVRIb09rbVlQa2p0MUU1WjcxenpRVUdhX1dadEhFQ0FJak9UM214SXk3cyIsIngiOiIxMjBfc1BGOVFFalpoVF9GQV9NcFlaR0ZVbTl1STJ0Yks0MXVhWm44Q284IiwieSI6Im8tYmpzZF9ZTlRaSEhpVVZGdW44UjB5OFNhSGRGY1FPV3dTMkFxTDg0ZkEifQ&redirect_uri=http%3A%2F%2Flocalhost%2Fauth%2Fdirect_post%2Fepassport&response_type=id_token&response_mode=direct_post&scope=openid&state=95d86046-ef6d-40b5-8da0-1b27f2f9de4a&nonce=5583195896156095755&request_uri=https%3A%2F%2Fuself-agent.k8s-cluster.tango.rid-intrasoft.eu%2Fauth%2Frequest_uri%2F5580790723616518882&request=eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJpc3MiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInJlc3BvbnNlX3R5cGUiOiJpZF90b2tlbiIsIm5vbmNlIjoiNTU4MzE5NTg5NjE1NjA5NTc1NSIsImNsaWVudF9pZCI6ImRpZDpqd2s6ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SWxBdE1qVTJJaXdpYTJsa0lqb2lVVlJJYjA5cmJWbFFhMnAwTVVVMVdqY3hlbnBSVlVkaFgxZGFkRWhGUTBGSmFrOVVNMjE0U1hrM2N5SXNJbmdpT2lJeE1qQmZjMUJHT1ZGRmFscG9WRjlHUVY5TmNGbGFSMFpWYlRsMVNUSjBZa3MwTVhWaFdtNDRRMjg0SWl3aWVTSTZJbTh0WW1welpGOVpUbFJhU0VocFZWWkdkVzQ0VWpCNU9GTmhTR1JHWTFGUFYzZFRNa0Z4VERnMFprRWlmUSIsInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCIsImF1ZCI6ImRpZDpqd2s6ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SWxBdE1qVTJJaXdpYTJsa0lqb2lVVlJJYjA5cmJWbFFhMnAwTVVVMVdqY3hlbnBSVlVkaFgxZGFkRWhGUTBGSmFrOVVNMjE0U1hrM2N5SXNJbmdpT2lJeE1qQmZjMUJHT1ZGRmFscG9WRjlHUVY5TmNGbGFSMFpWYlRsMVNUSjBZa3MwTVhWaFdtNDRRMjg0SWl3aWVTSTZJbTh0WW1welpGOVpUbFJhU0VocFZWWkdkVzQ0VWpCNU9GTmhTR1JHWTFGUFYzZFRNa0Z4VERnMFprRWlmUSIsInNjb3BlIjoib3BlbmlkIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiJob2xkZXItd2FsbGV0LXF1YWxpZmljYXRpb24tcHJlc2VudGF0aW9uIiwiZm9ybWF0Ijp7Imp3dF92cF9qc29uIjp7ImFsZyI6WyJFUzI1NiJdfSwiand0X3ZwIjp7ImFsZyI6WyJFUzI1NiJdfSwiand0X3ZjX2pzb24iOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdmMiOnsiYWxnIjpbIkVTMjU2Il19fSwiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiPGFueSBpZCwgcmFuZG9tIG9yIHN0YXRpYz4iLCJuYW1lIjoiaG9sZGVyLXdhbGxldC1xdWFsaWZpY2F0aW9uIiwicHVycG9zZSI6IlRoZSBob2xkZXIgd2FsbGV0IHF1YWxpZmljYXRpb24iLCJmb3JtYXQiOnsiand0X3ZwX2pzb24iOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdnAiOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdmNfanNvbiI6eyJhbGciOlsiRVMyNTYiXX0sImp3dF92YyI6eyJhbGciOlsiRVMyNTYiXX19LCJjb25zdHJhaW50cyI6eyJmaWVsZHMiOlt7InBhdGgiOlsiJC52Yy50eXBlIl0sImZpbHRlciI6eyJ0eXBlIjoiYXJyYXkiLCJjb250YWlucyI6eyJjb25zdCI6IlZlcmlmaWFibGVBdHRlc3RhdGlvbiJ9fX1dfX0seyJpZCI6IjxhbnkgaWQsIHJhbmRvbSBvciBzdGF0aWM-IiwibmFtZSI6ImhvbGRlci13YWxsZXQtcXVhbGlmaWNhdGlvbiIsInB1cnBvc2UiOiJUaGUgaG9sZGVyIHdhbGxldCBxdWFsaWZpY2F0aW9uIiwiZm9ybWF0Ijp7Imp3dF92cF9qc29uIjp7ImFsZyI6WyJFUzI1NiJdfSwiand0X3ZwIjp7ImFsZyI6WyJFUzI1NiJdfSwiand0X3ZjX2pzb24iOnsiYWxnIjpbIkVTMjU2Il19LCJqd3RfdmMiOnsiYWxnIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQudmMudHlwZSJdLCJmaWx0ZXIiOnsidHlwZSI6ImFycmF5IiwiY29udGFpbnMiOnsiY29uc3QiOiJWZXJpZmlhYmxlQXR0ZXN0YXRpb24ifX19XX19LHsiaWQiOiI8YW55IGlkLCByYW5kb20gb3Igc3RhdGljPiIsIm5hbWUiOiJob2xkZXItd2FsbGV0LXF1YWxpZmljYXRpb24iLCJwdXJwb3NlIjoiVGhlIGhvbGRlciB3YWxsZXQgcXVhbGlmaWNhdGlvbiIsImZvcm1hdCI6eyJqd3RfdnBfanNvbiI6eyJhbGciOlsiRVMyNTYiXX0sImp3dF92cCI6eyJhbGciOlsiRVMyNTYiXX0sImp3dF92Y19qc29uIjp7ImFsZyI6WyJFUzI1NiJdfSwiand0X3ZjIjp7ImFsZyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLnZjLnR5cGUiXSwiZmlsdGVyIjp7InR5cGUiOiJhcnJheSIsImNvbnRhaW5zIjp7ImNvbnN0IjoiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIn19fV19fV19LCJyZWRpcmVjdF91cmkiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvYXV0aC9kaXJlY3RfcG9zdC9lcGFzc3BvcnQiLCJzdGF0ZSI6Ijk1ZDg2MDQ2LWVmNmQtNDBiNS04ZGEwLTFiMjdmMmY5ZGU0YSIsImV4cCI6MTczMzEzMjEwNSwiaWF0IjoxNzMzMTMxNTA1fQ.U9Wm0IIV_Nn74tB10c01Kq-os5XdyzycQu-BYbFHO9B3LzPWavNEjfjNHqerOdQXBwZ3RQuJaq7ms1aBVmbhVg&presentation_definition=%7B%22id%22%3A%22holder-wallet-qualification-presentation%22%2C%22format%22%3A%7B%22jwt_vp_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vp%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%7D%2C%22input_descriptors%22%3A%5B%7B%22id%22%3A%22%3Cany+id%2C+random+or+static%3E%22%2C%22name%22%3A%22holder-wallet-qualification%22%2C%22purpose%22%3A%22The+holder+wallet+qualification%22%2C%22format%22%3A%7B%22jwt_vp_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vp%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%7D%2C%22constraints%22%3A%7B%22fields%22%3A%5B%7B%22path%22%3A%5B%22%24.vc.type%22%5D%2C%22filter%22%3A%7B%22type%22%3A%22array%22%2C%22contains%22%3A%7B%22const%22%3A%22VerifiableAttestation%22%7D%7D%7D%5D%7D%7D%2C%7B%22id%22%3A%22%3Cany+id%2C+random+or+static%3E%22%2C%22name%22%3A%22holder-wallet-qualification%22%2C%22purpose%22%3A%22The+holder+wallet+qualification%22%2C%22format%22%3A%7B%22jwt_vp_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vp%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%7D%2C%22constraints%22%3A%7B%22fields%22%3A%5B%7B%22path%22%3A%5B%22%24.vc.type%22%5D%2C%22filter%22%3A%7B%22type%22%3A%22array%22%2C%22contains%22%3A%7B%22const%22%3A%22VerifiableAttestation%22%7D%7D%7D%5D%7D%7D%2C%7B%22id%22%3A%22%3Cany+id%2C+random+or+static%3E%22%2C%22name%22%3A%22holder-wallet-qualification%22%2C%22purpose%22%3A%22The+holder+wallet+qualification%22%2C%22format%22%3A%7B%22jwt_vp_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vp%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc_json%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%2C%22jwt_vc%22%3A%7B%22alg%22%3A%5B%22ES256%22%5D%7D%7D%2C%22constraints%22%3A%7B%22fields%22%3A%5B%7B%22path%22%3A%5B%22%24.vc.type%22%5D%2C%22filter%22%3A%7B%22type%22%3A%22array%22%2C%22contains%22%3A%7B%22const%22%3A%22VerifiableAttestation%22%7D%7D%7D%5D%7D%7D%5D%7D
            """.trimIndent()

        val requestKeyCloak = AuthorisationRequest(
            scope = SCOPE.OPEN_ID,
            clientId = "keycloak-client",
            responseType = RESPONSE_TYPE.CODE,
            redirectUri = "https://keycloak.dev5.ari-bip.eu/realms/master/broker/uself/endpoint",
            state = TestsHelper.STATE,
            nonce = NONCE,
            redirect = null
        )

        val expectedResultKeyCloak =
            """
 https://keycloak.dev5.ari-bip.eu/realms/master/broker/uself/link?nonce=8661435939417066112&state=8e8a74d4-2ecc-4921-9099-b6a053d05549&hash=DtjXWv-cWo0BqTpBZRXQ6x5TFn7WSoDkXJeiDaj66Sc&client_id=keycloak-client&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fauth%2Frealms%2Fmaster%2Fprotocol%2Fopenid-connect%2Fauth
            """.trimIndent()
    }

    object DirectPost {
        val request = AuthDirectPost(
            idToken = """
                eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtib2o3ZzlQZlhKeGJiczRLWWVneXI3RUxuRlZucERNemJKSkRETlpqYXZYNmp2dERtQUxNYlhBR1c2N3BkVGdGZWEyRnJHR1NGczhFanhpOTZvRkxHSGNMNFA2YmpMRFBCSkV2UlJIU3JHNExzUG5lNTJmY3p0Mk1XakhMTEpCdmhBQyN6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIn0.eyJub25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJpc3MiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJpYXQiOjE3MTE0NTQzMjQsImV4cCI6MTcxMTQ1NDYyNH0.Ih2jrQZ36tBoUbPnda1z4sjCeFQH7mxb5JH-1CIqolUKjKQLFvzXusmP5cc0utk3xTgBOZpNSMjFVZfa6aBkkQ
                """.trim(),
            state = TestsHelper.STATE
        )
        val expectedResult =
            """
         openid://redirect?code=8661435939417066112&state=8e8a74d4-2ecc-4921-9099-b6a053d05549
            """.trimIndent()

        val requestKeycloak = AuthDirectPost(
            vpToken = """
eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnQ2ZEhNemdiUFVaYXNOdlJBaXZ6cm02OW53OWtzVWNhU2NmWHk1TVpjNWF4TGV4YkppVWN0Nk42bXh0OXN3OU5BeGdhRkQ5ZnJVQnVWcmZUTnBkSFJYZXlYVEJkS2h2SEpwY1ZWeTdLcERhV2ZhZVhIcWpYTHNmbkpHdkNuQkpzWDIjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtidDZkSE16Z2JQVVphc052UkFpdnpybTY5bnc5a3NVY2FTY2ZYeTVNWmM1YXhMZXhiSmlVY3Q2TjZteHQ5c3c5TkF4Z2FGRDlmclVCdVZyZlROcGRIUlhleVhUQmRLaHZISnBjVlZ5N0twRGFXZmFlWEhxalhMc2ZuSkd2Q25CSnNYMiIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnQ2ZEhNemdiUFVaYXNOdlJBaXZ6cm02OW53OWtzVWNhU2NmWHk1TVpjNWF4TGV4YkppVWN0Nk42bXh0OXN3OU5BeGdhRkQ5ZnJVQnVWcmZUTnBkSFJYZXlYVEJkS2h2SEpwY1ZWeTdLcERhV2ZhZVhIcWpYTHNmbkpHdkNuQkpzWDIiLCJhdWQiOiJkaWQ6ZWJzaTp6Z3pHTnQ5MkpyOU05ZnNZY291Z1FkQyIsIm5iZiI6MTcyNzI3OTQzMSwiaXNzIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2J0NmRITXpnYlBVWmFzTnZSQWl2enJtNjludzlrc1VjYVNjZlh5NU1aYzVheExleGJKaVVjdDZONm14dDlzdzlOQXhnYUZEOWZyVUJ1VnJmVE5wZEhSWGV5WFRCZEtodkhKcGNWVnk3S3BEYVdmYWVYSHFqWExzZm5KR3ZDbkJKc1gyIiwidnAiOnsiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiXSwidHlwZSI6WyJWZXJpZmlhYmxlUHJlc2VudGF0aW9uIl0sImlkIjoiODdhYWJmMjAtNTA4Ny00MDJlLWE5ZmUtOWM4ZTAzZTdmODM5IiwiaG9sZGVyIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2J0NmRITXpnYlBVWmFzTnZSQWl2enJtNjludzlrc1VjYVNjZlh5NU1aYzVheExleGJKaVVjdDZONm14dDlzdzlOQXhnYUZEOWZyVUJ1VnJmVE5wZEhSWGV5WFRCZEtodkhKcGNWVnk3S3BEYVdmYWVYSHFqWExzZm5KR3ZDbkJKc1gyIiwidmVyaWZpYWJsZUNyZWRlbnRpYWwiOlsiZXlKcmFXUWlPaUprYVdRNlpXSnphVHA2YlVoWmJtcGtiVnAyY1hRNVlWWk5NVmRrUTNSQlVpTmxZalpqTldJMFpUUXhPVGMwT1RFd1lXRTNNR00xTURVMU9UUXpZVFUxWmlJc0luUjVjQ0k2SWtwWFZDSXNJbUZzWnlJNklrVlRNalUySW4wLmV5SnpkV0lpT2lKa2FXUTZhMlY1T25veVpHMTZSRGd4WTJkUWVEaFdhMmszU21KMWRVMXRSbGx5VjFCbldXOTVkSGxyVlZvelpYbHhhSFF4YWpsTFluUTJaRWhOZW1kaVVGVmFZWE5PZGxKQmFYWjZjbTAyT1c1M09XdHpWV05oVTJObVdIazFUVnBqTldGNFRHVjRZa3BwVldOME5rNDJiWGgwT1hOM09VNUJlR2RoUmtRNVpuSlZRblZXY21aVVRuQmtTRkpZWlhsWVZFSmtTMmgyU0Vwd1kxWldlVGRMY0VSaFYyWmhaVmhJY1dwWVRITm1ia3BIZGtOdVFrcHpXRElpTENKdVltWWlPakUzTWpZMU1EWTBOaklzSW1semN5STZJbVJwWkRwbFluTnBPbnB0U0ZsdWFtUnRXblp4ZERsaFZrMHhWMlJEZEVGU0lpd2laWGh3SWpveE56VTNPVFUyTURZeUxDSnBZWFFpT2pFM01qWTFNRFkwTmpJc0luWmpJanA3SWtCamIyNTBaWGgwSWpwYkltaDBkSEJ6T2k4dmQzZDNMbmN6TG05eVp5OHlNREU0TDJOeVpXUmxiblJwWVd4ekwzWXhJbDBzSW5SNWNHVWlPbHNpVm1WeWFXWnBZV0pzWlVOeVpXUmxiblJwWVd3aUxDSldaWEpwWm1saFlteGxRWFIwWlhOMFlYUnBiMjRpTENKRFZGZGhiR3hsZEZOaGJXVkJkWFJvYjNKcGMyVmtTVzVVYVcxbElsMHNJbWxrSWpvaWRtTTZkWE5sYkdZNllXZGxiblFqTkRreU9EVXpOamd3T1RrMU9EZzFNRGN6TVNJc0ltbHpjM1ZsWkNJNklqSXdNalF0TURrdE1UWlVNVGM2TURjNk5ESmFJaXdpZG1Gc2FXUkdjbTl0SWpvaU1qQXlOQzB3T1MweE5sUXhOem93TnpvME1sb2lMQ0pqY21Wa1pXNTBhV0ZzVTJOb1pXMWhJanA3SW1sa0lqb2lhSFIwY0hNNkx5OWhjR2t0WTI5dVptOXliV0Z1WTJVdVpXSnphUzVsZFM5MGNuVnpkR1ZrTFhOamFHVnRZWE10Y21WbmFYTjBjbmt2ZGpJdmMyTm9aVzFoY3k5Nk0wMW5WVVpWYTJJM01qSjFjVFI0TTJSMk5YbEJTbTF1VG0xNlJFWmxTelZWUXpoNE9ETlJiMlZNU2swaUxDSjBlWEJsSWpvaVJuVnNiRXB6YjI1VFkyaGxiV0ZXWVd4cFpHRjBiM0l5TURJeEluMHNJbWx6YzNWbGNpSTZJbVJwWkRwbFluTnBPbnB0U0ZsdWFtUnRXblp4ZERsaFZrMHhWMlJEZEVGU0lpd2lhWE56ZFdGdVkyVkVZWFJsSWpvaU1qQXlOQzB3T1MweE5sUXhOem93TnpvME1sb2lMQ0psZUhCcGNtRjBhVzl1UkdGMFpTSTZJakl3TWpVdE1Ea3RNVFZVTVRjNk1EYzZOREphSWl3aVkzSmxaR1Z1ZEdsaGJGTjFZbXBsWTNRaU9uc2lhV1FpT2lKa2FXUTZhMlY1T25veVpHMTZSRGd4WTJkUWVEaFdhMmszU21KMWRVMXRSbGx5VjFCbldXOTVkSGxyVlZvelpYbHhhSFF4YWpsTFluUTJaRWhOZW1kaVVGVmFZWE5PZGxKQmFYWjZjbTAyT1c1M09XdHpWV05oVTJObVdIazFUVnBqTldGNFRHVjRZa3BwVldOME5rNDJiWGgwT1hOM09VNUJlR2RoUmtRNVpuSlZRblZXY21aVVRuQmtTRkpZWlhsWVZFSmtTMmgyU0Vwd1kxWldlVGRMY0VSaFYyWmhaVmhJY1dwWVRITm1ia3BIZGtOdVFrcHpXRElpTENKcFpERWlPaUprYVdRNmEyVjVPbm95WkcxNlJEZ3hZMmRRZURoV2EyazNTbUoxZFUxdFJsbHlWMUJuV1c5NWRIbHJWVm96WlhseGFIUXhhamxMWW5RMlpFaE5lbWRpVUZWYVlYTk9kbEpCYVhaNmNtMDJPVzUzT1d0elZXTmhVMk5tV0hrMVRWcGpOV0Y0VEdWNFlrcHBWV04wTms0MmJYaDBPWE4zT1U1QmVHZGhSa1E1Wm5KVlFuVldjbVpVVG5Ca1NGSllaWGxZVkVKa1MyaDJTRXB3WTFaV2VUZExjRVJoVjJaaFpWaEljV3BZVEhObWJrcEhka051UWtweldESWlmWDBzSW1wMGFTSTZJblpqT25WelpXeG1PbUZuWlc1MEl6UTVNamcxTXpZNE1EazVOVGc0TlRBM016RWlmUS5Ba3ZrZ3pDVUl4cVE1WjgwSjNGTXFuVGZvd1MyVjZpazdjcVc1OW1vVjVreEJHcVRhemhlT2tQbVZhaXdQWG0wQmpwaHVLaDFvWF82QXRTOXc1alVBUSJdfSwiZXhwIjoxNzI3Mjc5NzMxLCJpYXQiOjE3MjcyNzk0MzEsIm5vbmNlIjoidXlIaUwxdE9NSGl6cjF3NGdPNDk2ZyIsImp0aSI6Ijg3YWFiZjIwLTUwODctNDAyZS1hOWZlLTljOGUwM2U3ZjgzOSJ9.SMH_G9B-JzxgQ3sdBpshU_hdScb0GJqzXY2HX-3wTJuS9IHFELCuPKubT-WheV_zt07lDH2n4xv9sn1BCY5RvA
                """.trim(),
            state = TestsHelper.KEYCLOAK_STATE,
            presentationSubmission = """
                {\"id\":\"holder-wallet-qualification-presentation\",\"definition_id\":\"holder-wallet-qualification-presentation\",\"descriptor_map\":[{\"id\":\"same-device-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[0]\"}},{\"id\":\"cross-device-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[1]\"}},{\"id\":\"same-device-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[2]\"}},{\"id\":\"cross-device-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[3]\"}},{\"id\":\"same-device-pre-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-pre-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[4]\"}},{\"id\":\"cross-device-pre-authorised-in-time-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-pre-authorised-in-time-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[5]\"}},{\"id\":\"same-device-pre-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"same-device-pre-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[6]\"}},{\"id\":\"cross-device-pre-authorised-deferred-credential\",\"path\":\"$\",\"format\":\"jwt_vp\",\"path_nested\":{\"id\":\"cross-device-pre-authorised-deferred-credential\",\"format\":\"jwt_vc\",\"path\":\"$.verifiableCredential[7]\"}}]}
                """.trim(),
        )

        val expectedResultKeycloak =
            """
openid://redirect?code=uyHiL1tOMHizr1w4gO496g&state=8d8b6a3d-4bc0-4234-9a9a-ed1928815502
            """.trimIndent()
    }

    object DirectPostPassport {
        const val nonce = "n-0S6_WzA2Mj"
        val request = AuthDirectPost(
            idToken = ID_TOKEN_PASSPORT,
            state = TestsHelper.EPASSPORT_STATE
        )
        val expectedResult =
            """
         openid://?code=7079889543297632084&state=95d86046-ef6d-40b5-8da0-1b27f2f9de4a
            """.trimIndent()
    }

    object GetToken {
        val preAuthorizedCode = """
            eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQiLCJhdXRob3JpemF0aW9uX2RldGFpbHMiOnsidHlwZSI6Im9wZW5pZF9jcmVkZW50aWFsIiwiZm9ybWF0Ijoiand0X3ZjIiwibG9jYXRpb25zIjpudWxsLCJ0eXBlcyI6WyJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlZlcmlmaWFibGVBdHRlc3RhdGlvbiIsIkNUV2FsbGV0U2FtZVByZUF1dGhvcmlzZWRJblRpbWUiXSwiY3JlZGVudGlhbENvbmZpZ3VyYXRpb25JZCI6bnVsbCwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbH0sImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIiwiZXhwIjoxNzE3NDkzNDE5LCJpYXQiOjE3MTc0ODk4MTksImNsaWVudF9pZCI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCJ9.f4MpjBDpraopQ8krNwxJYW5TmbjhdEBosh1xuNZp1qpJOQfyPwAaQpdcQjqzlHhZB9cpldWg1cnHDsFWNswawQ
        """.trimIndent()
        val request = TokenRequest(
            grantType = GRAN_TYPE.AUTH_CODE,
            clientId = """
                did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC
                """.trim(),
            code = "8661435939417066112",
            codeVerifier = "KeQIuyPXkVX8OAM3LXCMPR5E40ylm_dpYAaa3IxmlJw"
        )
        val expectedResult = TokenResponse(
            accessToken = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtib2o3ZzlQZlhKeGJiczRLWWVneXI3RUxuRlZucERNemJKSkRETlpqYXZYNmp2dERtQUxNYlhBR1c2N3BkVGdGZWEyRnJHR1NGczhFanhpOTZvRkxHSGNMNFA2YmpMRFBCSkV2UlJIU3JHNExzUG5lNTJmY3p0Mk1XakhMTEpCdmhBQyIsImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aCIsImNsYWltcyI6eyJhdXRob3JpemF0aW9uRGV0YWlscyI6W3sidHlwZSI6Im9wZW5pZF9jcmVkZW50aWFsIiwiZm9ybWF0Ijoiand0X3ZjIiwibG9jYXRpb25zIjpbImh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIl0sInR5cGVzIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiQ1RXYWxsZXRTYW1lQXV0aG9yaXNlZEluVGltZSJdfV0sImNOb25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIiLCJjTm9uY2VFeHBpcmVzSW4iOjE3MTE0NTQ2MjQ0NTgsImNsaWVudElkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIn0sImV4cCI6MTcxMTQ1NDYyNCwiaWF0IjoxNzExNDU0MzI0LCJub25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIifQ.-jOr1AhgvTNbQitpAe_OrMlbfsiYgzJDO6wHcVu3oPK27FWV5MxFDbCjJ40GAw4K1EOlvg7vxdN3r5yHvSIqAw
                """.trim(),
            idToken = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJhdWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJpc3MiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2F1dGgiLCJleHAiOjE3MTE0NTQ2MjQsImlhdCI6MTcxMTQ1NDMyNCwibm9uY2UiOiI4NjYxNDM1OTM5NDE3MDY2MTEyIn0.ybAJLMHx8L7fdakTXiJcsDIUbP-nEtFqNdninu1abZqTjWhJK4SJ7oxUYYMCSANt6RHUyFIMuqK45zKduVk8Gg
                """.trim(),
            tokenType = TOKEN_TYPE.BEARER,
            expiresIn = 1711454624458,
            cNonce = "8661435939417066112",
            cNonceExpiresIn = 1711454624458
        )

        val requestEPassport = TokenRequest(
            grantType = GRAN_TYPE.AUTH_CODE,
            clientId = """
did:jwk:eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2Iiwia2lkIjoiUVRIb09rbVlQa2p0MUU1WjcxenpRVUdhX1dadEhFQ0FJak9UM214SXk3cyIsIngiOiIxMjBfc1BGOVFFalpoVF9GQV9NcFlaR0ZVbTl1STJ0Yks0MXVhWm44Q284IiwieSI6Im8tYmpzZF9ZTlRaSEhpVVZGdW44UjB5OFNhSGRGY1FPV3dTMkFxTDg0ZkEifQ
                """.trim(),
            code = "7649839783685200101",
            codeVerifier = "RXhjhR1RNT0FBaExIoptLmCyhPiebxygAOtg7aoEBrGH"
        )
        val expectedResultEPassport = TokenResponse(
            accessToken = """
eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvaXNzdWVyIiwic3ViIjoiZGlkOmp3azpleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbEF0TWpVMklpd2lhMmxrSWpvaVVWUkliMDlyYlZsUWEycDBNVVUxV2pjeGVucFJWVWRoWDFkYWRFaEZRMEZKYWs5VU0yMTRTWGszY3lJc0luZ2lPaUl4TWpCZmMxQkdPVkZGYWxwb1ZGOUdRVjlOY0ZsYVIwWlZiVGwxU1RKMFlrczBNWFZoV200NFEyODRJaXdpZVNJNkltOHRZbXB6WkY5WlRsUmFTRWhwVlZaR2RXNDRVakI1T0ZOaFNHUkdZMUZQVjNkVE1rRnhURGcwWmtFaWZRIiwiaXNzIjoiaHR0cHM6Ly91c2VsZi1hZ2VudC5rOHMtY2x1c3Rlci50YW5nby5yaWQtaW50cmFzb2Z0LmV1L2F1dGgiLCJjbGFpbXMiOnsiYXV0aG9yaXphdGlvbkRldGFpbHMiOlt7InR5cGUiOiJvcGVuaWRfY3JlZGVudGlhbCIsImZvcm1hdCI6Imp3dF92YyIsImxvY2F0aW9ucyI6bnVsbCwidHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJlUGFzc3BvcnQiXSwiY3JlZGVudGlhbENvbmZpZ3VyYXRpb25JZCI6bnVsbCwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbH1dLCJjTm9uY2UiOiI1NTgzMTk1ODk2MTU2MDk1NzU1IiwiY05vbmNlRXhwaXJlc0luIjoxNzMzMTMyMjM5MTk3LCJjbGllbnRJZCI6ImRpZDpqd2s6ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SWxBdE1qVTJJaXdpYTJsa0lqb2lVVlJJYjA5cmJWbFFhMnAwTVVVMVdqY3hlbnBSVlVkaFgxZGFkRWhGUTBGSmFrOVVNMjE0U1hrM2N5SXNJbmdpT2lJeE1qQmZjMUJHT1ZGRmFscG9WRjlHUVY5TmNGbGFSMFpWYlRsMVNUSjBZa3MwTVhWaFdtNDRRMjg0SWl3aWVTSTZJbTh0WW1welpGOVpUbFJhU0VocFZWWkdkVzQ0VWpCNU9GTmhTR1JHWTFGUFYzZFRNa0Z4VERnMFprRWlmUSJ9LCJleHAiOjE3MzMxMzIyMzksImlhdCI6MTczMzEzMTYzOSwibm9uY2UiOiI1NTgzMTk1ODk2MTU2MDk1NzU1In0.g902z2zGFJwVpIAPNInrhqVqlJItlu4RHaVW4ELSIrXt7ChiCuYU1Ay4adiBmCTdAF_TSS7I-ZIIYUveiv8TaQ
                """.trim(),
            idToken = """
eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6andrOmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNklsQXRNalUySWl3aWEybGtJam9pVVZSSWIwOXJiVmxRYTJwME1VVTFXamN4ZW5wUlZVZGhYMWRhZEVoRlEwRkphazlVTTIxNFNYazNjeUlzSW5naU9pSXhNakJmYzFCR09WRkZhbHBvVkY5R1FWOU5jRmxhUjBaVmJUbDFTVEowWWtzME1YVmhXbTQ0UTI4NElpd2llU0k2SW04dFltcHpaRjlaVGxSYVNFaHBWVlpHZFc0NFVqQjVPRk5oU0dSR1kxRlBWM2RUTWtGeFREZzBaa0VpZlEiLCJhdWQiOiJkaWQ6andrOmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNklsQXRNalUySWl3aWEybGtJam9pVVZSSWIwOXJiVmxRYTJwME1VVTFXamN4ZW5wUlZVZGhYMWRhZEVoRlEwRkphazlVTTIxNFNYazNjeUlzSW5naU9pSXhNakJmYzFCR09WRkZhbHBvVkY5R1FWOU5jRmxhUjBaVmJUbDFTVEowWWtzME1YVmhXbTQ0UTI4NElpd2llU0k2SW04dFltcHpaRjlaVGxSYVNFaHBWVlpHZFc0NFVqQjVPRk5oU0dSR1kxRlBWM2RUTWtGeFREZzBaa0VpZlEiLCJpc3MiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvYXV0aCIsImV4cCI6MTczMzEzMjIzOSwiaWF0IjoxNzMzMTMxNjM5LCJub25jZSI6IjU1ODMxOTU4OTYxNTYwOTU3NTUifQ.0uB67FEL5B9Z3o8HJPPxB3T_jkUGTw9bmKebCcydXMUtaMu6V5KY2q5_-3Ye874jEqyJ2yhy7OHTMmnZsRBAeA
                """.trim(),
            tokenType = TOKEN_TYPE.BEARER,
            expiresIn = 1733132239197,
            cNonce = "5583195896156095755",
            cNonceExpiresIn = 1733132239197
        )

        val requestKeycloak = TokenRequest(
            grantType = GRAN_TYPE.AUTH_CODE,
            clientId = """
did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbt6dHMzgbPUZasNvRAivzrm69nw9ksUcaScfXy5MZc5axLexbJiUct6N6mxt9sw9NAxgaFD9frUBuVrfTNpdHRXeyXTBdKhvHJpcVVy7KpDaWfaeXHqjXLsfnJGvCnBJsX2
                """.trim(),
            code = "uyHiL1tOMHizr1w4gO496g",
            codeVerifier = "password"
        )

        val expectedResultKeycloak = TokenResponse(
            accessToken = """
eyJraWQiOiJkaWQ6ZWJzaTp6Z3pHTnQ5MkpyOU05ZnNZY291Z1FkQyIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjUuYXJpLWJpcC5ldS9pc3N1ZXIiLCJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnQ2ZEhNemdiUFVaYXNOdlJBaXZ6cm02OW53OWtzVWNhU2NmWHk1TVpjNWF4TGV4YkppVWN0Nk42bXh0OXN3OU5BeGdhRkQ5ZnJVQnVWcmZUTnBkSFJYZXlYVEJkS2h2SEpwY1ZWeTdLcERhV2ZhZVhIcWpYTHNmbkpHdkNuQkpzWDIiLCJpc3MiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjUuYXJpLWJpcC5ldS9hdXRoIiwiY2xhaW1zIjp7ImNOb25jZSI6InV5SGlMMXRPTUhpenIxdzRnTzQ5NmciLCJjTm9uY2VFeHBpcmVzSW4iOjE3MjcyODAwMzE0MDYsImNsaWVudElkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2J0NmRITXpnYlBVWmFzTnZSQWl2enJtNjludzlrc1VjYVNjZlh5NU1aYzVheExleGJKaVVjdDZONm14dDlzdzlOQXhnYUZEOWZyVUJ1VnJmVE5wZEhSWGV5WFRCZEtodkhKcGNWVnk3S3BEYVdmYWVYSHFqWExzZm5KR3ZDbkJKc1gyIn0sImV4cCI6MTcyNzI4MDAzMSwiaWF0IjoxNzI3Mjc5NDMxLCJub25jZSI6InV5SGlMMXRPTUhpenIxdzRnTzQ5NmcifQ._I2hMzIOEzpDdn8nrUZtWghSwjS5ydU-JeQYeJUdcSfRvHzRQb5OgGsU9_o0t9kpUkngKgK6E9wiSvY9QkYdqQ
                """.trim(),
            idToken = """
eyJraWQiOiJkaWQ6ZWJzaTp6Z3pHTnQ5MkpyOU05ZnNZY291Z1FkQyIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnQ2ZEhNemdiUFVaYXNOdlJBaXZ6cm02OW53OWtzVWNhU2NmWHk1TVpjNWF4TGV4YkppVWN0Nk42bXh0OXN3OU5BeGdhRkQ5ZnJVQnVWcmZUTnBkSFJYZXlYVEJkS2h2SEpwY1ZWeTdLcERhV2ZhZVhIcWpYTHNmbkpHdkNuQkpzWDIiLCJhdWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnQ2ZEhNemdiUFVaYXNOdlJBaXZ6cm02OW53OWtzVWNhU2NmWHk1TVpjNWF4TGV4YkppVWN0Nk42bXh0OXN3OU5BeGdhRkQ5ZnJVQnVWcmZUTnBkSFJYZXlYVEJkS2h2SEpwY1ZWeTdLcERhV2ZhZVhIcWpYTHNmbkpHdkNuQkpzWDIiLCJpc3MiOiJodHRwczovL3VzZWxmLWFnZW50LmRldjUuYXJpLWJpcC5ldS9hdXRoIiwiZXhwIjoxNzI3MjgwMDMxLCJpYXQiOjE3MjcyNzk0MzEsIm5vbmNlIjoidXlIaUwxdE9NSGl6cjF3NGdPNDk2ZyJ9.ej2rGup_3JYoc2SW-bWCP-DBNnnmsRhIbMUc_3UpNyvCmpPBzcv0kq-CsMUzSCEM0YeNOgCkOJuG-qMoUH_KCQ
                """.trim(),
            tokenType = TOKEN_TYPE.BEARER,
            expiresIn = 1727280031406,
            cNonce = "uyHiL1tOMHizr1w4gO496g",
            cNonceExpiresIn = 1727280031406
        )
    }

    object IssueCredential {
        val did = """
            did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC
        """.trim()
        val bearer = """
        eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsInN1YiI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUtib2o3ZzlQZlhKeGJiczRLWWVneXI3RUxuRlZucERNemJKSkRETlpqYXZYNmp2dERtQUxNYlhBR1c2N3BkVGdGZWEyRnJHR1NGczhFanhpOTZvRkxHSGNMNFA2YmpMRFBCSkV2UlJIU3JHNExzUG5lNTJmY3p0Mk1XakhMTEpCdmhBQyIsImlzcyI6Imh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvYXV0aCIsImNsYWltcyI6eyJhdXRob3JpemF0aW9uRGV0YWlscyI6W3sidHlwZSI6Im9wZW5pZF9jcmVkZW50aWFsIiwiZm9ybWF0Ijoiand0X3ZjIiwibG9jYXRpb25zIjpbImh0dHBzOi8vdGFkcG9sZS1pbnRlcm5hbC1tYW1tYWwubmdyb2stZnJlZS5hcHAvaXNzdWVyIl0sInR5cGVzIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiVmVyaWZpYWJsZUF0dGVzdGF0aW9uIiwiQ1RXYWxsZXRTYW1lQXV0aG9yaXNlZEluVGltZSJdfV0sImNOb25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIiLCJjTm9uY2VFeHBpcmVzSW4iOjE3MTE0NTQ2MjQ0NTgsImNsaWVudElkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIn0sImV4cCI6MTcxMTQ1NDYyNCwiaWF0IjoxNzExNDU0MzI0LCJub25jZSI6Ijg2NjE0MzU5Mzk0MTcwNjYxMTIifQ.-jOr1AhgvTNbQitpAe_OrMlbfsiYgzJDO6wHcVu3oPK27FWV5MxFDbCjJ40GAw4K1EOlvg7vxdN3r5yHvSIqAw
        """.trimIndent()

        val bearerEPassport = """
            eyJraWQiOiJkaWQ6ZWJzaTp6blh6TVl6NVRKSlh1eUZ4RWNMRjRLZCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvaXNzdWVyIiwic3ViIjoiZGlkOmp3azpleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbEF0TWpVMklpd2lhMmxrSWpvaVVWUkliMDlyYlZsUWEycDBNVVUxV2pjeGVucFJWVWRoWDFkYWRFaEZRMEZKYWs5VU0yMTRTWGszY3lJc0luZ2lPaUl4TWpCZmMxQkdPVkZGYWxwb1ZGOUdRVjlOY0ZsYVIwWlZiVGwxU1RKMFlrczBNWFZoV200NFEyODRJaXdpZVNJNkltOHRZbXB6WkY5WlRsUmFTRWhwVlZaR2RXNDRVakI1T0ZOaFNHUkdZMUZQVjNkVE1rRnhURGcwWmtFaWZRIiwiaXNzIjoiaHR0cHM6Ly91c2VsZi1hZ2VudC5rOHMtY2x1c3Rlci50YW5nby5yaWQtaW50cmFzb2Z0LmV1L2F1dGgiLCJjbGFpbXMiOnsiYXV0aG9yaXphdGlvbkRldGFpbHMiOlt7InR5cGUiOiJvcGVuaWRfY3JlZGVudGlhbCIsImZvcm1hdCI6Imp3dF92YyIsImxvY2F0aW9ucyI6bnVsbCwidHlwZXMiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXR0ZXN0YXRpb24iLCJlUGFzc3BvcnQiXSwiY3JlZGVudGlhbENvbmZpZ3VyYXRpb25JZCI6bnVsbCwiY3JlZGVudGlhbERlZmluaXRpb24iOm51bGwsInZjdCI6bnVsbH1dLCJjTm9uY2UiOiI1NTgzMTk1ODk2MTU2MDk1NzU1IiwiY05vbmNlRXhwaXJlc0luIjoxNzMzMTMyMjM5MTk3LCJjbGllbnRJZCI6ImRpZDpqd2s6ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SWxBdE1qVTJJaXdpYTJsa0lqb2lVVlJJYjA5cmJWbFFhMnAwTVVVMVdqY3hlbnBSVlVkaFgxZGFkRWhGUTBGSmFrOVVNMjE0U1hrM2N5SXNJbmdpT2lJeE1qQmZjMUJHT1ZGRmFscG9WRjlHUVY5TmNGbGFSMFpWYlRsMVNUSjBZa3MwTVhWaFdtNDRRMjg0SWl3aWVTSTZJbTh0WW1welpGOVpUbFJhU0VocFZWWkdkVzQ0VWpCNU9GTmhTR1JHWTFGUFYzZFRNa0Z4VERnMFprRWlmUSJ9LCJleHAiOjE3MzMxMzIyMzksImlhdCI6MTczMzEzMTYzOSwibm9uY2UiOiI1NTgzMTk1ODk2MTU2MDk1NzU1In0.g902z2zGFJwVpIAPNInrhqVqlJItlu4RHaVW4ELSIrXt7ChiCuYU1Ay4adiBmCTdAF_TSS7I-ZIIYUveiv8TaQ
        """.trimIndent()

        val requestEPassport = CredentialRequest(
            format = FORMAT.JWT_VC,
            types = arrayListOf(
                CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                CREDENTIAL_TYPES.TANGO_EPASSPORT_INFO
            ),
            proof = CredentialRequestProof(
                proofType = PROOF_TYPE.JWT,
                jwt = """
eyJraWQiOiJkaWQ6andrOmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNklsQXRNalUySWl3aWEybGtJam9pVVZSSWIwOXJiVmxRYTJwME1VVTFXamN4ZW5wUlZVZGhYMWRhZEVoRlEwRkphazlVTTIxNFNYazNjeUlzSW5naU9pSXhNakJmYzFCR09WRkZhbHBvVkY5R1FWOU5jRmxhUjBaVmJUbDFTVEowWWtzME1YVmhXbTQ0UTI4NElpd2llU0k2SW04dFltcHpaRjlaVGxSYVNFaHBWVlpHZFc0NFVqQjVPRk5oU0dSR1kxRlBWM2RUTWtGeFREZzBaa0VpZlEjZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SWxBdE1qVTJJaXdpYTJsa0lqb2lVVlJJYjA5cmJWbFFhMnAwTVVVMVdqY3hlbnBSVlVkaFgxZGFkRWhGUTBGSmFrOVVNMjE0U1hrM2N5SXNJbmdpT2lJeE1qQmZjMUJHT1ZGRmFscG9WRjlHUVY5TmNGbGFSMFpWYlRsMVNUSjBZa3MwTVhWaFdtNDRRMjg0SWl3aWVTSTZJbTh0WW1welpGOVpUbFJhU0VocFZWWkdkVzQ0VWpCNU9GTmhTR1JHWTFGUFYzZFRNa0Z4VERnMFprRWlmUSIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6andrOmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNklsQXRNalUySWl3aWEybGtJam9pVVZSSWIwOXJiVmxRYTJwME1VVTFXamN4ZW5wUlZVZGhYMWRhZEVoRlEwRkphazlVTTIxNFNYazNjeUlzSW5naU9pSXhNakJmYzFCR09WRkZhbHBvVkY5R1FWOU5jRmxhUjBaVmJUbDFTVEowWWtzME1YVmhXbTQ0UTI4NElpd2llU0k2SW04dFltcHpaRjlaVGxSYVNFaHBWVlpHZFc0NFVqQjVPRk5oU0dSR1kxRlBWM2RUTWtGeFREZzBaa0VpZlEiLCJhdWQiOiJodHRwczovL3VzZWxmLWFnZW50Lms4cy1jbHVzdGVyLnRhbmdvLnJpZC1pbnRyYXNvZnQuZXUvaXNzdWVyIiwiaWF0IjoxNzMzMTMxNjM5LCJub25jZSI6ImV5SnJhV1FpT2lKa2FXUTZaV0p6YVRwNmJsaDZUVmw2TlZSS1NsaDFlVVo0UldOTVJqUkxaQ0lzSW5SNWNDSTZJa3BYVkNJc0ltRnNaeUk2SWtWVE1qVTJJbjAuZXlKaGRXUWlPaUpvZEhSd2N6b3ZMM1Z6Wld4bUxXRm5aVzUwTG1zNGN5MWpiSFZ6ZEdWeUxuUmhibWR2TG5KcFpDMXBiblJ5WVhOdlpuUXVaWFV2YVhOemRXVnlJaXdpYzNWaUlqb2laR2xrT21wM2F6cGxlVXB5WkVocmFVOXBTa1pSZVVselNXMU9lV1JwU1RaSmJFRjBUV3BWTWtscGQybGhNbXhyU1dwdmFWVldVa2xpTURseVlsWnNVV0V5Y0RCTlZWVXhWMnBqZUdWdWNGSldWV1JvV0RGa1lXUkZhRVpSTUVaS1lXczVWVTB5TVRSVFdHc3pZM2xKYzBsdVoybFBhVWw0VFdwQ1ptTXhRa2RQVmtaR1lXeHdiMVpHT1VkUlZqbE9ZMFpzWVZJd1dsWmlWR3d4VTFSS01GbHJjekJOV0Zab1YyMDBORkV5T0RSSmFYZHBaVk5KTmtsdE9IUlpiWEI2V2tZNVdsUnNVbUZUUldod1ZsWmFSMlJYTkRSVmFrSTFUMFpPYUZOSFVrZFpNVVpRVmpOa1ZFMXJSbmhVUkdjd1dtdEZhV1pSSWl3aWFYTnpJam9pYUhSMGNITTZMeTkxYzJWc1ppMWhaMlZ1ZEM1ck9ITXRZMngxYzNSbGNpNTBZVzVuYnk1eWFXUXRhVzUwY21GemIyWjBMbVYxTDJGMWRHZ2lMQ0pqYkdGcGJYTWlPbnNpWVhWMGFHOXlhWHBoZEdsdmJrUmxkR0ZwYkhNaU9sdDdJblI1Y0dVaU9pSnZjR1Z1YVdSZlkzSmxaR1Z1ZEdsaGJDSXNJbVp2Y20xaGRDSTZJbXAzZEY5Mll5SXNJbXh2WTJGMGFXOXVjeUk2Ym5Wc2JDd2lkSGx3WlhNaU9sc2lWbVZ5YVdacFlXSnNaVU55WldSbGJuUnBZV3dpTENKV1pYSnBabWxoWW14bFFYUjBaWE4wWVhScGIyNGlMQ0psVUdGemMzQnZjblFpWFN3aVkzSmxaR1Z1ZEdsaGJFTnZibVpwWjNWeVlYUnBiMjVKWkNJNmJuVnNiQ3dpWTNKbFpHVnVkR2xoYkVSbFptbHVhWFJwYjI0aU9tNTFiR3dzSW5aamRDSTZiblZzYkgxZExDSmpUbTl1WTJVaU9pSTFOVGd6TVRrMU9EazJNVFUyTURrMU56VTFJaXdpWTA1dmJtTmxSWGh3YVhKbGMwbHVJam94TnpNek1UTXlNak01TVRrM0xDSmpiR2xsYm5SSlpDSTZJbVJwWkRwcWQyczZaWGxLY21SSWEybFBhVXBHVVhsSmMwbHRUbmxrYVVrMlNXeEJkRTFxVlRKSmFYZHBZVEpzYTBscWIybFZWbEpKWWpBNWNtSldiRkZoTW5Bd1RWVlZNVmRxWTNobGJuQlNWbFZrYUZneFpHRmtSV2hHVVRCR1NtRnJPVlZOTWpFMFUxaHJNMk41U1hOSmJtZHBUMmxKZUUxcVFtWmpNVUpIVDFaR1JtRnNjRzlXUmpsSFVWWTVUbU5HYkdGU01GcFdZbFJzTVZOVVNqQlphM013VFZoV2FGZHRORFJSTWpnMFNXbDNhV1ZUU1RaSmJUaDBXVzF3ZWxwR09WcFViRkpoVTBWb2NGWldXa2RrVnpRMFZXcENOVTlHVG1oVFIxSkhXVEZHVUZZelpGUk5hMFo0VkVSbk1GcHJSV2xtVVNKOUxDSmxlSEFpT2pFM016TXhNekl5TXprc0ltbGhkQ0k2TVRjek16RXpNVFl6T1N3aWJtOXVZMlVpT2lJMU5UZ3pNVGsxT0RrMk1UVTJNRGsxTnpVMUluMC5nOTAyejJ6R0ZKd1ZwSUFQTklucmhxVnFsSkl0bHU0UkhhVlc0RUxTSXJYdDdDaGlDdVlVMUF5NGFkaUJtQ1RkQUZfVFNTN0ktWklJWVV2ZWl2OFRhUSJ9.2thtj_Fc-vMBnuXhA76LBPWLkz8y_cezvcH6qnhg_FjruM_bXvRwBBV0vgTsdNKDfL46QleR-kSO4q3dL3dYug
                    """.trim()
            )
        )

        val request = CredentialRequest(
            format = FORMAT.JWT_VC,
            types = arrayListOf(
                CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                CREDENTIAL_TYPES.EBSI_SAME_PRE_AUTH
            ),
            proof = CredentialRequestProof(
                proofType = PROOF_TYPE.JWT,
                jwt = """
                    eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6IkVTMjU2Iiwia2lkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDI3oyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMifQ.eyJpc3MiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsIm5vbmNlIjoiODY2MTQzNTkzOTQxNzA2NjExMiIsImlhdCI6MTcxMTQ1NDMyNH0.Yr2UoyLBw7jOH6Kl3JTXOpS43dCreVeYuWAncp3tV4O4M357gMsvtUDh-Y0ve_NJiGk9yjZ5EPXJJ9bmml0HCQ
                    """.trim()
            )
        )

        val expectedResult = CredentialResponse(
            format = FORMAT.JWT_VC,
            credential = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJuYmYiOjE3MTE0NTQzMjQsImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImV4cCI6MTcxMTQ1NDMyNCwiaWF0IjoxNzExNDU0MzI0LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL2V4YW1wbGVzL3YxIl0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlZlcmlmaWFibGVBdHRlc3RhdGlvbiIsIkNUV2FsbGV0U2FtZUF1dGhvcmlzZWRJblRpbWUiXSwiaWQiOiJ2Yzp1c2VsZjphZ2VudDojNjc3NTY5MzczMDM1MTg1MDg0MyIsImNyZWRlbnRpYWxTY2hlbWEiOnsiaWQiOiJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L3RydXN0ZWQtc2NoZW1hcy1yZWdpc3RyeS92Mi9zY2hlbWFzL3ozTWdVRlVrYjcyMnVxNHgzZHY1eUFKbW5ObXpERmVLNVVDOHg4M1FvZUxKTSIsInR5cGUiOiJGdWxsSnNvblNjaGVtYVZhbGlkYXRvcjIwMjEifSwidmFsaWRGcm9tIjoiMjAyNC0wMy0yNlQxMTo1ODo0NFoiLCJpc3N1ZWQiOiIyMDI0LTAzLTI2VDExOjU4OjQ0WiIsImlzc3VlciI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDMtMjZUMTE6NTg6NDRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDI0LTAzLTI2VDExOjU4OjQ0WiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIiwiaWQxIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIiwiZ2l2ZW5fbmFtZSI6IkFsaWNlIiwiZmFtaWx5X25hbWUiOiJEb2UiLCJlbWFpbCI6ImFsaWNlLmRvZUBldmlkZW4uY29tIn19LCJqdGkiOiJ2Yzp1c2VsZjphZ2VudDojNjc3NTY5MzczMDM1MTg1MDg0MyJ9.ginVNKl663CrYm_QWLnoqDwNS571BnhGPAkdf_uOW8PfNEOG2MUtZBIpkAPFSSqFaivUMco4_8tNuc88HMsqgw
                """.trim()
        )

        val expectedCredential = VerifiableCredential.fromJson(
            """
            {"@context":["https://www.w3.org/2018/credentials/v1","https://www.w3.org/2018/credentials/examples/v1"],"type":["VerifiableCredential","VerifiableAttestation","CTWalletSameAuthorisedInTime"],"id":"vc:uself:agent:#7048427358581316414","credentialSchema":{"id":"https://api-conformance.ebsi.eu/trusted-schemas-registry/v2/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM","type":"FullJsonSchemaValidator2021"},"validFrom":"2024-06-03T11:18:36Z","issued":"2024-06-03T11:18:36Z","issuer":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4","issuanceDate":"2024-06-03T11:18:36Z","expirationDate":"2025-06-02T11:18:36Z","credentialSubject":{"id":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC","id1":"did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC"}}
            """.trimIndent()
        )

        val expectedCredentialPassport = VerifiableCredential.fromJson(
            """
{
    "@context": [
      "https://www.w3.org/2018/credentials/v1",
      "https://www.w3.org/2018/credentials/examples/v1"
    ],
    "type": [
      "VerifiableCredential",
      "ePassport"
    ],
    "id": "vc:uself:agent:#8133274365875303553",
    "credentialSchema": {
      "id": "https://api-conformance.ebsi.eu/trusted-schemas-registry/v2/schemas/z3MgUFUkb722uq4x3dv5yAJmnNmzDFeK5UC8x83QoeLJM",
      "type": "FullJsonSchemaValidator2021"
    },
    "validFrom": "2024-03-26T11:58:51Z",
    "issued": "2024-03-26T11:58:51Z",
    "issuer": "did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kbpir6czBznRg5mU3ufPCmJPBxQFCELgjthyQoAsDPT1fhdm7W86jCQCucDcdGdv2m3u7FtPioaRj4NPhJK9ymGjs4r8GbP3pWAYsiYbih4FWAgRrdEd1qyrgcZAH1b7szY4",
    "issuanceDate": "2024-03-26T11:58:51Z",
    "expirationDate": "2024-03-26T11:58:51Z",
    "credentialSubject": {
      "id": "did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC",
      "id1": "did:key:z2dmzD81cgPx8Vki7JbuuMmFYrWPgYoytykUZ3eyqht1j9Kboj7g9PfXJxbbs4KYegyr7ELnFVnpDMzbJJDDNZjavX6jvtDmALMbXAGW67pdTgFea2FrGGSFs8Ejxi96oFLGHcL4P6bjLDPBJEvRRHSrG4LsPne52fczt2MWjHLLJBvhAC",
      "address": "Seestraße 1",
      "passportNumber": "C7648556",
      "familyName": "Little",
      "givenName": "Ross",
      "dateOfBirth": "1830-10-01",
      "placeOfBirth": "Zug",
      "gender": "M",
      "authority": "Aarau AG",
      "nationality": "Schweiz",
      "countryCode": "CHE",
      "passportPhoto": "data:image/png;base64,iVBORw0KGgo...kJggg==",
      "dateOfExpiry": "1935-10-01",
      "dateOfIssue": "11950-10-01"

    }
  }
            """.trimIndent()
        )

        val requestEmployee = CredentialRequest(
            format = FORMAT.JWT_VC,
            types = arrayListOf(
                CREDENTIAL_TYPES.VERIFIABLE_CREDENTIAL,
                CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION,
                CREDENTIAL_TYPES.TANGO_EMPLOYEE_CREDENTIAL
            ),
            proof = CredentialRequestProof(
                proofType = PROOF_TYPE.JWT,
                jwt = """
                    eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6IkVTMjU2Iiwia2lkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDI3oyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMifQ.eyJpc3MiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJhdWQiOiJodHRwczovL3RhZHBvbGUtaW50ZXJuYWwtbWFtbWFsLm5ncm9rLWZyZWUuYXBwL2lzc3VlciIsIm5vbmNlIjoiODY2MTQzNTkzOTQxNzA2NjExMiIsImlhdCI6MTcxMTQ1NDMyNH0.Yr2UoyLBw7jOH6Kl3JTXOpS43dCreVeYuWAncp3tV4O4M357gMsvtUDh-Y0ve_NJiGk9yjZ5EPXJJ9bmml0HCQ
                    """.trim()
            )
        )
    }
}

infix fun CredentialOfferResponse.shouldBeCredentialOffer(
    expected: CredentialOfferResponse
): CredentialOfferResponse {
    this shouldNotBe null
    this.credentials.first().types shouldBe expected.credentials.first().types
    this.grants?.authorizationCode?.issuerState?.let { issuerState ->
        assert(issuerState.isNotEmpty()) { "issuerState should not be empty" }
    }
    this.grants?.preAuthorizedCode?.preAuthorizedCode?.let { preAuthorizedCode ->
        assert(preAuthorizedCode.isNotEmpty()) { "preAuthorizedCode should not be empty" }
    }
    return this
}

infix fun List<String>.shouldBeValidRequestUri(
    expected: List<String>
): List<String> {
    this[0].contains(expected[0])
    this[1] shouldBe expected[1]
    // this[2] shouldBe expected[2]
    this[3] shouldBe expected[3]
    this[4] shouldBe expected[4]
    if (this.size > 5 && expected.size > 5) {
        this[5] shouldBe expected[5]
    }
    // nonce
    // this[6] shouldBe expected[6]
    // request_uri
    // this[7] shouldBe expected[7]
    // request
    // this[8] shouldBe expected[8]
    return this
}

infix fun TokenResponse.shouldBeTokenResponse(
    expected: TokenResponse
): TokenResponse {
    this shouldNotBe null
    this.accessToken shouldNotBe null
    this.idToken shouldNotBe null
    this.expiresIn shouldNotBe null
    this.cNonce shouldNotBe null
    this.cNonceExpiresIn shouldNotBe null
    this.tokenType shouldBe expected.tokenType
    return this
}

infix fun CredentialResponse.shouldBeCredentialResponse(
    expected: CredentialResponse
): CredentialResponse {
    this shouldNotBe null
    this.format shouldBe expected.format
    val credential = JwtVerifiableCredential.fromCompactSerialization(this.credential)
    val expectedCredential = JwtVerifiableCredential.fromCompactSerialization(expected.credential)
    credential.payloadObject.issuer shouldBe expectedCredential.payloadObject.issuer
    credential.payloadObject.credentialSubject shouldBe expectedCredential.payloadObject.credentialSubject
    return this
}

fun CredentialResponse.shouldHaveValidCredentialStatus(): CredentialResponse {
    this shouldNotBe null
    this.credential shouldNotBe null
    val actualCredential = JwtVerifiableCredential.fromCompactSerialization(this.credential)

    val actualCredentialStatus = JSONArray(
        JSONObject(actualCredential.payloadObject.toString()).getJSONArray("credentialStatus").toString()
    ).toListOfMaps()
    actualCredentialStatus.size shouldBe 2

    val actualStatuses = actualCredentialStatus.associateBy { it["statusPurpose"] }
    actualStatuses.keys shouldContainExactlyInAnyOrder listOf("revocation", "suspension")

    for (credentialStatus in actualCredentialStatus) {
        credentialStatus["statusListIndex"].toString().toIntOrNull() shouldNotBe null
        credentialStatus["type"].toString() shouldBe "StatusList2021Entry"
        credentialStatus["statusListCredential"].toString() shouldContain("/status/v")
        credentialStatus["id"] shouldNotBe null
    }

    return this
}

private fun JSONArray.toListOfMaps(): List<Map<String, Any>> {
    val list = mutableListOf<Map<String, Any>>()
    for (i in 0 until this.length()) {
        val jsonObject = this.getJSONObject(i)
        val map = jsonObject.toMap() // Convertir JSONObject a Map
        list.add(map)
    }
    return list
}

infix fun String.shouldBeAuthResponse(
    expected: String
): String {
    this shouldNotBe null
    val authTWT = SignedJWT.parse(this)
    val expectedAuthTWT = SignedJWT.parse(expected)
    authTWT.jwtClaimsSet.issuer shouldBe expectedAuthTWT.jwtClaimsSet.issuer
    authTWT.jwtClaimsSet.claims["client_id"] shouldBe expectedAuthTWT.jwtClaimsSet.claims["client_id"]
    authTWT.jwtClaimsSet.claims["nonce"] shouldBe expectedAuthTWT.jwtClaimsSet.claims["nonce"]
    authTWT.jwtClaimsSet.claims["redirect_uri"] shouldBe expectedAuthTWT.jwtClaimsSet.claims["redirect_uri"]

    return this
}
