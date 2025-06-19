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

package com.eviden.bds.rd.uself.agent.config

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.stereotype.Component

/**
 * Component class responsible for converting environment properties into a Koin module.
 *
 * @property environment The Spring ConfigurableEnvironment that holds the application's environment properties.
 */
@Component
class EnvPropertiesConverter(private val environment: ConfigurableEnvironment) {
    /**
     * Converts the environment properties into a Koin module.
     *
     * @return A Koin Module containing the environment properties as named single instances.
     *
     * It is filtered for the uself (application.yml) properties to avoid sending all the properties
     * (application.yml and environment).
     */
    fun envToModule(): Module {
        val propertiesMap = mutableMapOf<String, String>()

        // Extract property keys from the environment property sources
        val propertyKeys = environment.propertySources
            .filter { it.source is Map<*, *> }
            .flatMap { (it.source as Map<*, *>).keys }
            .mapNotNull { it as? String }
            .filter { it.startsWith("uself") }

        // Populate the properties map with key-value pairs from the environment
        propertyKeys.forEach { key ->
            val value = environment.getProperty(key)
            if (value != null) {
                propertiesMap[key] = value
            }
        }

        // Create a Koin module with the properties map
        return module {
            propertiesMap.forEach { (key, value) ->
                single(named(key)) { value }
            }
        }
    }
}
