package quest.nekoniyah.guildly.database.cache

import net.minecraft.server.level.ServerPlayer

/**
 * In-memory cache of currently online [net.minecraft.server.level.ServerPlayer] instances.
 *
 * Populated on [net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent] and cleared on logout.
 * Using [ServerPlayer] instead of the base [net.minecraft.world.entity.player.Player] gives access to
 * server-side methods like [ServerPlayer.sendSystemMessage] without casting.
 *
 * All access is from the main server thread, so no synchronization is needed.
 */
object PlayerCache {
	private val _players: MutableList<ServerPlayer> = mutableListOf()
	/** Read-only view of the current online player list. */
	val players: List<ServerPlayer> get() = _players

	/**
	 * Adds a player to the cache when they connect.
	 *
	 * @param player The [ServerPlayer] who just logged in.
	 */
	fun add(player: ServerPlayer) { _players.add(player) }

	/**
	 * Removes a player from the cache when they disconnect.
	 *
	 * @param player The [ServerPlayer] who just logged out.
	 */
	fun remove(player: ServerPlayer) { _players.removeIf { it.stringUUID == player.stringUUID } }

	/**
	 * Finds a cached player by their UUID string.
	 *
	 * @param uuid The player's string UUID.
	 * @return The matching [ServerPlayer], or null if not online.
	 */
	fun findByUUID(uuid: String): ServerPlayer? = _players.find { it.stringUUID == uuid }

	/**
	 * Finds a cached player by their display name string.
	 *
	 * @param name The player's display name.
	 * @return The matching [ServerPlayer], or null if not online.
	 */
	fun findByName(name: String): ServerPlayer? = _players.find { it.displayName?.string == name }
}
