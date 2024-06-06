package data

import data.model.Transaction
import data.model.TransactionDto
import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class Repository(private val client: HttpClient, private val transactionMapper: TransactionMapper) {

    private val cScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    private val newTransactionsFlow = MutableSharedFlow<TransactionDto>()
    private val approvedTransactionsFlow = MutableSharedFlow<TransactionDto>()

    init {
        cScope.launch {
            initNewTransactionsListener()
        }
        cScope.launch {
            initApprovedTransactionListener()
        }
    }

    private suspend fun initNewTransactionsListener() {
        client.sse("$URL/operator/subscribe/transaction/new") {
            incoming.map { sseEvent ->
                sseEvent.data?.let { data -> Json.decodeFromString<TransactionDto>(data) }
            }.collect { dto ->
                dto?.let {
                    newTransactionsFlow.emit(dto)
                } ?: run {
                    println("New transactions listener: Data is empty")
                }
            }
        }
    }

    private suspend fun initApprovedTransactionListener() {
        client.serverSentEvents("$URL/operator/subscribe/transaction/approved") {
            incoming.map { sseEvent ->
                sseEvent.data?.let { data -> Json.decodeFromString<TransactionDto>(data) }
            }.collect { dto ->
                dto?.let {
                    approvedTransactionsFlow.emit(dto)
                }
            }
        }
    }

    fun subscribeToNewTransactionsFlow(): Flow<Transaction> = newTransactionsFlow.map(transactionMapper::map).onEach {
        println("LOG: New transaction. Id = ${it.signedHash}, prevIds = ${it.prevIds}")
    }

    fun subscribeToApprovedTransactionFlow(): Flow<Transaction> = approvedTransactionsFlow.map(transactionMapper::map).onEach {
        println("LOG: New approved transaction. Id = ${it.signedHash}, prevIds = ${it.prevIds}")
    }

    private companion object {
        private const val URL = "http://localhost:3000"
    }
}