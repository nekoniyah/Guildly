package quest.nekoniyah.guildly.utils.database.joinrequest

import kotlinx.serialization.Serializable

@Serializable
data class JoinRequestData(
    val id: String,
    val userId: String,
    val guildName: String,
    val reason: String? = null,
    var saved: Boolean = false
)
