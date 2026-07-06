package quest.nekoniyah.guildly.database.managers.player

import net.minecraft.server.level.ServerPlayer
import quest.nekoniyah.guildly.database.core.Database
import quest.nekoniyah.guildly.database.core.ResultRow
import quest.nekoniyah.guildly.database.core.select
import quest.nekoniyah.guildly.database.core.upsert
import quest.nekoniyah.guildly.database.tables.PlayersTable

object PlayerManager {
	val loadedPlayers: MutableSet<PlayerData> = mutableSetOf()
	private val online: MutableMap<String, ServerPlayer> = mutableMapOf()

	init {
		Database.createTable(PlayersTable)
	}

	fun add(player: ServerPlayer) {
		online[player.stringUUID] = player
		val name = player.displayName?.string ?: player.gameProfile.name
		val lastSeen = (System.currentTimeMillis() / 1000).toInt()
		loadedPlayers.removeIf { it.uuid == player.stringUUID }
		loadedPlayers.add(PlayerData(player.stringUUID, name, lastSeen, saved = false))
	}

	fun remove(player: ServerPlayer) {
		online.remove(player.stringUUID)
	}

	fun findOnlineByUUID(uuid: String): ServerPlayer? = online[uuid]
	fun findOnlineByName(name: String): ServerPlayer? = online.values.find { it.displayName?.string == name }
	val onlinePlayers: List<ServerPlayer> get() = online.values.toList()

	fun getByUUID(uuid: String): PlayerData? {
		loadedPlayers.find { it.uuid == uuid }?.let { return it }
		val row = PlayersTable.select().where("uuid = ?", uuid).fetchOne(Database) ?: return null
		return mapRow(row).also { loadedPlayers.add(it) }
	}

	fun getByName(name: String): PlayerData? {
		loadedPlayers.find { it.name == name }?.let { return it }
		val row = PlayersTable.select().where("name = ?", name).fetchOne(Database) ?: return null
		return mapRow(row).also { loadedPlayers.add(it) }
	}

	fun loadAll() {
		loadedPlayers.clear()
		PlayersTable.select().fetch(Database).forEach { row ->
			loadedPlayers.add(mapRow(row))
		}
	}

	fun saveAll() {
		loadedPlayers.filter { !it.saved }.forEach { data ->
			val success = PlayersTable.upsert(Database) {
				set(PlayersTable.uuid, data.uuid)
				set(PlayersTable.name, data.name)
				set(PlayersTable.lastSeen, data.lastSeen)
			}
			if (success) data.saved = true
		}
	}

	private fun mapRow(row: ResultRow): PlayerData = PlayerData(
		uuid = row.get(PlayersTable.uuid),
		name = row.get(PlayersTable.name),
		lastSeen = row.get(PlayersTable.lastSeen),
		saved = true
	)
}
