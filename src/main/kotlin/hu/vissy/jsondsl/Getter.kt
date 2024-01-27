@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

/**
 * Returns a typed, mandatory value from the object based of the type [T].
 *
 * See documentation for supported types!
 *
 * @receiver The [JsonObject] to get the value from.
 * @param T  the type to cast the value to.
 * @param key the key of the field.
 * @throws IllegalArgumentException If the key is not exists or the value is null.
 */
inline operator fun <reified T : Any> JsonObject.invoke(key: String) = this.internalGetOrDefault(T::class, key)
    ?: error("Json field '$key' should not be null.")

/**
 * Returns a typed value from the object based of the type [T]. If the [key] doesn't exist in the
 * target, the [default] value is returned.
 *
 * See documentation for supported types!
 *
 * @receiver The [JsonObject] to get the value from.
 * @param T  the type to cast the value to.
 * @param key the key of the field.
 * @param default the default value returned when the field doesn't exist.
 */
inline operator fun <reified T : Any> JsonObject.invoke(key: String, default: T) =
    this.internalGetOrDefault(T::class, key, default)
        ?: default

@Suppress("UNUSED_PARAMETER")
/**
 * Returns a typed value from the object based of the type [T]. If the [key] doesn't exist in the
 * target, `null` is returned.
 *
 * See documentation for supported types!
 *
 * @receiver The [JsonObject] to get the value from.
 * @param T  the type to cast the value to.
 * @param key the key of the field.
 * @param optional a marker to mark the key optional.
 */
inline operator fun <reified T : Any> JsonObject.invoke(key: String, optional: optional) =
    this.internalGetOrDefault(T::class, key)


/**
 * **NOTE: THIS IS AN INTERNAL FUNCTION AND NOT PART OF THE API.**
 */
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
        LocalDate::class -> LocalDate.from(DateTimeFormatter.ISO_DATE.parse(v.asString))
        LocalTime::class -> LocalTime.from(DateTimeFormatter.ISO_TIME.parse(v.asString))
        LocalDateTime::class -> LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(v.asString))
        JsonObject::class -> v.asJsonObject
        JsonArray::class -> v.asJsonArray
        else -> {
            error("Unsupported type: ${clazz.simpleName} ")
        }
    } as T
}
