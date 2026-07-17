package quest.nekoniyah.guildly.database.managers.guild

import kotlinx.serialization.json.Json
import quest.nekoniyah.guildly.database.core.Database
import quest.nekoniyah.guildly.database.core.ResultRow
import quest.nekoniyah.guildly.database.core.delete
import quest.nekoniyah.guildly.database.core.select
import quest.nekoniyah.guildly.database.core.upsert
import quest.nekoniyah.guildly.database.tables.GuildsTable

object GuildManager {
	val loadedGuilds: MutableSet<GuildData> = mutableSetOf()

	init {
		Database.createTable(GuildsTable)
	}

	fun getGuild(name: String): GuildData? {
		loadedGuilds.find { it.name == name }?.let { return it }
		val row = GuildsTable.select().where("name = ?", name).fetchOne(Database) ?: return null
		return mapRow(row).also { loadedGuilds.add(it) }
	}

	fun exists(name: String): Boolean {
		if (loadedGuilds.any { it.name == name }) return true
		return GuildsTable.select().where("name = ?", name).fetchOne(Database) != null
	}

	fun findGuildOf(playerId: String): GuildData? {
		return loadedGuilds.find { it.ownerId == playerId || (it.playerIds.find { p -> p.uuid == playerId}) != null }
	}

	fun isValidName(name: String): Boolean = name.matches(Regex("^[a-zA-Z_]+$"))

	fun updateGuild(guild: GuildData) {
		loadedGuilds.removeIf { it.name == guild.name }
		loadedGuilds.add(guild.copy(saved = false))
	}

	fun deleteGuild(name: String) {
		loadedGuilds.removeIf { it.name == name }
		GuildsTable.delete(Database) {
			where("name = ?", name)
		}
	}

	fun loadAll() {
		loadedGuilds.clear()
		GuildsTable.select().fetch(Database).forEach { row -> loadedGuilds.add(mapRow(row)) }
	}

	fun saveAll() {
		loadedGuilds.filter { !it.saved }.forEach { guild ->
			val playerIdsJson = Json.encodeToString(guild.playerIds)
			val success = GuildsTable.upsert(Database) {
				set(GuildsTable.name, guild.name)
				set(GuildsTable.ownerId, guild.ownerId)
				set(GuildsTable.playerIds, playerIdsJson)
			}
			if (success) guild.saved = true
		}
	}

	private fun mapRow(row: ResultRow): GuildData {
		val playerIds = Json.decodeFromString<MutableSet<GuildMember>>(row.get(GuildsTable.playerIds))
		return GuildData(
			name = row.get(GuildsTable.name),
			ownerId = row.get(GuildsTable.ownerId),
			playerIds = playerIds,
			saved = true
		)
	}
}
