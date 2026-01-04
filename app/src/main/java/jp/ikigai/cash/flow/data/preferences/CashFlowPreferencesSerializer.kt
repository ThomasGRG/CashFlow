/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class CashFlowPreferencesSerializer : Serializer<CashFlowPreferences> {
    override val defaultValue: CashFlowPreferences
        get() = CashFlowPreferences()

    override suspend fun readFrom(input: InputStream): CashFlowPreferences {
        try {
            return Json.decodeFromString(
                deserializer = CashFlowPreferences.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Unable to read preferences", exception)
        }
    }

    override suspend fun writeTo(t: CashFlowPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json
                    .encodeToString(
                        serializer = CashFlowPreferences.serializer(),
                        value = t
                    )
                    .encodeToByteArray()
            )
        }
    }
}