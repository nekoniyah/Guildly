package quest.nekoniyah.guildly.utils

import kotlinx.serialization.Serializable

@Serializable
data class GuildData(
    val name: String,
    val ownerId: String,
    val playerIds: List<String>,
)