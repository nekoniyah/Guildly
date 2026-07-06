package quest.nekoniyah.guildly.utils.database.guild

import kotlinx.serialization.json.Json
import quest.nekoniyah.guildly.database.tables.GuildsTable
import quest.nekoniyah.guildly.utils.database.Database

object GuildManager {
    var loadedGuilds: MutableSet<GuildData> = mutableSetOf()

    init {
        GuildsTable.create()
    }

    fun getGuildData(name: String): GuildData? {
        // Check local cache first
        val cached = loadedGuilds.find { it.name == name }
        if (cached != null) return cached

        // Fallback to SQLite
        val results = Database.query("SELECT * FROM guilds WHERE name = ?", arrayOf(name))
        if (results.isEmpty()) return null

        val data = mapRowToGuildData(results.first())
        loadedGuilds.add(data)
        return data
    }

    fun exists(name: String): Boolean {
        if (loadedGuilds.any { it.name == name }) return true

        val results = Database.query("SELECT COUNT(*) as count FROM guilds WHERE name = ?", arrayOf(name))
        val count = results.firstOrNull()?.get("count") as? Int ?: 0
        return count > 0
    }

    fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]*$"))
    }

    fun deleteFromDatabase(name: String) {
        loadedGuilds.removeIf { it.name == name }
        Database.exec("DELETE FROM guilds WHERE name = ?", arrayOf(name))
    }

    fun updateGuild(guild: GuildData) {
        loadedGuilds.removeIf { g -> g.name == guild.name }
        guild.saved = false
        loadedGuilds.add(guild)
    }

    fun loadAll() {
        loadedGuilds.clear()
        val results = Database.query("SELECT * FROM guilds")
        results.forEach { row ->
            loadedGuilds.add(mapRowToGuildData(row))
        }
    }

    fun saveAll() {
        loadedGuilds.forEach { guild ->
            if (guild.saved) return@forEach

            // Serialize the Player IDs Set into a JSON string format for SQLite
            val playerIdsJson = Json.encodeToString(guild.playerIds)

            val sql = """
                INSERT INTO guilds (name, ownerId, playerIds) VALUES (?, ?, ?)
                ON CONFLICT(name) DO UPDATE SET 
                    ownerId = excluded.ownerId, 
                    playerIds = excluded.playerIds;
            """.trimIndent()

            val success = Database.exec(sql,  arrayOf(guild.name, guild.ownerId, playerIdsJson))
            if (success) {
                guild.saved = true
            }
        }
    }

    // Helper method to reconstruct the GuildData from an SQLite row result
    private fun mapRowToGuildData(row: Map<String, Any>): GuildData {
        val name = row["name"] as String
        val ownerId = row["ownerId"] as String
        val playerIdsRaw = row["playerIds"] as String

        // Deserialize back into a MutableSet
        val playerIds = Json.decodeFromString<MutableSet<String>>(playerIdsRaw)

        return GuildData(
            name = name,
            ownerId = ownerId,
            playerIds = playerIds,
            saved = true
        )
    }
}