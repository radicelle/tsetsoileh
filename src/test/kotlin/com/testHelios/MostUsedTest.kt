package com.testHelios

import com.testHelios.plugins.FuzzBuzzParams
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
            val fuzzBuzzParamsExample = FuzzBuzzParams(1, 1, 10, "a", "b")
            val mapCounter =
                logRequestCounterAsync(fuzzBuzzParamsExample, "mostUsedParametersTEST.json")
            assertEquals(
                1,
                mapCounter.await().counterMap[fuzzBuzzParamsExample.toString()],
                "Map has not been incremented"
            )
        }
    }
}
