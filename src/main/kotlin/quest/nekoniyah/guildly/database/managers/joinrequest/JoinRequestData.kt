package quest.nekoniyah.guildly.database.managers.joinrequest

import kotlinx.serialization.Serializable

@Serializable
data class JoinRequestData(
	val id: String,
	val userId: String,
	val guildName: String,
	var saved: Boolean = false,
)
