package domain

import data.Repository

class SubscribeToNewTransactionsUseCase(private val repository: Repository) {

    operator fun invoke() = repository.subscribeToNewTransactionsFlow()
}