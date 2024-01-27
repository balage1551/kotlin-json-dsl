# kotlin-json-dsl

Creating JSON in a Kotlin DSL way.

[TOC]

## Installation

For detailed syntax of including `hu.vissy.kotlin-json-dsl` in your project 
based on your preferred build tool, see [Maven Central (TBD)]().

For including it in gradle using Kotlin DSL:

```kotlin
implementation("hu.vissy.kotlin-json-dsl:kotlin-json-dsl:1.0.2")
```

<div style="background-color: #ff6666; color: white; padding: 15px; font-size: 120%;">
<strong>Note</strong>: Accidentally, the artifact name of the first release was called
<code>kotlin-json-dslcore</code>. It is wrong, and should not be used.</div>

## Usage

### Introduction

The library uses the builder DSL approach with local operator overloading 
to support a fluent and boiler-code free way to integrate JSON creation
into code.

A simple example:

```kotlin
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
                // A small caveat: Unary plus shorthand can't be used 
                // directly on numbers
                add(Random.nextInt(90)+1)
            }
        }
    }

    // Formatting JSON before print
    println( myJson.toPretty() )
```

will print:

```json
{
  "meaningOfLife": 42,
  "goodBye": "And thanks the fish",
  "signatures": [
    "Don", "Mark", "Liv"
  ],
  "lottery": [
    63, 19, 68, 24, 88
  ]
}
```

## Creating objects or arrays

### Creating JsonObject

To create a new `JsonObject` use the `jsonObject {}` builder.

```kotlin
    val myJson = jsonObject {
        "aKey" += aValue        
    } 
```

The return value of the builder is JsonObject (by GSON).

### Extending a JsonObject

The builder could also be used to extend an existing JsonObject. 
Simply pass the object to the builder:

```kotlin
    val myJson = jsonObject {
        "aKey" += aValue        
    }

    jsonObject(myJson) {
        "anotherKey" += aValue
    }
```

In this case the second builder will return the original json, which is extended (`myJson` will hold
both values). This mechanics is useful for creating JSON serialization in class hierarchy or for collection
data from multiple sources. (See examples later.)

### Creating a JsonArray programmatically

To create a new `JsonArray` use the `jsonArray {}` builder.

```kotlin
    val myJson = jsonArray {
        // you can use the unary plus operator for adding elements except for numbers
        +aValue        
        add(aNumber)
    } 
```

The return value of the builder is JsonArray (by GSON).

### Creating a JsonArray inline

An array could be constructed by simply passing the values to the builder:

```kotlin
    val myJson = jsonArray("aValue", 42, true)
```

The type of the values will be handled correctly.

*Note: a limitation for this that the first element of the array couldn't be a JsonArray itself, because 
it would be interpreted as an extension of the first argument.*

### Creating a JsonArray from an Iterable

An array could be directly constructed from any iterable:

```kotlin
    val myJson = jsonArray(myList)
```

### Extending a JsonArray

The builder could also be used to extend an existing JsonArray.
Simply pass the array to the builder:

```kotlin
    val myJson = JsonArray {
        +aValue        
    }

    jsonArray(myJson) {
        +anotherValue
    }
    
    // Extending inline or using an Iterable also possible
    jsonArray(myJson, aNewValue, anotherNewValue)
    jsonArray(myJson, myOtherList)
```

In these cases the builder will return the original json, which is extended (`myJson` will hold
all values added).

## Manipulating object fields

JSON Object field manipulation is generally done through local String operator overloading.
To set a value you have several options.

### Unconditional assignment

The `plusAssign` (`+=`) operator will overwrite the field with the provided value.

```kotlin
jsonObject {
    "field" += value // The field will be created even if value is null
}   
```

This assignment will use the correct json type for primitive values:

- null for `null`
- boolean for `Boolean`
- numeric for any numeric value

This statement will always create the field, even when the `value` is `null`, when null will be inserted.

Setting a sub-entity (object or array) is done absolutely the same way as setting a primitive. 
The input could be a builder or predefined json entity.

```kotlin
val obj = JsonObject()
jsonObject {
    "subObject" += jsonObject {
        "field" += "test"
        "subSub" += obj
    }

    "subArray" += jsonArray {
        +true
        +false
    }
}   
```

**Important: the assignment never copies (not even shallowly) the sub entities, so use the same 
object or array in multiple places as value with extreme caution. (Meaning: avoid it. ;-))**

### Conditional assignment

The conditional assignment is made by `remAssign` (`%=`) operator, which will **not** add the field if 
the `value` is null:

```kotlin
jsonObject {
    var value: String? = null
    "field" %= value // The field will be omitted
    value = "test"
    "field" %= value // The field will be added
    value = null
    "field" %= value // The field will be removed from the object
}
```

### Non-empty assignment

Using the non-empty setter by `divAssign` (`/=`) works the same way with any primitive values as the 
conditional assignment, but for sub-entities (object or array) it has an additional flavor: it doesn't
add the sub-entity if it is empty (no fields in an object or no elements in an array).

```kotlin
jsonObject {
    val subObject = jsonObject {}
    val subArray = jsonArray()
    "subObject" /= subObject // The field will be omitted
    "subArray" /= subArray // The field will be omitted
}
```

### Removing a key

A key could be removed by the `remove` function.

```kotlin
jsonObject {
    "field" += 42 // The json contains the field 
    remove("field") // The field is removed
}
```

### Adding values to an array

Generally, adding a value to an array is done by unary plus (prefix) operator.
However, due to the already existing overload of the operator on numeric values, it
won't work on numeric values. There the `add` function should be used. 

```kotlin
jsonArray {
    +true
    +"Text"
    +42 // This won't work
    add(42) // This works
    +null
}
```

## Reading values

For reading values from `JsonObject` the library offers some type-aware extensions.
Each based on the `invoke` (`()`) operator.

The simplest form requires only a key parameter and returns the value. This version requires the 
field to exist and not to be null, otherwise throws an exception.
What it adds as a plus over the original index (`[]`) operator is that it casts the value to the 
appropriate type of the target.
The following target types are supported:

- numeric values (`Int`, `Long` and `Double`)
- `String`
- `Boolean`
- `LocalDate`, `LocalTime`, `LocalDateTime`, if the value is in correct ISO format
- `JsonObject` and `JsonArray`
- `null`

If you wish to read a field, which may not be there or may be null, use the `optional` keyword as 
second parameter: `json("field", optional)`. This function will return null if the field doesn't exist
or the value is null.

The third variant is a flavor which allows default value as second parameter. The reason why sometimes
this function is better than using the Elvis operator is that by specifying the default value the type
could be automatically determined, and you don't have to declare.

For example, when the JSON is 

```json
{
  "aNumber" : 42,
  "empty" : null,
  "aBool" : false,
  "aDate" : "2024-01-20"
}
```

the following statements could be used:

```kotlin
// The field should exist and not be null
val num : Int = json("aNumber")

// If you specify default value, the type don't have to be specified explicitly  
val num2 = json("aNumber", 42)
// It is the same as
val num3: Int = json("aNumber", optional) ?: 42

// Not correct, value can't be null, will throw exception
val emptyWrong: String = json("empty")
// With optional flag it is correct
val empty: String? = json("empty", optional)

// Type specification on invoke
val bool = json.invoke<Boolean>("aBool")      

// With default value
val nonExisting = json("missing", "not there")

// Reading date
val date : LocalDate = json("aDate")
```

## Path function

The library provides some simple json path expression support.
The `JsonEntity.strictPath` will throw exception if the value defined by the path doesn't exist,
while the `JsonEntity.path` function returns null in this case.

The expression is a string. Any object field could be referenced by `.` + field name, and indexing
in an array could be done by `[]` holding the index. When you want to access an array in an array,
you can specify the indexes within the same brackets, separated by comma.


## Utility functions

The library provides some convenient shortcuts:

The `fromJson` extension function on `String` will parse the string and returns a `JsonElement`.  

```kotlin
val json = aJsonString.fromJson().asJsonObject
```

The `toPretty` extension function on `JsonElement` will format the json into a `String` well formatted.
This format is a little more compact than the one provided by GSON.

## Advanced usage

### Using in a serialization method

The structure is convenient to implement serialization over object hierarchy.

For example:

```kotlin
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

val child = ChildClass(42, listOf("This", "is", "Sparta"), mapOf( "val1" to "Alma", "val2" to 1, "val3" to LocalDateTime.now() ))
println(child.toJson().toPretty())
val child2 = ChildClass(42, listOf(), mapOf())
println(child2.toJson().toPretty())

```

will print:

```json
{
    "number": 42,
    "text": [
        "This", "is", "Sparta"
    ],
    "data": [
        {
            "key": "val1",
            "value": "Alma"
        },
        {
            "key": "val2",
            "value": "1"
        },
        {
            "key": "val3",
            "value": "2024-01-27T13:34:11.077086100"
        }
    ]
}
```
```json
{
    "number": 42,
    "data": []
}

```

