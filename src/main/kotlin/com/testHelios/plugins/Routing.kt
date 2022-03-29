package com.testHelios.plugins

import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileReader
import java.io.FileWriter


fun Application.configureRouting() {

    routing {

        get("/") {
            call.respondText("Hello World!")
        }

        get("/openapi.json") {
            call.respond(application.openAPIGen.api.serialize())
        }

        get("/docs") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }

        get("/mostUsedParams") {
            val result = GlobalScope.async {
                getMostUsedParams("mostUsedParameters.json").counterMap
            }
            val counterMap = result.await()
            if (counterMap.isEmpty()) call.respond(HttpStatusCode.Accepted, "FizzBuzz API never used")
            else
                call.respond(HttpStatusCode.Accepted, counterMap.entries.maxByOrNull { it.value }!!.key)
        }

        /**
         * Fizz buzz function endpoint taking safely 5 query params documented in /docs
         * logs the query params into a file (to set in configuration for prod environment)
         * responds by the fizz buzz behavior documented in FizzBuzz.kt functional programming file
         */
        get("/fizzbuzz") {
            try {
                val int1: Int = call.request.queryParameters["int1"].castIntParam("int1")
                val int2: Int = call.request.queryParameters["int2"].castIntParam("int2")
                val limit: Int = call.request.queryParameters["limit"].castIntParam("limit").checkValidity(max = 100)
                val str1: String = call.request.queryParameters["str1"].castStringParam("str1")
                val str2: String = call.request.queryParameters["str2"].castStringParam("str2")

                val fizzBuzzParams = FizzBuzzParams(int1, int2, limit, str1, str2)

                logRequestCounterAsync(fizzBuzzParams, "mostUsedParameters.json").await()

                call.respondText(
                    FizzBuzz.listReplacement(
                        int1 = int1,
                        int2 = int2,
                        limit = limit,
                        str1 = str1,
                        str2 = str2
                    ).toString()
                )
            } catch (e: BadContentTypeFormatException) {
                call.respond(HttpStatusCode.BadRequest, e.message!!)
            }
        }
    }
}

/**
 * Coroutine safe file reading (/mostUsedParameters.json)
 * Reads from the file and writes to the file at project root
 * In production we can imagine the same behavior with a Mongo Atlas instance or a redis Instance
 */
fun logRequestCounterAsync(
    fizzBuzzParams: FizzBuzzParams,
    filename: String
) = GlobalScope.async {
    val map: FizzBuzzParamsCounter = getMostUsedParams(filename)
    map.addEntry(fizzBuzzParams.toString())
    putMostUsedParams(map, filename)
    map
}


/**
 * puts in the map
 * Run this in coroutine safe environment
 */
private suspend fun putMostUsedParams(map: FizzBuzzParamsCounter, fileName: String) {
    withContext(Dispatchers.IO) {
        FileWriter(fileName).use {
            it.write(Json.encodeToString(map))
        }
    }
}

/**
 * fetches the map
 * Run this in coroutine safe environment
 */
private suspend fun getMostUsedParams(fileName: String): FizzBuzzParamsCounter =
    withContext(Dispatchers.IO) {
        FileReader(fileName).use {
            val fileContent = it.readText()
            Json.decodeFromString<FizzBuzzParamsCounter>(fileContent)
        }
    }


private fun Int.checkValidity(max: Int): Int {
    if (this >= max) throw BadContentTypeFormatException("$this is not a conform value, $max is the maximal value")
    return this
}

fun Application.configureAPIRouting() {
    apiRouting {
        route("/fizzbuzz").get<FizzBuzzParams, StringResponse>(
            info(
                "Fizzbuzz endpoint",
                "This is the answer to the fizzbuzz problem"
            ), example = StringResponse("/fizzbuzz?int1=3&int2=7&limit=16&str1=fizz&str2=buzz")
        ) {
            respond(StringResponse("[1,2,fizz,4,5,fizz,buzz,8,fizz,10,11,fizz,13,buzz,fizz,16]"))
        }
        route("/mostUsedParams").get<String, StringResponse>(
            info("Retrieves the most used param set of fizzbuzz API"),
            example = StringResponse("/mostUsedParams")
        ) {
            respond(StringResponse("int1=3&int2=7&limit=16&str1=fizz&str2=buzz"))
        }
    }
}

@Serializable
@Path("/fizzbuzz")
data class FizzBuzzParams(
    @QueryParam("first value to match") val int1: Int,
    @QueryParam("second value to match") val int2: Int,
    @QueryParam("limit from one to limit, max value = 100") val limit: Int,
    @QueryParam("string replacing int1 matches") val str1: String,
    @QueryParam("string replacing int2 matches") val str2: String,
)

@Serializable
data class FizzBuzzParamsCounter(val counterMap: LinkedHashMap<String, Int>) {
    fun addEntry(params: String) {
        val value = counterMap[params]
        if (value == null) counterMap[params] = 1
        else counterMap[params] = value + 1
    }
}

@Response("A String Response")
data class StringResponse(val str: String)

private fun String?.castStringParam(paramName: String): String {
    return this ?: throw BadContentTypeFormatException("$paramName is mandatory.")
}

private fun String?.castIntParam(paramName: String): Int {
    try {
        return this?.toInt() ?: throw BadContentTypeFormatException("$paramName cannot be empty.")
    }catch (e: java.lang.NumberFormatException){
        throw BadContentTypeFormatException("$paramName must be an integer.")
    }
}