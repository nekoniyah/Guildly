package quest.nekoniyah.guildly.database.core

/**
 * Type-safe wrapper around a raw JDBC result row (Map<String, Any>)
 *
 * Provides convenient typed accessors to avoid unsafe manual casting
 */
class ResultRow(val raw: Map<String, Any>) {
	/**
	 * Reads a non-null value from this row, casting it to [T].
	 *
	 * @param column The [Column] descriptor to read from.
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T: Any> get(column: Column<T>): T = raw[column.name] as T

	/**
	 * Reads a nullable value from this row, returning null if absent or NULL.
	 *
	 * @param column The [Column] descriptor to read from.
	 */
	@Suppress("UNCHECKED_CAST")
	fun <T: Any> getOrNull(column: Column<T>): T? = raw[column.name] as? T

	/**
	 * Returns the raw string value for a column by name, or null if missing.
	 *
	 * Useful for generic/utility contexts where no [Column] reference is available.
	 */
	fun getString(name: String): String? = raw[name] as? String

	/** Returns the raw int value for a column by name, or null if missing. */
	fun getInt(name: String): Int? = raw[name] as? Int
}
