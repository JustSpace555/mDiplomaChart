package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("id")
    val sensorId: Int?,
    val prevIds: List<String>?,
    val gasLevel: String?,
    val epochTime: ULong?,
    val signedHash: String?,
)