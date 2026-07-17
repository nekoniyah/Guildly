package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.database.core.Column
import quest.nekoniyah.guildly.database.core.Schema

object JoinRequestsTable: Schema("join_requests") {
	val id: Column<String> = text("id", primaryKey = true)
	val userId: Column<String> = text("userId")
	val guildName: Column<String> = text("guildName")
}
