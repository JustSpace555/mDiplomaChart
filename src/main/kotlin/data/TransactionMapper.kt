package data

import data.model.Transaction
import data.model.TransactionDto
import java.util.GregorianCalendar

class TransactionMapper {

    fun map(dto: TransactionDto): Transaction = Transaction(
        sensorId = requireNotNull(dto.sensorId),
        prevIds = requireNotNull(dto.prevIds),
        time = GregorianCalendar.getInstance().apply {
            timeInMillis = requireNotNull(dto.epochTime).toLong()
        }.time,
        gasLevel = requireNotNull(dto.gasLevel),
        signedHash = requireNotNull(dto.signedHash),
    )
}