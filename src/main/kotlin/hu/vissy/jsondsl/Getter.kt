@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

inline operator fun <reified T : Any> JsonObject.invoke(key: String) = this.internalGetOrDefault(T::class, key)
    ?: error("Json field '$key' should not be null.")

inline operator fun <reified T : Any> JsonObject.invoke(key: String, default: T) =
    this.internalGetOrDefault(T::class, key, default)
        ?: default

@Suppress("UNUSED_PARAMETER")
inline operator fun <reified T : Any> JsonObject.invoke(key: String, optional: optional) =
    this.internalGetOrDefault(T::class, key)


@Suppress("UNCHECKED_CAST")
fun <T : Any> JsonObject.internalGetOrDefault(clazz: KClass<T>, key: String, default: T? = null): T? {
    val v = this[key]
    if (v == null || v.isJsonNull) return default
    @Suppress("IMPLICIT_CAST_TO_ANY")
    return when (clazz) {
        Boolean::class -> v.asBoolean
        Int::class -> v.asInt
        Long::class -> v.asLong
        Double::class -> v.asDouble
        String::class -> v.asString
        LocalDate::class -> DateTimeFormatter.ISO_DATE.parse(v.asString)
        LocalTime::class -> DateTimeFormatter.ISO_DATE_TIME.parse(v.asString)
        LocalDateTime::class -> DateTimeFormatter.ISO_TIME.parse(v.asString)
        JsonObject::class -> v.asJsonObject
        JsonArray::class -> v.asJsonArray
        else -> {
            error("Unsupported type: ${clazz.simpleName} ")
        }
    } as T
}
