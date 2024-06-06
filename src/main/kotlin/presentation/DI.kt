package presentation

import org.koin.dsl.module

val presentationModule = module {

    factory { MainViewModel(get(), get()) }
}