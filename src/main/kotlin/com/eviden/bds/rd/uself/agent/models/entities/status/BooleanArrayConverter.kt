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

package com.eviden.bds.rd.uself.agent.models.entities.status

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class BooleanArrayConverter : AttributeConverter<BooleanArray,String> {

    override fun convertToDatabaseColumn(attribute: BooleanArray?): String {
        return attribute?.joinToString("") { if (it) "1" else "0" } ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): BooleanArray {
        return dbData?.map { it == '1' }?.toBooleanArray() ?: BooleanArray(0)
    }
}
