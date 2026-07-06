package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.utils.database.Table
import quest.nekoniyah.guildly.utils.database.TableField

val JoinRequestsTable = Table("join_requests").setFields(
    TableField("id", "TEXT", primaryKey = true),
    TableField("userId", "TEXT"),
    TableField("guildName", "TEXT")
)