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

// Import task types
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.ByteArrayOutputStream


plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.0.0.5145"
    // detekt plugin
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    // kotest
    // id("io.kotest") version "0.4.11"
    id("jacoco")
    id("com.bmuschko.docker-spring-boot-application") version "9.4.0"

    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
    id("org.spdx.sbom") version "0.9.0"
}

group = "com.eviden.bds.rd.uself"
version = "0.0.1-SNAPSHOT"
val registryPath = System.getProperty("REGISTRY_PATH") ?: findProperty("LOCAL_REGISTRY_PATH").toString()
val nexusURL = System.getProperty("NEXUS_DOCKER_URL") ?: findProperty("LOCAL_NEXUS_DOCKER_URL").toString()
val nexusUsername =
    System.getProperty("NEXUS_DOCKER_USERNAME") ?: findProperty("LOCAL_NEXUS_DOCKER_USERNAME").toString()
val nexusPass = System.getProperty("NEXUS_DOCKER_PASS") ?: findProperty("LOCAL_NEXUS_DOCKER_PASS").toString()

java {
    sourceCompatibility = JavaVersion.VERSION_17
}
// to be  removed when new version of detekt is released
configurations.matching { it.name != "detekt" }

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.danubetech.com/repository/maven-public/") }
    maven { url = uri("https://jcenter.bintray.com/") }
    maven {
        name = "DC4EU"
        url = uri("https://ossdc4eu.urv.cat:8443/repository/DC4EU-maven/")
//        credentials {
//            username = "dc4eupa"
//            password = "dc4eupa#"
//        }
    }
}
// Use KSP Generated sources
sourceSets {
    main {
        java.srcDirs("build/generated/ksp/main/kotlin")
    }
}

jacoco {
    toolVersion = "0.8.11"
}
sonar {
    properties {
        properties["sonar.projectKey"] = "uself-agent"
        properties["sonar.host.url"] = "https://sonari.atosresearch.eu"
        properties["sonar.login"] = System.getProperty("TOKEN_SONAR")
            ?: findProperty("LOCAL_TOKEN_SONAR").toString()
        properties["sonar.coverage.exclusions"] = "**/com/eviden/bds/rd/uself/agent/models/**"
    }
}

dependencies {
    // spring dependencies
    implementation("org.springframework.boot:spring-boot-starter") {
        exclude(group = "commons-logging", module = "commons-logging")
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "commons-logging", module = "commons-logging")
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("io.lettuce:lettuce-core:6.5.5.RELEASE")

    testImplementation("com.github.codemonstur:embedded-redis:1.4.3")
    implementation("com.h2database:h2:2.3.232")
    runtimeOnly("org.postgresql:postgresql")

    // Other Spring Boot dependencies...
    val kotestVersion = "5.9.1"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:1.3.0")

    // Kotlin dependencies
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.20")
    testImplementation(kotlin("test"))

    // Koin dependencies
    // Koin for Kotlin apps
    val koinVersion = "4.0.3"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-annotations:2.0.0")

    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Koin Detekt
    // detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.23.6")
    // Koin Tests
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    val ktorVersion = "3.1.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    // OpenAPI dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    //  Crypto dependencies
    implementation("decentralized-identity:did-common-java:1.18.0")
    api("com.danubetech:verifiable-credentials-java:1.14.0") {
        exclude(group = "org.scijava", module = "native-lib-loader")
        exclude(group = "org.scijava", module = "native-lib-loader-javadoc")
        exclude(group = "org.scijava", module = "native-lib-loader-sources")
    }

    // for openid support
    implementation("com.nimbusds:oauth2-oidc-sdk:11.23.1")
    implementation("com.eviden.bds.rd.uself:uself-common:v0.1.51")
   // implementation("com.eviden.bds.rd.uself:uself-common:v0.1.51")
    {
        exclude(group = "org.scijava", module = "native-lib-loader")
        exclude(group = "org.scijava", module = "native-lib-loader-javadoc")
        exclude(group = "org.scijava", module = "native-lib-loader-sources")
    }

    testImplementation("io.mockk:mockk:1.13.17")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.json:json:20250107") // for testing purposes

    // CORS
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.95.Final")

    implementation("com.networknt:json-schema-validator:1.5.6")
    // EUDI WALLET
    implementation("eu.europa.ec.eudi:eudi-lib-jvm-sdjwt-kt:0.13.1") {
        exclude(group = "com.github.peteroup", module = "datautilities")
    }
}

tasks.named("compileKotlin", KotlinCompilationTask::class.java) {
    compilerOptions {
        // freeCompilerArgs.add("-Xexport-kdoc")
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        with(setOf(PASSED, SKIPPED, FAILED)) {
            events = this
            info.events = this
        }
    }
    useJUnitPlatform()
}

tasks.register("killRedis") {
    group = "build"
    doLast {
        val os = System.getProperty("os.name").lowercase()
        val result = ByteArrayOutputStream()
        if (os.contains("win")) {
            exec {
                commandLine(
                    "cmd",
                    "/c",
                    "for /f \"tokens=5\" %a in ('netstat -ano ^| findstr :6370') do taskkill /PID %a /F"
                )
                standardOutput = result
                errorOutput = result
                isIgnoreExitValue = true
            }
        } else {
            exec {
                commandLine("sh", "-c", "pid=\$(lsof -t -i:6370) && [ -n \"\$pid\" ] && kill -9 \$pid")
                standardOutput = result
                errorOutput = result
                isIgnoreExitValue = true
            }
        }
    }
}

tasks.clean {
    dependsOn("killRedis")
}

// Dynamic Creation of Controller Tests
val baseControllersPackage = "com.eviden.bds.rd.uself.agent.bdd.api"
val controllerTasks = mutableListOf<TaskProvider<Test>>()
fileTree("src/test/kotlin/${baseControllersPackage.replace('.', '/')}").matching {
    include("**/*.kt")
}.forEach { file: File ->
    val className = file.nameWithoutExtension
    val testTask = tasks.register<Test>(className) {
        group = "tests bdd api"
        description = "Run tests for $className"
        filter {
            includeTestsMatching("$baseControllersPackage.$className")
        }
    }
    controllerTasks.add(testTask)
}

// Dynamic Creation of Services Tests
val baseServicesPackage = "com.eviden.bds.rd.uself.agent.bdd.services"
val servicesTasks = mutableListOf<TaskProvider<Test>>()
fileTree("src/test/kotlin/${baseServicesPackage.replace('.', '/')}").matching {
    include("**/*.kt")
}.forEach { file: File ->
    val className = file.nameWithoutExtension
    val testTask = tasks.register<Test>(className) {
        group = "tests bdd services"
        description = "Run tests for $className"
        filter {
            includeTestsMatching("$baseServicesPackage.$className")
        }
    }
    servicesTasks.add(testTask)
}

val testRepositories = tasks.register<Test>("testRepositories") {
    println("Testing the Repositories...")
    group = "tests bdd repositories"
    filter {
        includeTestsMatching("com.eviden.bds.rd.uself.agent.bdd.repositories.*")
    }
}

tasks.test {
    group = "tests"
    dependsOn(servicesTasks, controllerTasks, testRepositories)
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    enabled = false
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    executionData.setFrom(fileTree("build/jacoco").include("*.exec"))
    reports {
        xml.required.set(true)
    }
}

tasks.sonar {
    dependsOn(tasks.jacocoTestReport)
}

docker {
    springBootApplication {
        baseImage.set("openjdk:17-jdk")
        ports.set(listOf(8888))
        images.set(
            setOf(
                "$nexusURL/$registryPath/${rootProject.name}:$version",
            )
        )
    }
    registryCredentials {
        url.set(nexusURL)
        username.set(nexusUsername)
        password.set(nexusPass)
    }
}

tasks.detekt {
    parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config.setFrom("detekt-config.yml")

    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = false

    // Turns on all the rules. `false` by default.
    allRules = false

    // Disables all default detekt rulesets and will only run detekt with custom rules
    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.
    disableDefaultRuleSets = false

    // Adds debug output during task execution. `false` by default.
    debug = false

    // If set to `true` the build does not fail when the
    // maxIssues count was reached. Defaults to `false`.
    ignoreFailures = false

    // Specify the base path for file paths in the formatted reports.
    // If not set, all file paths reported will be absolute file path.
    basePath = projectDir.absolutePath
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

spdxSbom {
    targets {
        // create a target named "release",
        // this is used for the task name (spdxSbomForRelease)
        // and output file (build/spdx/release.spdx.json)
        create("release") {
            // use a different configuration (or multiple configurations)
            // configurations.set(listOf("myCustomConfiguration"))

            // override the default output file
            outputFile.set(layout.buildDirectory.file("uself-spdx.filename"))

            // provide scm info (usually from your CI)
//            scm {
//                uri.set("my-scm-repository")
//                revision.set("asdfasdfasdf...")
//            }

            // adjust properties of the document
            document {
                name.set("uSelf Agent spdx document")
                namespace.set("https://atos.net/spdx/2025")
                creator.set("Atos Spain Research and Innovation")
                // supplier.set("Organization:loosebazooka industries")

                // add an uber package on the document between the document and the
                // root module of the project being analyzed, you probably don't need this
                // but it's available if you want to describe the artifact in a special way
//                uberPackage {
//                    // you must set all or none of these
//                    name.set("goose")
//                    version.set("1.2.3")
//                    supplier.set("Organization:loosebazooka industries")
//                }
            }
            // optionally have multiple targets
            // create("another") {
            // }
        }
    }
}
