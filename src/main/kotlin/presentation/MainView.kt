package presentation

import javafx.scene.chart.NumberAxis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*
import java.text.SimpleDateFormat


class MainView : View() {

    private val viewModel: MainViewModel by di()

    private val cScope by lazy { CoroutineScope(Dispatchers.JavaFx + SupervisorJob()) }

    private val dateFormat by lazy { SimpleDateFormat("dd/MM/yyyy hh:mm:ss") }

    override val root = vbox {
        linechart("", DateAxis(), NumberAxis()) {
            series("Новые транзакции") {
                cScope.launch {
                    viewModel.newTransactionsListener().collect { transaction ->
                        data(transaction.time, transaction.gasLevel.toFloat(), transaction.signedHash) {
                            node.tooltip(
                                "ID сенсора: ${transaction.sensorId}\n" +
                                        "Уровень топлива: ${transaction.gasLevel}л\n" +
                                        "Дата: ${dateFormat.format(transaction.time)}"
                            )
                        }
                    }
                }
            }

            series("Подтвержденные транзакции") {
                cScope.launch {
                    viewModel.approvedTransactionsListener().collect { transaction ->
                        data(transaction.time, transaction.gasLevel.toFloat(), transaction.signedHash)
                    }
                }
            }
        }
    }
}
