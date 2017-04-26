package com.bastman.kjson.jackson.examples

import com.bastman.kjson.jackson.codec.Json
import com.bastman.kjson.jackson.codec.JsonBuilder
import com.bastman.kjson.jackson.fs.loadResource
import com.bastman.kjson.jackson.fs.loadResourceText
import com.bastman.kson.jackson.burt.jmespath.JsonJmesPath
import com.bastman.kson.jackson.fge.schemavalidation.JsonSchemaValidator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.time.Instant

fun JsonBuilder.relaxed() = withoutDeserializationFeature(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
)

fun Any?.toJson(json: Json = JSON) = json.encode(this)

val JSON_BUILDER = JsonBuilder
        .default()
        .withModules(
                JavaTimeModule(),
                Jdk8Module(),
                ParameterNamesModule()
        )
        .withoutSerializationFeature(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
        )

val JSON = JSON_BUILDER
        .relaxed()
        .build()

val JQ = JsonJmesPath(JSON)


data class MyDto(val foo: String, val t: Instant)
data class JqTestCase(val resource: String, val query: String)

fun main(args: Array<String>) {
    Main.basicExample()
    Main.jqExamples()
    Main.jsonSchemaExamples()
}

object Main {


    val jqExamplesDir = "jmespath-examples"
    val jqTestCases: List<JqTestCase> = listOf(
            JqTestCase(resource = "$jqExamplesDir/example-001.json", query = "a"),
            JqTestCase(resource = "$jqExamplesDir/example-002.json", query = "a.b.c.d")
    )

    val jsonSchemaExamplesDir = "json-schema-examples"


    fun basicExample() {
        val resource = "example-data/example1.json"

        println("load json from $resource as ${MyDto::class} ...")
        val dto: MyDto = JSON.loadResource(resource = resource)
        println("dto: $dto")

        val outJson = dto.toJson()
        println("dto.toJson: $outJson")

        val query = "t"
        println("dto | jq(query='${query}) ...")
        val t: Instant = JQ.query(dto.toJson(), query)
        println("jq result: [${t::class} = $t]")
    }


    fun jqExamples() {

        jqTestCases.forEach {
            println("==== jq: resource=${it.resource} query=${it.query} ====")

            val sourceJson = JSON.loadResourceText(it.resource)
            println("sourceJson = $sourceJson")
            println("query = ${it.query}")

            val resultJson = JQ.search(sourceJson, it.query)
                    .toJson()

            println("resultJson = $resultJson")
        }

    }

    fun jsonSchemaExamples() {
        val testCases: List<JsonSchemaTestCase> = listOf(
                JsonSchemaTestCase(
                        schemaResource = "$jsonSchemaExamplesDir/001-schema.json",
                        contentResource = "$jsonSchemaExamplesDir/001-data.valid.json"
                ),
                JsonSchemaTestCase(
                        schemaResource = "$jsonSchemaExamplesDir/001-schema.json",
                        contentResource = "$jsonSchemaExamplesDir/001-data.invalid.json"
                )
        )

        testCases.forEach {
            testCase ->

            println("==== json-schema-test schema=${testCase.schemaResource} content=${testCase.contentResource} ===")
            val schemaValidator = JsonSchemaValidator.of(
                    schemaNode = JSON.loadResource(testCase.schemaResource)
            )
            val result = schemaValidator.verify(
                    content = JSON.loadResource(testCase.contentResource),
                    deepCheck = true
            )

            if (result.isSuccess) {
                println("JsonSchemaValidator: SUCCESS. json data matches schema")
            } else {
                println("JsonSchemaValidator: FAILED. (${result.logLevel}: json data does not match the given schema. )")
                result.errors.forEach {
                    println("- ${it.logLevel}: ${it.message}")
                }
            }
        }
    }

}

data class JsonSchemaTestCase(val schemaResource: String, val contentResource: String)




