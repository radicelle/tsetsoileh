package com.testHelios

import com.papsign.ktor.openapigen.OpenAPIGen
import com.testHelios.plugins.configureAPIRouting
import com.testHelios.plugins.configureRouting
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*


/**
 * Server configuration
 * API doc accessible via /docs -> openAPI on swagger
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(OpenAPIGen) {
            info {
                version = "0.0.1"
                title = "Test Helios"
                description = "Fuzz buzz test"

                contact {
                    name = "Emmanuel Breton-Belz"
                    email = "emmanuelbretonbelz@gmail.com"
                }
            }
        }
        configureRouting()
        configureAPIRouting()
    }.start(wait = true)
}
