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
@file:Suppress("MagicNumber", "MaxLineLength")

package com.eviden.bds.rd.uself.agent.models.entities.keys

import com.eviden.bds.rd.uself.common.services.crypto.CryptoService
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.koin.java.KoinJavaComponent.inject
import org.springframework.beans.factory.annotation.Value
import java.util.Base64

@Converter
class KeysEncryption : AttributeConverter<String, String> {

    // Injects the CryptoService dependency using Koin
    private val cryptoService by inject<CryptoService>(CryptoService::class.java)

    // Injects the master key from the application properties
    @Value("\${master.key.kms}")
    private lateinit var key: String

    /**
     * Initializes the secret key. If the key is empty, a new key is generated.
     * Otherwise, the key is decoded from Base64 and used to create a SecretKeySpec.
     */
    private val secretKey by lazy {
        if (key.isEmpty()) {
            cryptoService.createSymmetricKey()
        } else {
            val decodedKey = Base64.getDecoder().decode(key)
            javax.crypto.spec.SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        }
    }

    /**
     * Converts the entity attribute to a database column representation.
     * Encrypts the attribute using AES-GCM with a generated IV and the secret key.
     *
     * @param attribute The entity attribute to convert.
     * @return The encrypted attribute as a Base64 encoded string.
     */
    override fun convertToDatabaseColumn(attribute: String?): String {
        return attribute?.let {
            val iv = cryptoService.generateIv()
            val encryptedData = cryptoService.encrypt(it, secretKey, iv)
            Base64.getEncoder().encodeToString(iv + encryptedData)
        } ?: ""
    }

    /**
     * Converts the database column representation back to the entity attribute.
     * Decrypts the data using AES-GCM with the stored IV and the secret key.
     *
     * @param dbData The database column data to convert.
     * @return The decrypted entity attribute.
     */
    override fun convertToEntityAttribute(dbData: String?): String {
        return dbData?.let {
            val decodedData = Base64.getDecoder().decode(it)
            val iv = decodedData.copyOfRange(0, 12)
            val encryptedData = decodedData.copyOfRange(12, decodedData.size)

            cryptoService.decrypt(encryptedData, secretKey, iv)
        } ?: ""
    }
}
