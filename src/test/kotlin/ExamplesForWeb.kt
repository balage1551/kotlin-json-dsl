import hu.vissy.jsondsl.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

class ExamplesForWeb {

    fun example_into(needLottery: Boolean = true) {
        val myJson = jsonObject {
            // You can add any type to the json. For numeric, boolean, null and string type
            // the correct mapping to JSON primitive will be applied, for any other type
            // they will map to string using toString
            "meaningOfLife" += 42
            "goodBye" += "And thanks the fish"

            // Making deep structure is easy
            "signatures" += jsonArray {
                +"Don"
                +"Mark"
                +"Liv"
            }

            // Normal code and creation may mingle seamlessly
            if (needLottery) {
                "lottery" += jsonArray {
                    (0..4).forEach { _ ->
                        // Unary plus shorthand can't be used directly on numbers
                        add(Random.nextInt(90) + 1)
                    }
                }
            }
        }
    }

    fun example_rem() {
        val myJson = jsonObject {
            var value: String? = null
            "field" %= value
            println(build())

            value = "test"
            "field" %= value
            println(build())

            val value2: String? = null
            "field" %= value2
            println(build())

            val subObject = jsonObject {}
            val subArray = jsonArray()
            "subObject" /= subObject // The field will be omitted
            "subArray" /= subArray // The field will be omitted
            println(build())

            val num = 42
            "arr" += jsonArray {
                +null
            }
            println(build())
        }
    }

    fun example_hierarchy() {
        val child = ChildClass(42, listOf("This", "is", "Sparta"), mapOf( "val1" to "Alma", "val2" to 1, "val3" to LocalDateTime.now() ))
        println(child.toJson().toPretty())
        val child2 = ChildClass(42, listOf(), mapOf())
        println(child2.toJson().toPretty())
    }

    fun example_getter() {
        val json = """{
              "aNumber" : 42,
              "empty" : null,
              "aBool" : false,
              "aDate" : "2024-01-20"
            }""".fromJson().asJsonObject

        val num : Int = json("aNumber")

        // If you specify default value, the type don't have to be specified explicitly
        val num2 = json("aNumber", 42)
        // It is the same as
        val num3: Int = json("aNumber", optional) ?: 42

        val emptyWrong: String = json("empty")        // Not correct, value can't be null, will throw exception
        val empty: String? = json("empty", optional)  // With optional flag it is correct
        val bool = json.invoke<Boolean>("aBool")      // Type specification on invoke
        val nonExisting: String = json("missing", "not there")
        val date : LocalDate = json("aDate")

    }
 }

open class ParentClass(val num: Int, val strings: List<String?>) {
    open fun toJson() = jsonObject {
        "number" += num
        // Do not export empty array
        "text" /= jsonArray(strings)
    }
}

open class ChildClass(num: Int, strings: List<String?>, val properties: Map<String, Any?>) : ParentClass(num, strings) {

    // Extend the json received from parent
    override fun toJson() = jsonObject(super.toJson()) {
        "data" += jsonArray {
            properties.forEach { (key, value) ->
                +jsonObject {
                    "key" += key
                    "value" += value
                }
            }
        }
    }
}

fun main() {
    ExamplesForWeb().example_getter()
}