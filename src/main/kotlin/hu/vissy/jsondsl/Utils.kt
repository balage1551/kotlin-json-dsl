@file:Suppress("MemberVisibilityCanBePrivate")

package hu.vissy.jsondsl

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser

@Suppress("ClassName")
object optional

fun String.fromJson(): JsonElement {
    return JsonParser.parseString(this)
}


fun JsonElement.toPretty() =
    GsonBuilder().setPrettyPrinting().create().toJson(this)
        .replace("\n".toRegex(), "ß") // Replace newlines to allow over-the-line search
        .replace("\",ß( *)\"".toRegex(), "\",$1\"") // Replace consecutive string elements to be in one line
        .replace(",( *)(\"[^\"]*\"):".toRegex(), ",ß$1$2:") // For object keys, the newline marker is restores
        .replace(", *\"".toRegex(), ", \"") // Remove unnecessary spaces
        .replace(" {2}".toRegex(), "    ") // Indent 4, instead of 2
        .replace("ß".toRegex(), "\n")

