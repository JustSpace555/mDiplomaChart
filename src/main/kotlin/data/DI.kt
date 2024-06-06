package data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module

val dataModule = module {

    single<HttpClient> {
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(SSE)
            install(ContentNegotiation) { json() }
        }
    }

    factory<TransactionMapper> { TransactionMapper() }

    single<Repository> { Repository(get(), get()) }
}
