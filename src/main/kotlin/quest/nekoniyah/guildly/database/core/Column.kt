package quest.nekoniyah.guildly.database.core

/**
 * A single column in a table schema.
 *
 * @param T Type it maps to
 * @property name SQL column name
 * @property sqlType Raw SQL type string ("TEXT", "INTEGER", etc.)
 * @property primaryKey Is a primary key ?
 * @property autoIncrement Auto-increments ? (INTEGER only)
 * @property nullable Accept NULL values ?
 * @property defaultValue Optional SQL-level default value expression
 */
data class Column<T: Any>(
	val name: String,
	val sqlType: String,
	val primaryKey: Boolean = false,
	val autoIncrement: Boolean = false,
	val nullable: Boolean = false,
	val defaultValue: String? = null,
) {
	fun toSQL(): String = buildString {
		append("$name $sqlType")
		if (primaryKey) append(" PRIMARY KEY")
		if (autoIncrement) append(" AUTOINCREMENT")
		if (nullable) append(" NOT NULL")
		if (defaultValue != null) append(" DEFAULT $defaultValue")
	}
}
