@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

/**
 * Creates a [JsonObject] with content defined in the body.
 *
 * If [obj] is passed, the function extends [obj] instead of creating a new object.
 *
 * The [op] receives the [JsonObjectBuilder] wrapper of the target as `this`.
 *
 * Example:
 *
 * ```kotlin
 * val newUser = jsonObject {
 *    "name" += "John Doe"
 *    "age" += 42
 * }
 * ```
 *
 * @param obj Optional object to use as target. If omitted, a new [JsonObject] will be
 *            created.
 * @param op  The operation performed on a [JsonObjectBuilder] wrapper of the target.
 */
fun jsonObject(obj: JsonObject = JsonObject(), op: JsonObjectBuilder.() -> Unit) =
    JsonObjectBuilder(obj).apply(op).build()


/**
 * A wrapper of a [JsonObject] used in the operation of [jsonObject]. The class contains
 * useful utility functions and operators for populating the object with fields.
 *
 * *Note: This class is typically not used directly, but through the function [jsonObject].*
 */
class JsonObjectBuilder(private val obj: JsonObject = JsonObject()) {

    /**
     * Sets the value of a property as integer on the target object.
     *
     * @receiver The name of the field.
     * @param value A non-null integer value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: Int?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.addProperty(this, value)

    /**
     * Sets the value of a property as string on the target object, but only if the [value]
     * is not null.
     * If the  [value] is null, this function doesn't set the value and removes the key if it
     * has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable integer value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: Int?) {
        if (value != null) obj.addProperty(this, value)
        else remove(this)
    }

    /**
     * Sets the value of a property as long on the target object.
     *
     * @receiver The name of the field.
     * @param value A non-null long value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: Long?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.addProperty(this, value)

    /**
     * Sets the value of a property as string on the target object, but only if the [value]
     * is not null.
     * If the  [value] is null, this function doesn't set the value and removes the key if it
     * has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable long value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: Long?) {
        if (value != null) obj.addProperty(this, value)
        else remove(this)
    }

    /**
     * Sets the value of a property as floating-point on the target object.
     *
     * @receiver The name of the field.
     * @param value A non-null double value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: Double?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.addProperty(this, value)

    /**
     * Sets the value of a property as string on the target object, but only if the [value]
     * is not null.
     * If the  [value] is null, this function doesn't set the value and removes the key if it
     * has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable double value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: Double?) {
        if (value != null) obj.addProperty(this, value)
    }

    /**
     * Sets the value of a property as boolean on the target object.
     *
     * @receiver The name of the field.
     * @param value A non-null boolean value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: Boolean?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.addProperty(this, value)

    /**
     * Sets the value of a property as string on the target object, but only if the [value]
     * is not null.
     * If the  [value] is null, this function doesn't set the value and removes the key if it
     * has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable boolean value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: Boolean?) {
        if (value != null) obj.addProperty(this, value)
        else remove(this)
    }

    /**
     * Sets the value of a property as string on the target object.
     *
     * @receiver The name of the field.
     * @param value A non null string value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: String?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.addProperty(this, value)

    /**
     * Sets the value of a property as string on the target object, but only if the [value]
     * is not null.
     * If the  [value] is null, this function doesn't set the value and removes the key if it
     * has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable string value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: String?) {
        if (value != null) obj.addProperty(this, value)
        else remove(this)
    }

    /**
     * Sets the value of a property using a [JsonElement] on the target object.
     *
     * @receiver The name of the field.
     * @param value A non null JsonElement value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: JsonElement?) = if (value == null) obj.add(this, JsonNull.INSTANCE) else obj.add(this, value)

    /**
     * Sets the value of a property from an optionally pre-created [JsonElement] on the
     * target object, but only if the value is not null.
     *
     * @receiver The name of the field.
     * @param value A nullable [JsonElement] value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: JsonElement?) {
        if (value != null) obj.add(this, value)
        else remove(this)
    }

    /**
     * Sets the value of a field on the target object, using the `toString` of value.
     * This function sets the value even if it is null.
     *
     * @receiver The name of the field.
     * @param value A nullable value to set.
     * @see [remAssign]
     */
    infix operator fun String.plusAssign(value: Any?) =
        if (value == null)
            obj.add(this, JsonNull.INSTANCE)
        else obj.addProperty(this, value.toString())

    /**
     * Sets the value of a field on the target object, using the `toString` of value.
     * If the  [value] is null, this function doesn't set the value and removes the key if it has already existed.
     *
     * @receiver The name of the field.
     * @param value A nullable [JsonElement] value to set.
     * @see [plusAssign]
     */
    infix operator fun String.remAssign(value: Any?) {
        if (value != null) obj.addProperty(this, value.toString())
        else remove(this)
    }

    /**
     * Adds an optional [JsonElement] to the target, but only when the value fulfills
     * all the following predicates:
     *
     * - the [value] is not null
     * - if the [value] is a [JsonObject], it has at least one field
     * - if the [value] is a [JsonArray], it has at least one item
     *
     * @receiver The name of the field.
     * @param value A nullable [JsonElement] value to set.
     */
    infix operator fun String.divAssign(value: JsonElement?) {
        remove(this)
        if (value == null) return
        if (value.isJsonObject && value.asJsonObject.keySet().isEmpty()) return
        if (value.isJsonArray && value.asJsonArray.size() == 0) return
        obj.add(this, value)
    }

    /**
     * Adds a subobject to the target.
     *
     * @receiver The name of the field.
     * @op An operation for populating the new object.
     */
    operator fun String.invoke(op: (JsonObjectBuilder) -> Unit) = obj.add(this, JsonObjectBuilder().apply(op).build())

    /**
     * Removes a key from the target.
     *
     * @receiver The key to remove.
     */
    operator fun String.unaryMinus() = remove(this)

    /**
     * Adds or extends an object on the target.
     *
     * @param key The key of the object to extend. The key should be missing from the target or
     * should point to a [JsonObject].
     * @param op The operation to populate the object referred by [key].
     * @throws IllegalArgumentException If [key] doesn't point to a [JsonObject].
     */
    fun extendObject(key: String, op: JsonObjectBuilder.() -> Unit) {
        if (obj[key] == null) key(op)
        else {
            when (val e = obj[key]) {
                is JsonObject -> JsonObjectBuilder(e).apply(op)
                else -> throw IllegalStateException("Can't extend ${e::class.simpleName} of '$key'")
            }
        }
    }

    /**
     * Adds or extends an array on the target.
     *
     * @param key The key of the array to extend. The key should be missing from the target or
     * should point to a [JsonArray].
     * @param op The operation to populate the array referred by [key].
     * @throws IllegalArgumentException If [key] doesn't point to a [JsonArray].
     */
    fun extendArray(key: String, op: JsonArrayBuilder.() -> Unit) {
        if (obj[key] == null) obj.add(key, JsonArrayBuilder().apply(op).build())
        else {
            when (val e = obj[key]) {
                is JsonArray -> JsonArrayBuilder(e).apply(op)
                else -> throw IllegalStateException("Can't extend ${e::class.simpleName} of '$key'")
            }
        }
    }

    /**
     * Removes a key.
     *
     * @param key The key to remove.
     * @see unaryMinus
     */
    fun remove(key: String) {
        if (obj.has(key)) obj.remove(key)
    }

    /**
     * Returns the target.
     *
     * Usually this function should not be called manually. It is called by [jsonObject] function.
     *
     * *Note: This function doesn't return a copy of the target, but the target itself.*
     */
    fun build() = obj
}
