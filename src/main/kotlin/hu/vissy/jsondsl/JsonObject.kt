@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

fun jsonObject(obj: JsonObject = JsonObject(), op: JsonObjectBuilder.() -> Unit) =
    JsonObjectBuilder(obj).apply(op).build()



class JsonObjectBuilder(private val obj: JsonObject = JsonObject()) {

    infix operator fun String.plusAssign(value: Int) = obj.addProperty(this, value)
    infix operator fun String.remAssign(value: Int?) {
        if (value != null) obj.addProperty(this, value)
    }

    infix operator fun String.plusAssign(value: Long) = obj.addProperty(this, value)
    infix operator fun String.remAssign(value: Long?) {
        if (value != null) obj.addProperty(this, value)
    }

    infix operator fun String.plusAssign(value: Double) = obj.addProperty(this, value)
    infix operator fun String.remAssign(value: Double?) {
        if (value != null) obj.addProperty(this, value)
    }

    infix operator fun String.plusAssign(value: Boolean) = obj.addProperty(this, value)
    infix operator fun String.remAssign(value: Boolean?) {
        if (value != null) obj.addProperty(this, value)
    }

    infix operator fun String.plusAssign(value: String) = obj.addProperty(this, value)
    infix operator fun String.remAssign(value: String?) {
        if (value != null) obj.addProperty(this, value)
    }

    infix operator fun String.plusAssign(value: JsonElement) = obj.add(this, value)
    infix operator fun String.remAssign(value: JsonElement?) {
        if (value != null) obj.add(this, value)
    }

    infix operator fun String.divAssign(value: JsonElement?) {
        if (value == null) return
        if (value.isJsonObject && value.asJsonObject.keySet().isEmpty()) return
        if (value.isJsonArray && value.asJsonArray.size() == 0) return
        obj.add(this, value)
    }

    infix operator fun String.plusAssign(value: Any?) =
        if (value == null)
            obj.add(this, JsonNull.INSTANCE)
        else obj.addProperty(this, value.toString())

    infix operator fun String.remAssign(value: Any?) {
        if (value != null) obj.addProperty(this, value.toString())
    }

    operator fun String.invoke(op: (JsonObjectBuilder) -> Unit) = obj.add(this, JsonObjectBuilder().apply(op).build())

    operator fun String.unaryMinus() = remove(this)

    fun extendObject(key: String, op: JsonObjectBuilder.() -> Unit) {
        if (obj[key] == null) key(op)
        else {
            when (val e = obj[key]) {
                is JsonObject -> JsonObjectBuilder(e).apply(op)
                else -> throw IllegalStateException("Can't extend ${e::class.simpleName} of '$key'")
            }
        }
    }

    fun extendArray(key: String, op: JsonArrayBuilder.() -> Unit) {
        if (obj[key] == null) obj.add(key, JsonArrayBuilder().apply(op).build())
        else {
            when (val e = obj[key]) {
                is JsonArray -> JsonArrayBuilder(e).apply(op)
                else -> throw IllegalStateException("Can't extend ${e::class.simpleName} of '$key'")
            }
        }
    }

    fun remove(key: String) {
        if (obj.has(key)) obj.remove(key)
    }

    fun build() = obj
}
