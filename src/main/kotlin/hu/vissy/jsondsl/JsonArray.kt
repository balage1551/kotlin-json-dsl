@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.*

fun jsonArray(arr: JsonArray = JsonArray(), op: JsonArrayBuilder.() -> Unit) = JsonArrayBuilder(arr).apply(op).build()

fun jsonArray(items: Iterable<Any?>): JsonArray {
    val res = JsonArray()
    items.forEach { addToArray(res, it) }
    return res
}


fun jsonArray(vararg items: Any?): JsonArray {
    val res = JsonArray()
    items.forEach { addToArray(res, it) }
    return res
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

class JsonArrayBuilder(private val arr: JsonArray = JsonArray()) {

    fun add(other: Number) = arr.add(JsonPrimitive(other))

    operator fun Boolean.unaryPlus() = arr.add(JsonPrimitive(this))
    operator fun String.unaryPlus() = arr.add(JsonPrimitive(this))

    @Suppress("MemberVisibilityCanBePrivate", "FunctionName")
    fun NULL() = arr.add(JsonNull.INSTANCE)
    operator fun JsonElement.unaryPlus() = arr.add(this)
    operator fun Any?.unaryPlus() = if (this == null) NULL() else arr.add(JsonPrimitive(this.toString()))

    fun build() = arr
}
