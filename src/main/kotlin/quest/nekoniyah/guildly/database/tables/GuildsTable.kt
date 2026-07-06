package quest.nekoniyah.guildly.database.tables

import quest.nekoniyah.guildly.database.core.Column
import quest.nekoniyah.guildly.database.core.Schema

/**
 * Schema definition for `guilds` table.
 *
 * `playerIds` is stored as a JSON-encoded string array (e.g. `["uuid1", "uuid2"]`).
 */
object GuildsTable: Schema("guilds") {
	val name: Column<String> = text("name", primaryKey = true)
	val ownerId: Column<String> = text("ownerId")
	val playerIds: Column<String> = text("playerIds")

	init {
		columns.addAll(listOf(name, ownerId, playerIds))
	}
}
