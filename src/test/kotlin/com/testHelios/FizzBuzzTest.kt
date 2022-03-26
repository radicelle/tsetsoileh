package com.testHelios

import com.testHelios.plugins.configureRouting
import io.ktor.http.*
import io.ktor.server.testing.*
import io.netty.handler.codec.http.HttpResponseStatus
import org.junit.jupiter.api.Test
import kotlin.test.junit5.JUnit5Asserter.assertEquals


class FizzBuzzTest {

    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals("Request failed", HttpStatusCode.OK, response.status())
                assertEquals("Response not matching", "Hello World!", response.content)
            }
        }
    }


    @Test
    fun `test fizzbuzz service responses`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=1&int2=1&limit=1&str1=fizz&str2=buzz").apply {
                assertEquals("Request failed", HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `test fizz with one fizzbuzz`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=2&int2=8&limit=10&str1=fizz&str2=buzz").apply {
                assertEquals("Request failed", HttpStatusCode.OK, response.status())
                assertEquals(
                    "Response not matching",
                    listOf("1", "fizz", "3", "fizz", "5", "fizz", "7", "fizzbuzz", "9", "fizz").toString(),
                    response.content
                )
            }
        }
    }

    @Test
    fun `test bullshit params`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=atchoum&int2=8&limit=10&str1=^^#'&str2=buzz").apply {
                assertEquals("Request succeeded and it should not", HttpStatusCode.BadRequest, response.status())
                assertEquals(
                    "Response not matching",
                    "Bad Content-Type format: int1 must be an integer.",
                    response.content
                )
            }
        }
    }

    @Test
    fun `test missing param`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=&int2=8&limit=10&str1=&str2=buzz").apply {
                assertEquals("Request succeeded and it should not", HttpStatusCode.BadRequest, response.status())
                assertEquals(
                    "Response not matching",
                    "Bad Content-Type format: int1 must be an integer.",
                    response.content
                )
            }
        }
    }

    @Test
    fun `test limit overload`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=2&int2=3&limit=1000&str1=toto&str2=buzz").apply {
                assertEquals("Request succeeded and it should not", HttpStatusCode.BadRequest, response.status())
                assertEquals(
                    "Response not matching",
                    "Bad Content-Type format: 1000 is not a conform value, 100 is the maximal value",
                    response.content
                )
            }
        }
    }

    @Test
    fun `test prime number no fizzbuzz`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/fizzbuzz?int1=3&int2=7&limit=16&str1=fizz&str2=buzz").apply {
                assertEquals("Request failed", HttpStatusCode.OK, response.status())
                assertEquals(
                    "Response not matching",
                    listOf(
                        "1",
                        "2",
                        "fizz",
                        "4",
                        "5",
                        "fizz",
                        "buzz",
                        "8",
                        "fizz",
                        "10",
                        "11",
                        "fizz",
                        "13",
                        "buzz",
                        "fizz",
                        "16"
                    ).toString(), response.content
                )
            }
        }
    }

    @Test
    fun `test fizzbuzz bad request`() {
        withTestApplication({ configureRouting() }) {


            handleRequest(HttpMethod.Get, "/fizzbuzz?wrongParam=something").apply {
                assertEquals("Response type not matching", HttpResponseStatus.BAD_REQUEST.code(), response.status()?.value)

            }
        }
    }
}
