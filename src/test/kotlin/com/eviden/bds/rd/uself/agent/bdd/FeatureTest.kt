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

package com.eviden.bds.rd.uself.agent.bdd

import com.eviden.bds.rd.uself.common.services.Utils.hashStringBase64
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FeatureTest : BehaviorSpec({

    var username = "test1"
    var password = "Pass1d23"
    var result = false

//    Context: User enters valid credentials
//    Given: the user is on a page with the login form
//      And: the user has entered the username “test1”
//      And: the user has entered the password “Pass123”
//    When: he clicks login
//    Then: the user is taken to his homepage
    Context("User enters valid credentials") {
        Given("the user is on a page with the login form") {
            And("the user has entered the username 'test1'") {
                // Enter the username 'test1'
                username = "test1"
                val test2 = hashStringBase64("did:ebsi:1234")
                val test3 = hashStringBase64("did:ebsi:1234")
                println("valor ${test2 == test3}")
            }

            And("the user has entered the password 'Pass123'") {
                // Enter the password 'Pass123'
                password = "Pass1d23"
            }

            When("he clicks login") {
                // Click the login button
                result = login(username, password)
            }

            Then("the user is taken to his homepage") {
                // Check that the user is on his homepage
                // This is a placeholder, replace with actual implementation

                result shouldBe true
            }
        }

        Given("the user is on a page with the login form with wrong info") {
            And("the user has entered the username 'test1'") {
                // Enter the username 'test1'
                username = "test1"
            }

            And("the user has entered the password 'Pass123'") {
                // Enter the password 'Pass123'
                password = "Pass1d23"
            }

            When("he clicks login") {
                // Click the login button
                result = login(username, password)
            }

            Then("the user is taken to his homepage") {
                // Check that the user is on his homepage
                // This is a placeholder, replace with actual implementation

                result shouldBe true
            }
        }
    }

//    Context: User forgets their password
//    Given: the user is on a page with the login fields
//      And: the user has entered the username “test1”
//    When: he clicks the “Forgot password” link
//    Then: the user is taken to the reset password paged

    Context("User forgets their password") {
        Given("the user is on a page with the login fields") {
            And("the user has entered the username “test1”") {
                // Enter the username 'test1'
                username = "test1"
            }

            When("he clicks the “Forgot password” link") {
                // Click the login button
                result = login(username, password)
            }

            Then("the user is taken to the reset password paged") {
                // Check that the user is on his homepage
                // This is a placeholder, replace with actual implementation

                result shouldBe true
            }
        }
    }
})

fun login(username: String, password: String): Boolean {
    // This is a placeholder, replace with actual implementation
    println("Username: $username and password: $password")
    return true
}
