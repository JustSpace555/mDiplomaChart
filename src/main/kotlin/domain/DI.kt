package domain

import org.koin.dsl.module

val domainModule = module {
    factory<SubscribeToNewTransactionsUseCase> { SubscribeToNewTransactionsUseCase(get()) }
    factory<SubscribeToApprovedTransactionsUseCase> { SubscribeToApprovedTransactionsUseCase(get()) }
}