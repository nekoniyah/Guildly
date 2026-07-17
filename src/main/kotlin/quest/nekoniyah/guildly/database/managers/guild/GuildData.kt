package quest.nekoniyah.guildly.database.managers.guild

import kotlinx.serialization.Serializable

@Serializable
data class GuildData(
    val name: String,
    var ownerId: String,
    val playerIds: MutableSet<GuildMember>,
    var saved: Boolean = false
)

@Serializable
data class GuildMember(
    val uuid: String,
    var role: String = "member"
)
