package com.bastman.kson.jackson.fge.schemavalidation

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.core.report.LogLevel
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory


class JsonSchemaValidator(private val schema: JsonSchema) {
    companion object {
        private val SCHEMA_FACTORY_DEFAULT = JsonSchemaFactory.byDefault()

        fun of(schema: JsonSchema): JsonSchemaValidator = JsonSchemaValidator(schema)

        fun of(schemaNode: JsonNode, schemaFactory: JsonSchemaFactory = SCHEMA_FACTORY_DEFAULT): JsonSchemaValidator {
            return of(schema = schemaFactory.getJsonSchema(schemaNode))
        }
    }

    fun verify(content: JsonNode, deepCheck: Boolean = false): JsonSchemaValidationResult {
        val report = schema.validate(content, deepCheck)

        val messages = report.map {
            JsonSchemaValidationResult.Error(
                    message = it.message,
                    logLevel = it.logLevel,
                    details = it.asJson(),
                    detailsText = it.toString()
            )
        }

        return JsonSchemaValidationResult(
                isSuccess = report.isSuccess,
                logLevel = report.logLevel,
                errors = messages
        )
    }
}

data class JsonSchemaValidationResult(
        val isSuccess: Boolean,
        val logLevel: LogLevel,
        val errors: List<Error>
) {
    data class Error(
            val message: String,
            val logLevel: LogLevel,
            val details: JsonNode,
            val detailsText: String
    )
}

