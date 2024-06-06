package presentation

import domain.SubscribeToApprovedTransactionsUseCase
import domain.SubscribeToNewTransactionsUseCase
import tornadofx.ViewModel

class MainViewModel(
    private val subscribeToNewTransactionsUseCase: SubscribeToNewTransactionsUseCase,
    private val subscribeToApprovedTransactionsUseCase: SubscribeToApprovedTransactionsUseCase,
) : ViewModel() {

    fun newTransactionsListener() = subscribeToNewTransactionsUseCase()

    fun approvedTransactionsListener() = subscribeToApprovedTransactionsUseCase()
}