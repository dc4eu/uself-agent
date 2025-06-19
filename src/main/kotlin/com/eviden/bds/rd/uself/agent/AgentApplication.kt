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
@file:Suppress("SpreadOperator", "MaxLineLength")

package com.eviden.bds.rd.uself.agent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Class that serves as the entry point for the SSI Agent application.
 *
 * The component scanning is performed in the order specified, starting with `com.eviden.bds.rd.uself.agent.config`,
 * to ensure that dependency injection using Koin is initialized first, followed by the scanning of other application components.
 *
 */

@SpringBootApplication
@ComponentScan(basePackages = ["com.eviden.bds.rd.uself.agent.config", "com.eviden.bds.rd.uself.agent"])
class AgentApplication

fun main(args: Array<String>) {
    println("Starting SSI Agent")
    runApplication<AgentApplication>(*args)
}
