package com.bastman.kjson.jackson

import com.fasterxml.jackson.databind.JsonNode
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime

class JsonJmesPath(
        val json: Json,
        val runtime: JmesPath<JsonNode> = JACKSON_RUNTIME
) {

    companion object {
        private val JACKSON_RUNTIME = JacksonRuntime()
    }

    fun compile(query: String): Expression<JsonNode> = runtime.compile(query)

    fun search(content: String, expression: Expression<JsonNode>, codec: Json = json): JsonNode = expression.search(
            codec.decodeTree(content)
    )

    fun search(content: String, query: String, codec: Json = json): JsonNode {
        val expression = compile(query = query)

        return search(content = content, expression = expression, codec = codec)
    }

    inline fun <reified T> query(content: String, query: String, codec: Json = json): T {
        val expression = compile(query = query)

        val resultNode = search(content = content, expression = expression, codec = codec)
        val resultJson = codec.encode(resultNode)

        return codec.decode(content = resultJson)
    }


    inline fun <reified T> jsonDecode(content: String, codec: Json = json): T = codec.decode(content)
    fun <T> jsonDecode(content: String, valueType: Class<T>, codec: Json = json): T = codec.decode(content, valueType)

    fun jsonEncode(data: Any?, codec: Json = json): String = codec.encode(data)

}
