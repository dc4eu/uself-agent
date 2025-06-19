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
@file:Suppress("MaxLineLength", "TooManyFunctions")

package com.eviden.bds.rd.uself.agent.config

import com.eviden.bds.rd.uself.common.models.LoadCredentials
import com.eviden.bds.rd.uself.common.models.authenticSource.AuthenticSource
import com.eviden.bds.rd.uself.common.models.credentialSpecification.CredentialSpec
import com.eviden.bds.rd.uself.common.models.openid.client.OpenIdClientData
import com.eviden.bds.rd.uself.common.services.authenticSource.AuthenticSourceService
import com.eviden.bds.rd.uself.common.services.credentialSpecification.CredentialSpecificationService
import com.eviden.bds.rd.uself.common.services.openIdClients.OpenIdClientService
import com.eviden.bds.rd.uself.common.services.tracker.Tracker
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component("InitServer")
class InitServer : InitializingBean {

    private val tracker: Tracker = Tracker(this::class.java)
    private val credSpecService: CredentialSpecificationService by inject(CredentialSpecificationService::class.java)
    private val authenticSourceService: AuthenticSourceService by inject(AuthenticSourceService::class.java)
    private val openIdClientService: OpenIdClientService by inject(OpenIdClientService::class.java)

    @Value("\${uself.loadDir}")
    private lateinit var loadDir: String

    override fun afterPropertiesSet() {
        val listData = getData()
        listData.forEach { data ->
            loadOpenIdClients(data.openIdClients)
            loadAuthenticSources(data.authenticSources)
            loadCredentialSpecifications(data.credentialSpecifications)
        }
        tracker.info("Data loaded successfully")
    }

    private fun loadOpenIdClients(openIdClients: List<OpenIdClientData>) {
        openIdClients.forEach { openIdClient ->
            openIdClientService.createOpenIdClient(openIdClient)
        }
    }

    private fun loadAuthenticSources(authenticSources: List<AuthenticSource>) {
        authenticSources.forEach { authenticSource ->
            authenticSourceService.postAuthenticSource(authenticSource)
        }
    }

    private fun loadCredentialSpecifications(credentialSpecifications: List<CredentialSpec>) {
        credentialSpecifications.forEach { credentialSpecification ->
            credSpecService.postCredentialSpecification(credentialSpecification)
        }
    }

    private fun getData(): ArrayList<LoadCredentials> {
        val list = arrayListOf<LoadCredentials>()
        val directoryPath = loadDir // Replace with the actual directory path
        val directory = File(directoryPath)

        if (directory.exists() && directory.isDirectory) {
            val jsonFiles = directory.listFiles { file -> file.extension == "json" } ?: emptyArray()

            jsonFiles.forEach { file ->
                val jsonContent = file.readText()
                val data = Json.decodeFromString<LoadCredentials>(jsonContent)
                list.add(data)
            }
        } else {
            println("Directory does not exist or is not a directory.")
        }
        return list
    }
}
