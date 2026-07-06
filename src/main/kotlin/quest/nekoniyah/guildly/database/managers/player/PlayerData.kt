package quest.nekoniyah.guildly.database.managers.player

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
	val uuid: String,
	val name: String,
	val lastSeen: Int,
	var saved: Boolean = false
)
