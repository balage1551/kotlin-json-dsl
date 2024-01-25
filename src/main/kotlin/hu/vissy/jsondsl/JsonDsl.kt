@file:Suppress("unused")

package hu.vissy.jsondsl

import com.google.gson.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

fun jsonObject(obj: JsonObject = JsonObject(), op: JsonObjectBuilder.() -> Unit) =
    JsonObjectBuilder(obj).apply(op).build()

fun jsonArray(arr: JsonArray = JsonArray(), op: JsonArrayBuilder.() -> Unit) = JsonArrayBuilder(arr).apply(op).build()

fun jsonArray(items: Iterable<Any?>): JsonArray {
    val res = JsonArray()
    items.forEach { addToArray(res, it) }
    return res
}

fun String.fromJson(): JsonElement {
    return JsonParser.parseString(this)
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


val j = jsonObject {
    "alma" += 4
    "citrom" += null
    "3" += "null"
    "o" += jsonObject {
        "b" += true
    }
    "arr" += jsonArray {
        (0..10).forEach { +"Value $it" }
        +"alma"
        +jsonObject { }
    }
    "a2" += jsonArray(1, null, "lama")
}

fun JsonElement.toPretty() =
    GsonBuilder().setPrettyPrinting().create().toJson(this)
        .replace("\n".toRegex(), "ß") // Replace newlines to allow over-the-line search
        .replace("\",ß( *)\"".toRegex(), "\",$1\"") // Replace consecutive string elements to be in one line
        .replace(",( *)(\"[^\"]*\"):".toRegex(), ",ß$1$2:") // For object keys, the newline marker is restores
        .replace(", *\"".toRegex(), ", \"") // Remove unnecessary spaces
        .replace(" {2}".toRegex(), "    ") // Indent 4, instead of 2
        .replace("ß".toRegex(), "\n")


inline operator fun <reified T : Any> JsonObject.invoke(key: String) = this.internalGetOrDefault(T::class, key)
    ?: error("Json field '$key' should not be null.")

inline operator fun <reified T : Any> JsonObject.invoke(key: String, default: T) =
    this.internalGetOrDefault(T::class, key, default)
        ?: default

@Suppress("ClassName")
object optional

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
        JsonObject::class -> v.asJsonObject
        JsonArray::class -> v.asJsonArray
        else -> {
            error("Unsupported type: ${clazz.simpleName} ")
        }
    } as T
}

fun JsonElement.strictPath(path: String): JsonElement = this.path(path, true)!!

fun JsonElement.path(path: String, strict: Boolean = false): JsonElement? {

    var p = path
    var j = this
    var inArray = false
    var v = ""

    fun extract(trim: Int, vararg endOfToken: String) {
        p = p.substring(trim)
        var i = endOfToken.map { p.indexOf(it) }.filter { it != -1 }.minOrNull() ?: -1
        if (i == -1) {
            if (endOfToken.contains("EOP")) i = p.length
            else throw IllegalArgumentException("End of token not found at ${path.length - p.length} : $path")
        }
        v = p.substring(0, i)
        p = p.substring(i)
    }

    while (p.isNotBlank()) {
        var skip = false
        when {
            p.startsWith("[") -> {
                if (inArray) throw IllegalArgumentException("Nested indexes at ${path.length - p.length} : $path")
                inArray = true
                extract(1, ",", "]")
            }

            inArray && p.startsWith(",") -> {
                extract(1, ",", "]")
            }

            inArray && p.startsWith("]") -> {
                p = p.substring(1)
                inArray = false
                skip = true
            }

            !inArray && p.startsWith(".") -> {
                p = p.substring(1)
                skip = true
            }

            else -> {
                extract(0, ".", "[", "EOP")
            }
        }
        if (skip) continue

        if (inArray) {
            if (j.isJsonArray) {
                val i = v.toIntOrNull()
                    ?: throw IllegalArgumentException("Index ($v) should be a number at ${path.length - p.length} : $path")
                j = j.asJsonArray[i]
            } else {
                if (!strict) return null else throw IllegalArgumentException("Array index on non-array element at ${path.length - p.length} : $path")
            }
        } else {
            if (j.isJsonObject) {
                val o = j.asJsonObject
                if (o.has(v))
                    j = j.asJsonObject[v]
                else
                    if (!strict) return null else throw IllegalArgumentException("Element '$v' not found at ${path.length - p.length} : $path")
            } else {
                if (!strict) return null else throw IllegalArgumentException("Element is not an object at ${path.length - p.length} : $path")
            }
        }

//        println("$inArray -> $v  ($p)  -> $j")
    }
    return j
}


class JsonMapper<T : Any>(
    private val json: JsonObject,
    private val default: T? = null,
    private val prefix: String? = null
) : ReadOnlyProperty<Any?, T> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val field = property.name
        val propertyType = property.returnType

        val path = if (prefix == null) field else "$prefix.$field"
        val e =
            json.path(path) ?: (return default ?: throw IllegalArgumentException("Property missing: $path in $json"))
        @Suppress("UNCHECKED_CAST")
        return when {
            propertyType.isSubtypeOf(typeOf<Int>()) -> e.asInt as T
            propertyType.isSubtypeOf(typeOf<Long>()) -> e.asLong as T
            propertyType.isSubtypeOf(typeOf<Double>()) -> e.asDouble as T
            propertyType.isSubtypeOf(typeOf<Boolean>()) -> e.asBoolean as T
            propertyType.isSubtypeOf(typeOf<String>()) -> e.asString as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

//    companion object {
//        operator fun invoke(clazz: Class<*>): Logger = invoke(clazz.canonicalName)
//
//        operator fun invoke(name: String): Logger {
//            return LogManager.getLogger(name)
//                ?: throw IllegalArgumentException("Can't create logger for $name")
//        }
//
//    }

}

