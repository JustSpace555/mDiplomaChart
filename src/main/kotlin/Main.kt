import data.dataModule
import domain.domainModule
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import presentation.MainApp
import presentation.presentationModule
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

fun main() {

    startKoin {
        modules(dataModule, domainModule, presentationModule)
    }

    FX.dicontainer = object : DIContainer, KoinComponent {
        override fun <T : Any> getInstance(type: KClass<T>): T = getKoin().get(type)
    }

    launch<MainApp>()
}
