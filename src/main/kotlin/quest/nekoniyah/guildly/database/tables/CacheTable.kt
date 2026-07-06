package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.utils.database.Table
import quest.nekoniyah.guildly.utils.database.TableField

val CacheTable = Table("cache").setFields(
    TableField("players", "TEXT")
)