package com.bastman.kjson.jackson.examples

import com.bastman.kjson.jackson.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.time.Instant

fun JsonBuilder.relaxed() = withDeserializationFeatureConfig(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
)

val JSON = JsonBuilder
        .default()
        .withModules(
                JavaTimeModule(),
                Jdk8Module(),
                ParameterNamesModule()
        )
        .relaxed()
        .build()

val JQ = JsonJmesPath(JSON)

fun Any?.toJson(json: Json=JSON) = json.encode(this)

data class MyDto(val foo: String, val t: Instant)
data class JqTestCase(val resource: String, val query: String)

fun main(args: Array<String>) {
    Main.basicExample()
    Main.jqExamples()
}

object Main {

    val jqExamplesDir = "jmespath-examples"
    val jqTestCases: List<JqTestCase> = listOf(
            JqTestCase(resource = "$jqExamplesDir/example-001.json", query = "a"),
            JqTestCase(resource = "$jqExamplesDir/example-002.json", query = "a.b.c.d")
    )


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

}




