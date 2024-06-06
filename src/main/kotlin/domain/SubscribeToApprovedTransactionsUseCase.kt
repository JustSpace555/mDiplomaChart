package domain

import data.Repository

class SubscribeToApprovedTransactionsUseCase(private val repository: Repository) {

    operator fun invoke() = repository.subscribeToApprovedTransactionFlow()
}