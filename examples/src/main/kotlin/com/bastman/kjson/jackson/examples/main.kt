package com.bastman.kjson.jackson.examples

import com.bastman.kjson.jackson.Json
import com.bastman.kjson.jackson.loadResource

import java.time.Instant

val JSON = Json.relaxed()
fun Any?.toJson() = JSON.encode(this)
data class MyDto(val foo: String, val t: Instant)

fun main(args: Array<String>) {

    val resources: List<String> = listOf(
            "example-data/example1.json"
    )

    resources.forEach {
        println("load $it ...")
        val dto: MyDto = JSON.loadResource(resource = it)

        println("dto: $dto")

        val outJson = dto.toJson()
        println("dto.toJson: $outJson")
    }

}


