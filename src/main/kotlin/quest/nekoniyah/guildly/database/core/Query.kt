package quest.nekoniyah.guildly.database.core

/**
 * A fluent, immutable SELECT query builder for a single table.
 *
 * Construct via [Schema.select] or [Schema.selectAll]
 *
 * ```kotlin
 * val results = GuildsTable.select()
 *  .where("name = ?", guildName)
 *  .limit(1)
 *  .fetch(database)
 * ```
 *
 * @property schema The [Schema] being queried.
 */
class Query(private val schema: Schema) {
	private var whereClause: String? = null
	private var whereArgs: Array<out Any> = emptyArray()
	private var limitValue: Int? = null
	private var orderByClause: String? = null

	/**
	 * Adds a WHERE clause to the query.
	 *
	 * @param clause The SQL condition string with `?` placeholders.
	 * @param args The value for each `?` placeholder, in order.
	 */
	fun where(clause: String, vararg args: Any): Query {
		whereClause = clause
		whereArgs = args
		return this
	}

	/**
	 * Limits the number of rows returned.
	 *
	 * @param n Maximum number of rows.
	 */
	fun limit(n: Int): Query {
		limitValue = n
		return this
	}

	/**
	 * Adds an ORDER BY clause.
	 *
	 * @param clause The column(s) and direction, e.g. `"name ASC"`
	 */
	fun orderBy(clause: String): Query {
		orderByClause = clause
		return this
	}

	/** Builds the final SQL SELECT string from the configured clauses. */
	fun toSQL(): String = buildString {
		append("SELECT * FROM ${schema.tableName}")
		if (whereClause != null) append(" WHERE $whereClause")
		if (orderByClause != null) append(" ORDER BY $orderByClause")
		if (limitValue != null) append(" LIMIT $limitValue")
	}

	/**
	 * Executes the query against the provided [Database] and returns all matching rows.
	 *
	 * @param database The [Database] connection to execute against.
	 * @return A list of [ResultRow] wrappers, one per matched row.
	 */
	fun fetch(database: Database): List<ResultRow> = database.query(toSQL(), whereArgs).map { ResultRow(it) }

	/**
	 * Executes the query and returns the first matching row, or null if none.
	 *
	 * @param database The [Database] connection to execute against.
	 */
	fun fetchOne(database: Database): ResultRow? = limit(1).fetch(database).firstOrNull()
}

/** Creates a [Query] builder for this schema. */
fun Schema.select(): Query = Query(this)
