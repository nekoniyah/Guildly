package quest.nekoniyah.guildly.database.core

import quest.nekoniyah.guildly.Guildly
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Manages the single SQLite JDBC connection for the mod.
 *
 * Call [init] once during server startup. All query/exec methods
 * are safe to call after that from any context.
 *
 * The connection is intentionally not thread-pooled, Minecraft's
 * main server thread handles all command execution synchronously,
 * so a single connection is sufficient and avoid locking overhead.
 */
object Database {
	/** The active JDBC connection, null until [init] is called. */
	var connection: Connection? = null
		private set

	/**
	 * Initializes the SQLite database connection and ensures the data directory exists.
	 *
	 * Should be called once from [net.neoforged.neoforge.event.server.ServerStartingEvent].
	 *
	 * @return The established [Connection]
	 * @throws IllegalStateException if the connection cannot be established
	 */
	fun init(): Connection {
		val databaseFile = File("./${Guildly.ID}/data.db")
		databaseFile.parentFile?.mkdirs()
		connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
		return connection!!
	}

	/**
	 * Creates a table in the database from a [Schema] definition if it does not already exist.
	 *
	 * @param schema The [Schema] to create.
	 * @return `true` if the CREATE TABLE statement executed successfully.
	 */
	fun createTable(schema: Schema): Boolean = exec(schema.toCreateSQL())

	/**
	 * Drops a table from the database if it exists.
	 *
	 * @param schema The [Schema] to drop.
	 * @return `true` if the DROP TABLE statement executed successfully.
	 */
	fun dropTable(schema: Schema): Boolean = exec(schema.toDropSQL())

	/**
	 * Executes a SELECT SQL query with optional positional parameters.
	 *
	 * Fully closes the [ResultSet] and [java.sql.PreparedStatement] after reading to prevent cursor leaks.
	 *
	 * @param sql The SELECT SQL string with optional `?` placeholders.
	 * @param args The values for each `?` placeholder, in order.
	 * @return A list of rows, where each row is a map of column name -> raw value.
	 */
	fun query(sql: String, args: Array<out Any> = emptyArray()): List<Map<String, Any>> {
		val results = mutableListOf<Map<String, Any>>()
		val connection = connection ?: return emptyList()
		try {
			connection.prepareStatement(sql).use { statement ->
				args.forEachIndexed { index, arg -> statement.setObject(index + 1, arg) }
				statement.executeQuery().use { resultSet ->
					val meta = resultSet.metaData
					val columnCount = meta.columnCount
					while (resultSet.next()) {
						val row = mutableMapOf<String, Any>()
						for (index in 1..columnCount) row[meta.getColumnName(index)] = resultSet.getObject(index) ?: ""
						results.add(row)
					}
				}
			}
		} catch (exception: Exception) {
			Guildly.LOGGER.error("[Database] Query failed: $sql", exception)
		}
		return results
	}

	/**
	 * Executes a mutating SQL statement (INSERT, UPDATE, DELETE, CREATE, DROP).
	 *
	 * @param sql The SQL string with optional `?` placeholders.
	 * @param args The values for each `?` placeholder, in order.
	 * @return `true` if the statement executed without error, `false` otherwise.
	 */
	fun exec(sql: String, args: Array<out Any> = emptyArray()): Boolean {
		val connection = connection ?: return false
		return try {
			connection.prepareStatement(sql).use { statement ->
				args.forEachIndexed { index, arg -> statement.setObject(index + 1, arg) }
				statement.executeUpdate()
				true
			}
		} catch (sqlException: SQLException) {
			Guildly.LOGGER.error("[Database] Exec failed: $sql", sqlException)
			false
		}
	}
}
