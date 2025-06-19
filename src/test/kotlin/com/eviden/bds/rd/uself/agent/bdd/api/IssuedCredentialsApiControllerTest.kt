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

@file:Suppress("NoWildcardImports", "MaxLineLength")

package com.eviden.bds.rd.uself.agent.bdd.api

import com.eviden.bds.rd.uself.agent.mocks.KEYS.createBasicToken
import com.eviden.bds.rd.uself.agent.services.repositories.issuedCredential.IssuedCredentialRepository
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import org.apache.catalina.util.URLEncoder
import org.hamcrest.CoreMatchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IssuedCredentialsApiControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val issuedCredentialRepository: IssuedCredentialRepository
) : FunSpec({

    val getIssuedCredentialUrl = "/issued-credential/{id}"
    val postIssuedCredentialUrl = "/issued-credential"
    val getIssuedCredentialsUrl = "/issued-credential"
    val deleteIssuedCredentialsUrl = "/issued-credential"
    val credentialID = "vc:uself:agent:#6775693730351850843"
    val username = "uself-agent"
    val password = "uself-agent-password"
    val credential = """
                eyJraWQiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYnBpcjZjekJ6blJnNW1VM3VmUENtSlBCeFFGQ0VMZ2p0aHlRb0FzRFBUMWZoZG03Vzg2akNRQ3VjRGNkR2R2Mm0zdTdGdFBpb2FSajROUGhKSzl5bUdqczRyOEdiUDNwV0FZc2lZYmloNEZXQWdScmRFZDFxeXJnY1pBSDFiN3N6WTQjejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnoyZG16RDgxY2dQeDhWa2k3SmJ1dU1tRllyV1BnWW95dHlrVVozZXlxaHQxajlLYm9qN2c5UGZYSnhiYnM0S1llZ3lyN0VMbkZWbnBETXpiSkpERE5aamF2WDZqdnREbUFMTWJYQUdXNjdwZFRnRmVhMkZyR0dTRnM4RWp4aTk2b0ZMR0hjTDRQNmJqTERQQkpFdlJSSFNyRzRMc1BuZTUyZmN6dDJNV2pITExKQnZoQUMiLCJuYmYiOjE3MTE0NTQzMjQsImlzcyI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImV4cCI6MTcxMTQ1NDMyNCwiaWF0IjoxNzExNDU0MzI0LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL2V4YW1wbGVzL3YxIl0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlZlcmlmaWFibGVBdHRlc3RhdGlvbiIsIkNUV2FsbGV0U2FtZUF1dGhvcmlzZWRJblRpbWUiXSwiaWQiOiJ2Yzp1c2VsZjphZ2VudDojNjc3NTY5MzczMDM1MTg1MDg0MyIsImNyZWRlbnRpYWxTY2hlbWEiOnsiaWQiOiJodHRwczovL2FwaS1jb25mb3JtYW5jZS5lYnNpLmV1L3RydXN0ZWQtc2NoZW1hcy1yZWdpc3RyeS92Mi9zY2hlbWFzL3ozTWdVRlVrYjcyMnVxNHgzZHY1eUFKbW5ObXpERmVLNVVDOHg4M1FvZUxKTSIsInR5cGUiOiJGdWxsSnNvblNjaGVtYVZhbGlkYXRvcjIwMjEifSwidmFsaWRGcm9tIjoiMjAyNC0wMy0yNlQxMTo1ODo0NFoiLCJpc3N1ZWQiOiIyMDI0LTAzLTI2VDExOjU4OjQ0WiIsImlzc3VlciI6ImRpZDprZXk6ejJkbXpEODFjZ1B4OFZraTdKYnV1TW1GWXJXUGdZb3l0eWtVWjNleXFodDFqOUticGlyNmN6QnpuUmc1bVUzdWZQQ21KUEJ4UUZDRUxnanRoeVFvQXNEUFQxZmhkbTdXODZqQ1FDdWNEY2RHZHYybTN1N0Z0UGlvYVJqNE5QaEpLOXltR2pzNHI4R2JQM3BXQVlzaVliaWg0RldBZ1JyZEVkMXF5cmdjWkFIMWI3c3pZNCIsImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDMtMjZUMTE6NTg6NDRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDI0LTAzLTI2VDExOjU4OjQ0WiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIiwiaWQxIjoiZGlkOmtleTp6MmRtekQ4MWNnUHg4VmtpN0pidXVNbUZZcldQZ1lveXR5a1VaM2V5cWh0MWo5S2JvajdnOVBmWEp4YmJzNEtZZWd5cjdFTG5GVm5wRE16YkpKREROWmphdlg2anZ0RG1BTE1iWEFHVzY3cGRUZ0ZlYTJGckdHU0ZzOEVqeGk5Nm9GTEdIY0w0UDZiakxEUEJKRXZSUkhTckc0THNQbmU1MmZjenQyTVdqSExMSkJ2aEFDIiwiZ2l2ZW5fbmFtZSI6IkFsaWNlIiwiZmFtaWx5X25hbWUiOiJEb2UiLCJlbWFpbCI6ImFsaWNlLmRvZUBldmlkZW4uY29tIn19LCJqdGkiOiJ2Yzp1c2VsZjphZ2VudDojNjc3NTY5MzczMDM1MTg1MDg0MyJ9.ginVNKl663CrYm_QWLnoqDwNS571BnhGPAkdf_uOW8PfNEOG2MUtZBIpkAPFSSqFaivUMco4_8tNuc88HMsqgw
    """.trimIndent()

    test("getIssuedCredentialUrl") {
        issuedCredentialRepository.insert(credentialID, credential)
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                getIssuedCredentialUrl,
                credentialID
            ).header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
        }
        //  .andExpect(content().json("{}", false))
    }

    test("putIssuedCredentialUrl") {
        issuedCredentialRepository.insert(credentialID, credential)
        mockMvc.perform(
            MockMvcRequestBuilders.put(
                getIssuedCredentialUrl,
                credentialID
            ).header("Authorization", createBasicToken(username, password))
        ).andExpect {
            status().isOk
        }
    }

    test("postIssuedCredential") {
        mockMvc.perform(
            MockMvcRequestBuilders.post(postIssuedCredentialUrl)
                .header("Authorization", createBasicToken(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(credential)
        )
            .andDo { print() }
            .andExpect {
                status().isCreated
            }
    }
    test("getIssuedCredentials") {
        issuedCredentialRepository.insert(credentialID, credential)
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    getIssuedCredentialsUrl
                ).header("Authorization", createBasicToken(username, password))
            )
            .andExpect {
                status().isOk
            }.andExpect(
                content().string("[$credential]")

            )
    }
    test("deleteIssuedCredentials") {
        issuedCredentialRepository.insert(credentialID, credential)
        val formated = URLEncoder.DEFAULT.encode(credentialID, Charsets.UTF_8)
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete(
                    "$deleteIssuedCredentialsUrl/$formated"
                ).header("Authorization", createBasicToken(username, password))
            )
            .andExpect {
                status().isOk
            }.andExpect(
                content().string(containsString(""))

            )
    }
})
