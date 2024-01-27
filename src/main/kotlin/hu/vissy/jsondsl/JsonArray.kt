@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.*

/**
 * Creates a [JsonArray] with content defined in the body.
 *
 * If [arr] is passed, the function extends [arr] instead of creating a new array.
 *
 * The [op] receives the [JsonArrayBuilder] wrapper of the target as `this`.
 *
 * Example:
 *
 * ```kotlin
 * val newUser = jsonArray {
 *    +"John Doe"
 *    +true
 * }
 * ```
 *
 * @param arr Optional array to use as target. If omitted, a new [JsonArray] will be
 *            created.
 * @param op  The operation performed on a [JsonArrayBuilder] wrapper of the target.
 * @return The array containing the items.
 */
fun jsonArray(arr: JsonArray = JsonArray(), op: JsonArrayBuilder.() -> Unit) = JsonArrayBuilder(arr).apply(op).build()

/**
 * Adds all items in the [items] iterable to a new list.
 *
 * @param items The items to add.
 * @return The array containing the items.
 */
fun jsonArray(items: Iterable<Any?>) = jsonArray( JsonArray(), items)


/**
 * Adds all items in the [items] iterable to the list.
 *
 * @param arr Optional array to use as target. If omitted, a new [JsonArray] will be
 *            created.
 * @param items The items to add.
 * @return The array containing the items.
 */
fun jsonArray(arr: JsonArray = JsonArray(), items: Iterable<Any?>): JsonArray {
    items.forEach { addToArray(arr, it) }
    return arr
}

/**
 * Adds all arguments to a new the list.
 *
 * @param items The items to add.
 * @return The array containing the items.
 */
fun jsonArray(vararg items: Any?) = jsonArray(JsonArray(), *items)

/**
 * Adds all arguments to the list.
 *
 * @param arr Optional array to use as target. If omitted, a new [JsonArray] will be
 *            created.
 * @param items The items to add.
 * @return The array containing the items.
 */
fun jsonArray(arr: JsonArray = JsonArray(), vararg items: Any?): JsonArray {
    items.forEach { addToArray(arr, it) }
    return arr
}

private fun addToArray(res: JsonArray, it: Any?) {
    if (it == null) res.add(JsonNull.INSTANCE)
    else when (it) {
        is String -> res.add(JsonPrimitive(it))
        is Number -> res.add(JsonPrimitive(it))
        is Boolean -> res.add(JsonPrimitive(it))
        is JsonObject -> res.add(it)
        is JsonArray -> res.add(it)
        else -> res.add(JsonPrimitive(it.toString()))
    }
}

/**
 * A wrapper of a [JsonArray] used in the operation of [jsonArray]. The class contains
 * useful utility functions and operators for populating the object with data.
 *
 * *Note: This class is typically not used directly, but through the function [jsonArray].*
 */
class JsonArrayBuilder(private val arr: JsonArray = JsonArray()) {

    /**
     * Adds a number to the array.
     *
     * **Note that the [unaryPlus] operator could not be used to add number constants to the
     * list. Use this method on numbers.**
     *
     * @param other The number to add.
     */
    fun add(other: Number) = arr.add(JsonPrimitive(other))

    /**
     * Adds a boolean to the list.
     *
     * @receiver The value to add.
     */
    operator fun Boolean.unaryPlus() = arr.add(JsonPrimitive(this))

    /**
     * Adds a string to the list.
     *
     * @receiver The value to add.
     */
    operator fun String.unaryPlus() = arr.add(JsonPrimitive(this))

    /**
     * Adds a null value to the list.
     */
    @Suppress("MemberVisibilityCanBePrivate", "FunctionName")
    @Deprecated("Use addNull or +null instead.", replaceWith = ReplaceWith("addNull"))
    fun NULL() = arr.add(JsonNull.INSTANCE)

    /**
     * Adds a null value to the list.
     */
    fun addNull() = arr.add(JsonNull.INSTANCE)

    /**
     * Adds another [JsonElement] to the list.
     *
     * @receiver The element to add.
     */
    operator fun JsonElement.unaryPlus() = arr.add(this)

    /**
     * Adds an arbitrary value to the list using the `toString` of the receiver.
     *
     * @receiver The value to add.
     */
    operator fun Any?.unaryPlus() = if (this == null) NULL() else arr.add(JsonPrimitive(this.toString()))

    /**
     * Returns the target.
     *
     * Usually this function should not be called manually. It is called by [jsonArray] function.
     *
     * *Note: This function doesn't return a copy of the target, but the target itself.*
     */
    fun build() = arr
}
