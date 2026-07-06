package quest.nekoniyah.guildly.utils.database.joinrequest

import net.minecraft.server.MinecraftServer
import quest.nekoniyah.guildly.database.tables.JoinRequestsTable
import quest.nekoniyah.guildly.utils.Cache
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.database.Database
import quest.nekoniyah.guildly.utils.database.guild.GuildManager

object JoinRequestManager {
    var loadedRequests: MutableSet<JoinRequestData> = mutableSetOf()

    init {
        JoinRequestsTable.create()
    }

    fun getData(id: String): JoinRequestData? {
        // Check cache first
        val cached = loadedRequests.find { r -> r.userId == id }
        if (cached != null) return cached

        // Fallback to SQLite query
        val results = Database.query("SELECT * FROM join_requests WHERE userId = ?", arrayOf(id))
        if (results.isEmpty()) return null

        val data = mapRowToJoinRequest(results.first())
        loadedRequests.add(data)
        return data
    }

    fun addRequest(request: JoinRequestData) {
        loadedRequests.add(request)
        request.saved = false // Mark it dirty so saveAll() catches it
    }

    fun acceptRequest(server: MinecraftServer, id: String) {
        val data = getData(id) ?: return
        val onlinePlayer = server.playerList.players.find { p -> p.stringUUID == id }

        if (onlinePlayer != null) {
            val msg = Feedback.build("Your request to join the ${data.guildName} guild has been approved.")
            onlinePlayer.sendSystemMessage(msg)
        }

        val foundGuild = GuildManager.loadedGuilds.find { g ->
            g.name == data.guildName
        } ?: return

        val owner = server.playerList.players.find { p -> p.stringUUID == foundGuild.ownerId }

        if (owner != null) {
            val cachedPlayer = Cache.players.find { it.stringUUID == id } ?: return
            val msg = Feedback.build("${cachedPlayer.displayName!!.string} has been approved to join the ${data.guildName} guild.")
            owner.sendSystemMessage(msg)
        }

        foundGuild.playerIds.add(data.userId)
        GuildManager.updateGuild(foundGuild)

        // Clean up the request after it's accepted
        deleteRequest(data)
    }

    fun deleteRequest(request: JoinRequestData) {
        loadedRequests.remove(request)
        Database.exec("DELETE FROM join_requests WHERE id = ?", arrayOf(request.id))
    }

    fun loadAll() {
        loadedRequests.clear()
        val results = Database.query("SELECT * FROM join_requests")
        results.forEach { row ->
            loadedRequests.add(mapRowToJoinRequest(row))
        }
    }

    fun saveAll() {
        loadedRequests.forEach { request ->
            if (request.saved) return@forEach

            val sql = """
                INSERT INTO join_requests (id, userId, guildName) VALUES (?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET 
                    userId = excluded.userId, 
                    guildName = excluded.guildName;
            """.trimIndent()

            val success = Database.exec(sql, arrayOf(request.id, request.userId, request.guildName))
            if (success) {
                request.saved = true
            }
        }
    }

    // Maps SQLite primitive types back into the JoinRequestData entity wrapper
    private fun mapRowToJoinRequest(row: Map<String, Any>): JoinRequestData {
        return JoinRequestData(
            id = row["id"] as String,
            userId = row["userId"] as String,
            guildName = row["guildName"] as String,
            saved = true
        )
    }
}