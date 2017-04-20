package com.bastman.kjson.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule


class Json(val mapper: ObjectMapper) {

    companion object {

        fun default(): Json {
            val mapper: ObjectMapper = jacksonObjectMapper()
                    .registerModule(ParameterNamesModule())
                    .registerModule(Jdk8Module())
                    .registerModule(JavaTimeModule())

            return Json(mapper = mapper)
                    // encoder
                    .encoderEnumsAsString(true)
                    .encoderDatesAsTimestamps(false)
                    .encoderDateTimestampsAsNanos(false)
                    // decoder
                    .decoderEnumsAsString(true)
                    .decoderDateTimestampsAsNanos(false)
                    .decoderAdjustDatesToContextTimezone(false)
        }

        fun strict() = default()
                .decoderFailOnUnknownProperties(true)

        fun relaxed() = default()
                .decoderFailOnUnknownProperties(false)

    }

    fun copy(): Json = Json(mapper = mapper.copy())
    fun withMapper(mapper: ObjectMapper) = Json(mapper = mapper)

    fun encode(data: Any?): String = mapper.writeValueAsString(data)
    inline fun <reified T> decode(json: String): T = mapper.readValue(json, object : TypeReference<T>() {})
    fun <T> decode(json: String, valueType: Class<T>): T = mapper.readValue(json, valueType)
    fun normalize(json: String): String {
        // seems to fix issues with property ordering
        // result can be compared to sth else.
        val decoded: Any? = decode(json)

        return encode(decoded).trim()
    }

    fun configureEncoder(feature: SerializationFeature, enabled: Boolean): Json = copy().apply {
        mapper.configure(feature, enabled)
    }

    fun configureDecoder(feature: DeserializationFeature, enabled: Boolean) = copy().apply {
        mapper.configure(feature, enabled)
    }


}


// configure: encoder

fun Json.encoderEnumsAsString(enabled: Boolean): Json = configureEncoder(
        SerializationFeature.WRITE_ENUMS_USING_TO_STRING, enabled
)

fun Json.encoderSerializationInclusion(incl: JsonInclude.Include) = copy().apply {
    mapper.setSerializationInclusion(incl)
}

fun Json.encoderExcludeNull(): Json = encoderSerializationInclusion(JsonInclude.Include.NON_NULL)


fun Json.encoderDatesAsTimestamps(enabled: Boolean): Json = configureEncoder(
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, enabled
)

fun Json.encoderDateTimestampsAsNanos(enabled: Boolean): Json = configureEncoder(
        SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, enabled
)

// configure decoder

fun Json.decoderFailOnUnknownProperties(enabled: Boolean): Json = configureDecoder(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, enabled
)

fun Json.decoderEnumsAsString(enabled: Boolean): Json = configureDecoder(
        DeserializationFeature.READ_ENUMS_USING_TO_STRING, enabled
)

fun Json.decoderDateTimestampsAsNanos(enabled: Boolean): Json = configureDecoder(
        DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, enabled
)

fun Json.decoderAdjustDatesToContextTimezone(enabled: Boolean): Json = configureDecoder(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, enabled
)
