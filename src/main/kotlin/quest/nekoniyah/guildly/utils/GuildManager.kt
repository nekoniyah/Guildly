package quest.nekoniyah.guildly.utils

import kotlinx.serialization.json.Json
import java.io.File

object GuildManager {
    const val GUILDS_FOLDER = "./guildly/guilds"
    val loadedGuilds: MutableSet<GuildData> = mutableSetOf()
    var databaseFile: File = File(GUILDS_FOLDER)

    fun getGuildData(name: String): GuildData? {
        val file = File("$GUILDS_FOLDER/$name.json")
        if (!file.exists()) return null

        val content = file.reader().readText()
        val data = Json.decodeFromString<GuildData>(content)
        return data
    }

    fun exists(name: String): Boolean {
        val cachedGuild = loadedGuilds.find { guild ->
            guild.name == name
        }

        if (cachedGuild != null) return true

        val file = File("$GUILDS_FOLDER/$name.json")
        return file.exists()
    }

    fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]*$"))
    }

    init {
        if (!databaseFile.exists()) databaseFile.mkdirs()
    }

    fun loadAll() {
        loadedGuilds.clear()
        databaseFile.listFiles().forEach { file ->
            if (file.name.endsWith(".json")) {
                val guild =  getGuildData(file.name.replace(".json", "")) ?: return
                loadedGuilds.add(guild)
            }
        }
    }

    fun saveAll() {
        loadedGuilds.forEach { guild ->
            val json = Json.encodeToString(guild)
            File("${GUILDS_FOLDER}/${guild.name}.json").writeText(json)
        }
    }
}