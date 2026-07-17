package quest.nekoniyah.guildly.database.managers.guild

import kotlinx.serialization.Serializable

@Serializable
data class GuildData(
	val name: String,
	val ownerId: String,
	val playerIds: MutableSet<GuildMember>,
	var saved: Boolean = false
)

@Serializable
data class GuildMember(
	val uuid: String,
	val role: String = "member"
)
