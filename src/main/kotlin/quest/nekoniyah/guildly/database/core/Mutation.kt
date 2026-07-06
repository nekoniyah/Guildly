package quest.nekoniyah.guildly.database.core

/**
 * A fluent builder for INSERT, UPDATE, and DELETE mutations on a single table.
 *
 * Construct via the DSL helpers [Schema.insert], [Schema.upsert], [Schema.update], [Schema.delete]
 *
 * ```kotlin
 * GuildsTable.upsert(database) {
 *  set(GuildsTable.name, "NekoGuild")
 *  set(GuildsTable.ownerId, uuid)
 *  set(GuildsTable.playerIds, "[]")
 * }
 * ```
 *
 * @property schema The target [Schema]
 * @property mode The mutation mode: INSERT, UPDATE, UPSERT (INSERT OR REPLACE), UPDATE, or DELETE
 */
class Mutation(private val schema: Schema, private val mode: Mode) {
	/** Supported mutation modes. */
	enum class Mode { INSERT, UPDATE, UPSERT, DELETE }

	private val values: MutableMap<String, Any?> = mutableMapOf()
	private var whereClause: String? = null
	private var whereArgs: Array<out Any> = emptyArray()

	/**
	 * Sets the value for a typed column.
	 *
	 * @param column The [Column] descriptor.
	 * @param value The value to assign.
	 */
	fun <T: Any> set(column: Column<T>, value: T?) { values[column.name] = value }

	/**
	 * Adds a WHERE clause (used for UPDATE and DELETE).
	 *
	 * @param clause SQL condition string with `?` placeholders.
	 * @param args Values for each `?` placeholder, in order.
	 */
	fun where(clause: String, vararg args: Any): Mutation {
		whereClause = clause
		whereArgs = args
		return this
	}

	/**
	 * Execute this mutation against the given [Database].
	 *
	 * @param database The [Database] connection.
	 * @return `true` if the operation succeeded, `false` otherwise
	 */
	fun execute(database: Database): Boolean {
		val sql = toSQL()
		val args = buildArgs()
		return database.exec(sql, args)
	}

	private fun toSQL(): String = when (mode) {
		Mode.INSERT -> buildInsertSQL()
		Mode.UPSERT -> buildUpsertSQL()
		Mode.UPDATE -> buildUpdateSQL()
		Mode.DELETE -> buildDeleteSQL()
	}

	private fun buildInsertSQL(): String {
		val columns = values.keys.joinToString(", ")
		val placeholders = values.keys.joinToString(", ") { "?" }
		return "INSERT INTO ${schema.tableName} ($columns) VALUES ($placeholders)"
	}

	private fun buildUpsertSQL(): String {
		val columns = values.keys.joinToString(", ")
		val placeholders = values.keys.joinToString(", ") { "?" }
		val updates = values.keys.joinToString(", ") { "$it = excluded.$it" }
		val pkColumns = schema.columns.firstOrNull { it.primaryKey }?.name ?: error("Upsert on '${schema.tableName} requires a primary key column'")
		return "INSERT INTO ${schema.tableName} ($columns) VALUES ($placeholders) ON CONFLICT($pkColumns) DO UPDATE SET $updates"
	}

	private fun buildUpdateSQL(): String {
		val sets = values.keys.joinToString(", ") { "$it = ?" }
		val base = "UPDATE ${schema.tableName} SET $sets"
		return if (whereClause != null) "$base WHERE $whereClause" else base
	}

	private fun buildDeleteSQL(): String {
		val base = "DELETE FROM ${schema.tableName}"
		return if (whereClause != null) "$base WHERE $whereClause" else base
	}

	private fun buildArgs(): Array<out Any> = when (mode) {
		Mode.INSERT, Mode.UPSERT -> values.values.filterNotNull().toTypedArray()
		Mode.UPDATE -> (values.values.filterNotNull() + whereArgs.toList()).toTypedArray()
		Mode.DELETE -> whereArgs
	}
}

/** DSL shorthand: INSERT a new row */
fun Schema.insert(database: Database, block: Mutation.() -> Unit): Boolean = Mutation(this, Mutation.Mode.INSERT).apply(block).execute(database)
/** DSL shorthand: INSERT OR REPLACE (upsert) a row */
fun Schema.upsert(database: Database, block: Mutation.() -> Unit): Boolean = Mutation(this, Mutation.Mode.UPSERT).apply(block).execute(database)
/** DSL shorthand: UPDATE a row */
fun Schema.update(database: Database, block: Mutation.() -> Unit): Boolean = Mutation(this, Mutation.Mode.UPDATE).apply(block).execute(database)
/** DSL shorthand: DELETE a new row */
fun Schema.delete(database: Database, block: Mutation.() -> Unit): Boolean = Mutation(this, Mutation.Mode.DELETE).apply(block).execute(database)
