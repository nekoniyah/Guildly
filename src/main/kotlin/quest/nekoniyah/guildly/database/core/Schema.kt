package quest.nekoniyah.guildly.database.core

/**
 * Defines the schema for a single datable table, including all its column definitions.
 *
 * Use the [table] DSL builder to create instances:
 * ```kt
 * val GuildsTable = table("guilds") {
 *  text("name", primaryKey = true)
 *  text("ownerId")
 *  text("playerIds")
 * }
 * ```
 *
 * @property tableName The SQL name of the table.
 * @property columns The ordered list of column definitions.
 */
open class Schema(val tableName: String) {
	val columns: MutableList<Column<*>> = mutableListOf()

	/**
	 * Registers a TEXT column.
	 *
	 * @param name Column name
	 * @param primaryKey Is a primary key ?
	 * @param nullable Is it NULLABLE ?
	 * @param default Optional DEFAULT value expression
	 */
	fun text(name: String, primaryKey: Boolean = false, nullable: Boolean = false, default: String? = null): Column<String> {
		val column = Column<String>(name, "TEXT", primaryKey, nullable, defaultValue = default)
		columns.add(column)
		return column
	}

	/**
	 * Registers a INTEGER column.
	 *
	 * @param name Column name
	 * @param primaryKey Is a primary key ?
	 * @param autoIncrement Does it auto-increment ?
	 * @param nullable Is it NULLABLE ?
	 * @param default Optional DEFAULT value expression
	 */
	fun integer(name: String, primaryKey: Boolean = false, autoIncrement: Boolean = false, nullable: Boolean = false, default: String? = null): Column<Int> {
		val column = Column<Int>(name, "INTEGER", primaryKey, autoIncrement, nullable, default)
		columns.add(column)
		return column
	}

	/**
	 * Registers a REAL (floating-point) column.
	 *
	 * @param name Column name
	 * @param nullable Is it NULLABLE ?
	 * @param default Optional DEFAULT value expression
	 */
	fun real(name: String, nullable: Boolean = false, default: String? = null): Column<Double> {
		val column = Column<Double>(name, "REAL", nullable, defaultValue = default)
		columns.add(column)
		return column
	}

	/**
	 * Generates the full `CREATE TABLE IF NOT EXISTS ...` SQL statement for this schema.
	 *
	 * @throws IllegalStateException if no columns have been defined.
	 */
	fun toCreateSQL(): String {
		check(columns.isNotEmpty()) { "Table '$tableName' must have at least one column." }
		val sqlColumns = columns.joinToString(", ") { it.toSQL() }
		return "CREATE TABLE IF NOT EXISTS $tableName ($sqlColumns);"
	}

	/** Generates the `DROP TABLE IF EXSITS ...` SQL statement. */
	fun toDropSQL(): String = "DROP TABLE IF EXISTS $tableName;"
}

/**
 * DSL entry point for defining a table schema.
 *
 * ```kt
 * val UsersTable = table("users") {
 *  text("id", primaryKey = true)
 *  text("name")
 *  integer("age", nullable = true)
 * }
 * ```
 *
 * @param name The SQL table name
 * @param block The schema definition block
 * @return The fully configured [Schema] instance
 */
fun table(name: String, block: Schema.() -> Unit): Schema = Schema(name).apply(block)
