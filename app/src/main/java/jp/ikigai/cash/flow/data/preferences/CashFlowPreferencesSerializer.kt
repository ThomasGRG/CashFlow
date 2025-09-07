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