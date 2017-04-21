package com.bastman.kjson.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime

class JsonJmesPath(
        val mapper: ObjectMapper,
        val runtime: JmesPath<JsonNode> = JACKSON_RUNTIME
) {

    companion object {
        private val JACKSON_RUNTIME = JacksonRuntime()
    }

    fun compile(query: String): Expression<JsonNode> = runtime.compile(query)

    fun search(content: String, expression: Expression<JsonNode>): JsonNode = expression.search(
            mapper.readTree(content)
    )

    fun search(content: String, query: String): JsonNode {
        val expression = compile(query = query)

        return search(content = content, expression = expression)
    }

    inline fun <reified T> query(content: String, query: String, encoder: ObjectMapper = mapper): T {
        val expression = compile(query = query)

        val resultNode = search(content = content, expression = expression)
        val resultJson = jsonEncode(data = resultNode, mapper = encoder)

        return jsonDecode(json = resultJson, mapper = mapper)
    }


    inline fun <reified T> jsonDecode(json: String, mapper: ObjectMapper): T = mapper.readValue(json, object : TypeReference<T>() {})
    inline fun <reified T> jsonDecode(json: String): T = jsonDecode(json = json, mapper = mapper)

    fun <T> jsonDecode(json: String, valueType: Class<T>, mapper: ObjectMapper): T = mapper.readValue(json, valueType)
    fun <T> jsonDecode(json: String, valueType: Class<T>): T = jsonDecode(json = json, valueType = valueType, mapper = mapper)

    fun jsonEncode(data: Any?, mapper: ObjectMapper): String = mapper.writeValueAsString(data)
    fun jsonEncode(data: Any?): String = jsonEncode(data = data, mapper = mapper)

}
