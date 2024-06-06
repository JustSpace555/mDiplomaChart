package data.model

import java.util.Date

data class Transaction(
    val sensorId: Int,
    val prevIds: List<String>,
    val gasLevel: String,
    val time: Date,
    val signedHash: String,
)