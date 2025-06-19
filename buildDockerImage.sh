#!/bin/bash

#
# Copyright (c) 2025 Atos Spain S.A. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Exit immediately if a command exits with a non-zero status
set -e

# Define the version (default to 'test' if not provided)
VERSION=${1:-test}

# Clean the project and generate the boot JAR
./gradlew clean bootJar

# Create the build/dependency directory
mkdir -p build/dependency

# Extract the JAR file into the build/dependency directory
(cd build/dependency; jar -xf ../libs/*.jar)

# Build the Docker image with the version argument
docker buildx build -f DockerfileProd --platform linux/amd64,linux/arm64 --push -t ossdc4eu.urv.cat:8081/eviden/rd/uself/uself-agent:${VERSION}  .
