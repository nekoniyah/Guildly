package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.database.core.Column
import quest.nekoniyah.guildly.database.core.Schema

object PlayersTable : Schema("players") {
    val uuid: Column<String> = text("uuid", primaryKey = true)
    val name: Column<String> = text("name")
    val lastSeen: Column<Int> = integer("lastSeen", default = "0")
}
