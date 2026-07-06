package quest.nekoniyah.guildly.utils.database

import quest.nekoniyah.guildly.Guildly
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object Database {
    var connection: Connection? = null

    fun init(): Connection {
        // Ensure parent directories exist before SQLite connection
        val dbFile = java.io.File("./${Guildly.ID}/data.db")
        dbFile.parentFile?.mkdirs()

        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

        return connection!!
    }

    /**
     * Executes a SELECT query and safely maps rows into a list of key-value maps.
     * This avoids leaking open ResultSet cursors or statements.
     */
    fun query(sql: String, args: Array<out Any> = emptyArray()): List<Map<String, Any>> {
        val results = mutableListOf<Map<String, Any>>()
        val conn = connection ?: return emptyList()

        try {
            conn.prepareStatement(sql).use { stmt ->
                // Properly map arguments using 1-based indexing
                args.forEachIndexed { index, arg ->
                    stmt.setObject(index + 1, arg)
                }

                stmt.executeQuery().use { rs ->
                    val metaData = rs.metaData
                    val columnCount = metaData.columnCount

                    while (rs.next()) {
                        val row = mutableMapOf<String, Any>()
                        for (i in 1..columnCount) {
                            val columnName = metaData.getColumnName(i)
                            row[columnName] = rs.getObject(i) ?: ""
                        }
                        results.add(row)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace() // Log errors instead of completely swallowing them
        }
        return results
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE query.
     */
    fun exec(sql: String, args: Array<out Any> = emptyArray()): Boolean {
        val conn = connection ?: return false
        return try {
            conn.prepareStatement(sql).use { stmt ->
                args.forEachIndexed { index, arg ->
                    stmt.setObject(index + 1, arg)
                }
                stmt.executeUpdate()
                true
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }
}