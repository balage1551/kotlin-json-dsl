@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.JsonElement

/**
 * Finds a value in a [JsonElement] using a path expression.
 *
 * @receiver the element to search.
 * @param path The path expression to use. See [path] for details.
 * @return The element found.
 * @throws IllegalArgumentException If the path doesn't correct or if there is no item at the
 * position defined by the path.
 */
fun JsonElement.strictPath(path: String): JsonElement = this.path(path, true)
    ?: error("Path '$path' doesn't exist.")

/**
 * Finds a value in a [JsonElement] using a path expression.
 *
 * The possible path expressions:
 *
 *
 * `.<key>` - references the field
 *
 * `[<index>]` - reference in an array
 *
 * `[<index>,<index>...]` - references in nested arrays
 *
 * @receiver the element to search.
 * @param path The path expression to use.
 * @return The element found.
 * @throws IllegalArgumentException If the path syntax doesn't correct.
 */
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
    }
    return j
}
