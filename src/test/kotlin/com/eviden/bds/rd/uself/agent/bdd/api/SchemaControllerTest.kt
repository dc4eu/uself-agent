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
package com.eviden.bds.rd.uself.agent.bdd.api

import SchemaRequest
import com.eviden.bds.rd.uself.common.services.schema.repository.SchemaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SchemaControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val schemaRepository: SchemaRepository
) : FunSpec({

    val getSchemaUrl = "/schema/{id}"
    val postSchemaUrl = "/schema"
    val schema = SchemaRequest(
        schema = "https://json-schema.org/draft/2020-12/schema",
        title = "EBSI Verifiable Attestation",
        description = "Defines generic structure for any EBSI related Verifiable Credential",
        type = "object",
        properties = com.eviden.bds.rd.uself.common.models.schema.Properties(
            context = com.eviden.bds.rd.uself.common.models.schema.ContextProperty(
                description = "Semantic context for the issued credential",
                type = "array",
                items = com.eviden.bds.rd.uself.common.models.schema.ItemsProperty(
                    type = "string",
                    format = "uri"
                )
            ),
            id = com.eviden.bds.rd.uself.common.models.schema.IdProperty(
                description = "Globally unique identifier for the issued credential",
                type = "string",
                format = "uri"
            ),
            type = com.eviden.bds.rd.uself.common.models.schema.TypeProperty(
                description = "Full type chain, used to identify the credential base types",
                type = "array",
                items = com.eviden.bds.rd.uself.common.models.schema.ItemsTypeProperty(type = "string")
            ),
            issuer = com.eviden.bds.rd.uself.common.models.schema.IssuerProperty(
                description = "DID which issued this credential",
                type = "string",
                format = "uri"
            ),
            issuanceDate = com.eviden.bds.rd.uself.common.models.schema.IssuanceDateProperty(
                description = "Defines the date and time, when the issued credential becomes valid",
                type = "string",
                format = "date-time"
            ),
            issued = com.eviden.bds.rd.uself.common.models.schema.IssuedProperty(
                description = "Defines when the issued credential was issued",
                type = "string",
                format = "date-time"
            ),
            validFrom = com.eviden.bds.rd.uself.common.models.schema.ValidFromProperty(
                description = "Defines the date and time, when the issued credential becomes valid",
                type = "string",
                format = "date-time"
            ),
            validUntil = null, // Esto puede ser opcional, según el JSON
            expirationDate = null, // Esto puede ser opcional
            credentialSubject = com.eviden.bds.rd.uself.common.models.schema.CredentialSubjectProperty(
                description = "Defines information about the subject that is defined by the type chain",
                type = "object",
                properties = com.eviden.bds.rd.uself.common.models.schema.SubjectProperties(
                    id = com.eviden.bds.rd.uself.common.models.schema.IdProperty(
                        description = "Defines the DID of the subject that is described by the issued credential",
                        type = "string",
                        format = "uri"
                    )
                )
            ),
            credentialStatus = com.eviden.bds.rd.uself.common.models.schema.CredentialStatusProperty(
                description = "Defines revocation details for the issued credential",
                type = "object",
                properties = com.eviden.bds.rd.uself.common.models.schema.CredentialStatusProperties(
                    id = com.eviden.bds.rd.uself.common.models.schema.IdProperty(
                        description = "Exact identity for the credential status",
                        type = "string",
                        format = "uri"
                    ),
                    type = com.eviden.bds.rd.uself.common.models.schema.CredentialsTypeProperty(
                        description = "description",
                        type = "string"
                    )
                ),
                required = listOf("id", "type")
            ),
            credentialSchema = com.eviden.bds.rd.uself.common.models.schema.CredentialSchema(
                anyOf = listOf(
                    com.eviden.bds.rd.uself.common.models.schema.AnyOf(ref = "#\$defs/credentialSchema")
                )
            ),
            termsOfUse = com.eviden.bds.rd.uself.common.models.schema.TermsOfUse(
                anyOf = listOf(
                    com.eviden.bds.rd.uself.common.models.schema.AnyOf(ref = "#\$defs/termsOfUse")
                )
            ),
            evidence = com.eviden.bds.rd.uself.common.models.schema.Evidence(
                description = "Contains information about the process which resulted in the issuance of the credential",
                type = "array",
                items = com.eviden.bds.rd.uself.common.models.schema.EvidenceItems(
                    type = "object",
                    properties = com.eviden.bds.rd.uself.common.models.schema.EvidenceProperties(
                        id = com.eviden.bds.rd.uself.common.models.schema.IdProperty(
                            description = "If present, it MUST contain a URL that points to more information",
                            type = "string",
                            format = "uri"
                        ),
                        type = com.eviden.bds.rd.uself.common.models.schema.TypeProperty(
                            description = "Defines the evidence type extension",
                            type = "array",
                            items = com.eviden.bds.rd.uself.common.models.schema.ItemsTypeProperty(type = "string")
                        )
                    ),
                    required = listOf("type")
                )
            ),
            proof = com.eviden.bds.rd.uself.common.models.schema.Proof(
                description = "Contains information about the proof",
                type = "object",
                properties = com.eviden.bds.rd.uself.common.models.schema.ProofProperties(
                    type = com.eviden.bds.rd.uself.common.models.schema.ItemsTypeProperty(type = "string"),
                    proofPurpose = com.eviden.bds.rd.uself.common.models.schema.ItemsTypeProperty(type = "string"),
                    created = com.eviden.bds.rd.uself.common.models.schema.IssuanceDateProperty(
                        description = "Defines the date and time, when the proof has been created",
                        type = "string",
                        format = "date-time"
                    ),
                    verificationMethod = com.eviden.bds.rd.uself.common.models.schema.IssuerProperty(
                        description = "Contains information about the verification method / proof mechanisms",
                        type = "string",
                        format = "uri"
                    ),
                    jws = com.eviden.bds.rd.uself.common.models.schema.IssuerProperty(
                        description = "Defines the proof value in JWS format",
                        type = "string",
                        format = "uri"
                    )
                ),
                required = listOf("type", "proofPurpose", "created", "verificationMethod", "jws")
            )
        ),
        required = listOf(
            "@context",
            "id",
            "type",
            "issuer",
            "issuanceDate",
            "issued",
            "validFrom",
            "credentialSubject",
            "credentialSchema"
        ),
        defs = com.eviden.bds.rd.uself.common.models.schema.Definition(
            credentialSchema = com.eviden.bds.rd.uself.common.models.schema.CredentialSchemaDefinition(
                description = "Contains info about the credential schema on which the issued credential is based",
                type = "object",
                properties = com.eviden.bds.rd.uself.common.models.schema.CredentialSchemaProperties(
                    id = com.eviden.bds.rd.uself.common.models.schema.FieldDefinition(
                        description = "References the credential schema stored on the Trusted Schemas Registry (TSR)",
                        type = "string",
                        format = "uri"
                    ),
                    type = com.eviden.bds.rd.uself.common.models.schema.CredentialTypeEnum(
                        description = "Defines credential schema type",
                        type = "string",
                        enum = listOf("FullJsonSchemaValidator2021")
                    )
                ),
                required = listOf("id", "type")
            ),
            termsOfUse = com.eviden.bds.rd.uself.common.models.schema.TermsOfUseDefinition(
                description = "Contains the terms under which the issued credential was issued",
                type = "object",
                properties = com.eviden.bds.rd.uself.common.models.schema.TermsOfUseProperties(
                    id = com.eviden.bds.rd.uself.common.models.schema.FieldDefinition(
                        description = "Contains a URL that points to more information",
                        type = "string",
                        format = "uri"
                    ),
                    type = com.eviden.bds.rd.uself.common.models.schema.FieldDefinition(
                        description = "Defines the type extension",
                        type = "string"
                    )
                ),
                required = listOf("type")
            )
        )
    )
    val mapper = ObjectMapper()
    val jsonStr: String = mapper.writeValueAsString(schema)

    test("getSchema") {
        val x = schemaRepository.insert(jsonStr)
        mockMvc.perform(MockMvcRequestBuilders.get(getSchemaUrl, x)).andExpect {
            status().isOk
        }.andExpect(content().string(jsonStr))
    }

    test("postSchema") {
        mockMvc.perform(
            MockMvcRequestBuilders.post(postSchemaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andDo { print() }
            .andExpect {
                status().isCreated
            }
    }
})
