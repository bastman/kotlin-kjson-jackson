package com.bastman.kjson.jackson.examples

import com.bastman.kjson.jackson.Json
import com.bastman.kjson.jackson.JsonJmesPath
import com.bastman.kjson.jackson.loadResource
import com.bastman.kjson.jackson.loadResourceText
import com.fasterxml.jackson.databind.ObjectMapper

import java.time.Instant

val JSON = Json.relaxed()
val JQ = JsonJmesPath(JSON.mapper)

fun Any?.toJson() = JSON.encode(this)
fun Any?.toJson(mapper: ObjectMapper) = JSON.withMapper(mapper).encode(this)

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




