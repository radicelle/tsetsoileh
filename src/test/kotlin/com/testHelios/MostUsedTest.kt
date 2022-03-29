package com.testHelios

import com.testHelios.plugins.FizzBuzzParams
import com.testHelios.plugins.logRequestCounterAsync
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertEquals


class MostUsedTest {

    @BeforeTest
    fun `clean file before test`() {
        File(object{}.javaClass.getResource("/mostUsedParametersInit.json").file).copyTo(File("mostUsedParametersTEST.json"), overwrite = true)
    }

    @Test
    fun `Test update map of most used`() {
        runBlocking {
            val fizzBuzzParamsExample = FizzBuzzParams(1, 1, 10, "a", "b")
            val mapCounter =
                logRequestCounterAsync(fizzBuzzParamsExample, "mostUsedParametersTEST.json")
            assertEquals(
                1,
                mapCounter.await().counterMap[fizzBuzzParamsExample.toString()],
                "Map has not been incremented"
            )
        }
    }
}
