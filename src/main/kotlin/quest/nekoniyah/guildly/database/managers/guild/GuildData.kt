package quest.nekoniyah.guildly.database.managers.guild

import kotlinx.serialization.Serializable

@Serializable
data class GuildData(
	val name: String,
	val ownerId: String,
	val playerIds: MutableSet<String>,
	var saved: Boolean = false
)
