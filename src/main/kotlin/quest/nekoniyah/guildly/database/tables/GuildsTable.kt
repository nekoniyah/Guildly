package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.utils.database.Table
import quest.nekoniyah.guildly.utils.database.TableField

val GuildsTable = Table("guilds").setFields(
    TableField("name", "TEXT", primaryKey = true),
    TableField("ownerId", "TEXT"),
    TableField("playerIds", "TEXT") // We will store this as a JSON array string
)