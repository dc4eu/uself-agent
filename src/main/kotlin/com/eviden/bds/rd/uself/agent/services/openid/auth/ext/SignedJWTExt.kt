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
@file:Suppress("TooManyFunctions", "UNCHECKED_CAST")

package com.eviden.bds.rd.uself.agent.services.openid.auth.ext

import com.danubetech.verifiablecredentials.credentialstatus.StatusList2021Entry
import com.danubetech.verifiablecredentials.jwt.FromJwtConverter
import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential
import com.danubetech.verifiablecredentials.jwt.JwtVerifiablePresentation
import com.danubetech.verifiablecredentials.validation.Validation
import com.eviden.bds.rd.uself.common.models.exceptions.ExceptionValidating
import com.eviden.bds.rd.uself.common.services.status.StatusList2021EntryExt.validate
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object SignedJWTExt {
    fun SignedJWT.getHeaderDID(): String {
        return this.header.toJSONObject()["kid"].toString().split("#").first()
    }

    fun String.parseJWT(): Pair<String, SignedJWT> {
        val jwt = SignedJWT.parse(this)
        return Pair(jwt.getHeaderDID(), jwt)
    }

    fun SignedJWT.validateCredential() {
        validatePeriod()
        // For the moment we don't verify the status
        // validateStatus()
    }

    fun SignedJWT.validatePresentation() {
        // validatePeriod()

        // Validate the VC included in the VP
        val presentation = this.payload.toJSONObject()["vp"] as Map<*, *>
        val credentials = presentation["verifiableCredential"] as ArrayList<*>
        credentials.forEach {
            val jwtVc = SignedJWT.parse(it.toString())
            jwtVc.validateCredential()
        }
    }

    fun SignedJWT.validatePeriod() {
        this.validateExp()
        this.validateIat()
        this.validateNbf()
    }

    fun SignedJWT.validateExp() {
        val expLong = this.payload.toJSONObject()["exp"] as Long
        val expDate = Date(TimeUnit.SECONDS.toMillis(expLong))
        if (!expDate.after(Date.from(Instant.now()))) {
            throw ExceptionValidating("\"<any id, random or static>\" is expired")
        }
    }

    fun SignedJWT.validateIat() {
        val iat = this.payload.toJSONObject()["iat"] as Long
        if (iat > Date.from(Instant.now()).time) {
            throw ExceptionValidating(
                "\"<any id, random or static>\" is not yet valid"
            )
        }
    }

    fun SignedJWT.validateNbf() {
        val nbf = (this.payload.toJSONObject()["nbf"]) as Long
        val nbfDate = Date(TimeUnit.SECONDS.toMillis(nbf))
        if (!nbfDate.before(
                Date.from(Instant.now())
            )
        ) {
            throw ExceptionValidating("\"<any id, random or static>\" is not yet valid")
        }
    }

    fun SignedJWT.validateJti() {
        val jti = this.payload.toJSONObject()["jti"]
        val id = (this.payload.toJSONObject()["vp"] as LinkedHashMap<*, *>)["id"]
        if (jti == id) throw ExceptionValidating("")
    }

    fun SignedJWT.validateHolder() {
        val iss = this.payload.toJSONObject()["iss"]
        val id = (this.payload.toJSONObject()["vp"] as LinkedHashMap<*, *>)["holder"]

        if (iss == id) throw ExceptionValidating("")
    }

    fun SignedJWT.validateIssuer() {
        val iss = this.payload.toJSONObject()["iss"]
        val id = (this.payload.toJSONObject()["vc"] as LinkedHashMap<*, *>)["issuer"]
        if (iss == id) throw ExceptionValidating("")
    }

    fun SignedJWT.validateStatus() {
        val vc = this.payload.toJSONObject()["vc"]
        if (vc != null) {
            val status = (vc as MutableMap<String, Any>)["credentialStatus"]
            if (status != null) {
                val credentialStatus = StatusList2021Entry.fromMap(status as MutableMap<String, Any>)
                credentialStatus.validate()
            }
        }
    }

    fun SignedJWT.other(vpString: String) {
        val jwtVerPresentation = JwtVerifiablePresentation.fromCompactSerialization(vpString)

        val vp = FromJwtConverter.fromJwtVerifiablePresentation(jwtVerPresentation)
        Validation.validate(vp)

        val jwtVerCredentialString = vp.jwtVerifiableCredentialString
        // val jwtVp2 = SignedJWT.parse(jwtVerCredentialString)
        // jwtService.verifyDID(did, jwtVp2)

        val jwtVerCredential = JwtVerifiableCredential.fromCompactSerialization(jwtVerCredentialString)
        val vc = FromJwtConverter.fromJwtVerifiableCredential(jwtVerCredential)
        Validation.validate(vc)
    }
}
