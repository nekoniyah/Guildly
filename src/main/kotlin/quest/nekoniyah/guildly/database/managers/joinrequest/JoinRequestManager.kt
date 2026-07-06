package quest.nekoniyah.guildly.database.managers.joinrequest

import quest.nekoniyah.guildly.database.core.Database
import quest.nekoniyah.guildly.database.core.ResultRow
import quest.nekoniyah.guildly.database.core.delete
import quest.nekoniyah.guildly.database.core.select
import quest.nekoniyah.guildly.database.core.upsert
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import quest.nekoniyah.guildly.database.tables.JoinRequestsTable
import quest.nekoniyah.guildly.utils.Feedback

object JoinRequestManager {
	val loadedRequests: MutableSet<JoinRequestData> = mutableSetOf()

	init {
		Database.createTable(JoinRequestsTable)
	}

	fun getByUser(userId: String): JoinRequestData? {
		loadedRequests.find { it.userId == userId }?.let { return it }
		val row = JoinRequestsTable.select().where("userId = ?", userId).fetchOne(Database) ?: return null
		return mapRow(row).also { loadedRequests.add(it) }
	}

	fun addRequest(request: JoinRequestData) {
		loadedRequests.add(request.copy(saved = false))
	}

	fun acceptRequest(userId: String) {
		val request = getByUser(userId) ?: return
		val guild = GuildManager.loadedGuilds.find { it.name == request.guildName } ?: return
		PlayerManager.findOnlineByUUID(userId)?.sendSystemMessage(Feedback.build("Your request to join the ${guild.name} guild has been approved."))
		val applicantName = PlayerManager.getByUUID(userId)?.name ?: userId
		PlayerManager.findOnlineByUUID(guild.ownerId)?.sendSystemMessage(Feedback.build("$applicantName has been approved to join the ${guild.name} guild."))
		guild.playerIds.add(userId)
		GuildManager.updateGuild(guild)
		deleteRequest(request)
	}

	fun deleteRequest(request: JoinRequestData) {
		loadedRequests.remove(request)
		JoinRequestsTable.delete(Database) {
			where("id = ?", request.id)
		}
	}

	fun loadAll() {
		loadedRequests.clear()
		JoinRequestsTable.select().fetch(Database).forEach { row -> loadedRequests.add(mapRow(row)) }
	}

	fun saveAll() {
		loadedRequests.filter { !it.saved }.forEach { request ->
			val success = JoinRequestsTable.upsert(Database) {
				set(JoinRequestsTable.id, request.id)
				set(JoinRequestsTable.userId, request.userId)
				set(JoinRequestsTable.guildName, request.guildName)
			}
			if (success) request.saved = true
		}
	}

	private fun mapRow(row: ResultRow): JoinRequestData = JoinRequestData(
		id = row.get(JoinRequestsTable.id),
		userId = row.get(JoinRequestsTable.userId),
		guildName = row.get(JoinRequestsTable.guildName),
		saved = true
	)
}
