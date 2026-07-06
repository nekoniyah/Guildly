package quest.nekoniyah.guildly.database.managers.joinrequest

import quest.nekoniyah.guildly.database.cache.PlayerCache
import quest.nekoniyah.guildly.database.core.Database
import quest.nekoniyah.guildly.database.core.ResultRow
import quest.nekoniyah.guildly.database.core.delete
import quest.nekoniyah.guildly.database.core.select
import quest.nekoniyah.guildly.database.core.upsert
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.tables.GuildsTable
import quest.nekoniyah.guildly.database.tables.JoinRequestTable
import quest.nekoniyah.guildly.utils.Feedback

object JoinRequestManager {
	val loadedRequests: MutableSet<JoinRequestData> = mutableSetOf()

	init {
		Database.createTable(JoinRequestTable)
	}

	fun getByUser(userId: String): JoinRequestData? {
		loadedRequests.find { it.userId == userId }?.let { return it }
		val row = JoinRequestTable.select().where("userId = ?", userId).fetchOne(Database) ?: return null
		return mapRow(row).also { loadedRequests.add(it) }
	}

	fun addRequest(request: JoinRequestData) {
		loadedRequests.add(request.copy(saved = false))
	}

	fun acceptRequest(userId: String) {
		val request = getByUser(userId) ?: return
		val guild = GuildManager.loadedGuilds.find { it.name == request.guildName } ?: return
		PlayerCache.findByUUID(userId)?.sendSystemMessage(Feedback.build("Your request to join the ${guild.name} guild has been approved."))
		val applicantName = PlayerCache.findByUUID(userId)?.displayName?.string ?: PlayerCache.findByUUID(userId)?.name?.string ?: userId
		PlayerCache.findByUUID(guild.ownerId)?.sendSystemMessage(Feedback.build("$applicantName has been approved to join the ${guild.name} guild."))
		guild.playerIds.add(userId)
		GuildManager.updateGuild(guild)
		deleteRequest(request)
	}

	fun deleteRequest(request: JoinRequestData) {
		loadedRequests.remove(request)
		JoinRequestTable.delete(Database) {
			where("id = ?", request.id)
		}
	}

	fun loadAll() {
		loadedRequests.clear()
		JoinRequestTable.select().fetch(Database).forEach { row -> loadedRequests.add(mapRow(row)) }
	}

	fun saveAll() {
		loadedRequests.filter { !it.saved }.forEach { request ->
			val success = JoinRequestTable.upsert(Database) {
				set(JoinRequestTable.id, request.id)
				set(JoinRequestTable.userId, request.userId)
				set(JoinRequestTable.guildName, request.guildName)
			}
			if (success) request.saved = true
		}
	}

	private fun mapRow(row: ResultRow): JoinRequestData = JoinRequestData(
		id = row.get(JoinRequestTable.id),
		userId = row.get(JoinRequestTable.userId),
		guildName = row.get(JoinRequestTable.guildName),
		saved = true
	)
}
