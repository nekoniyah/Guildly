package quest.nekoniyah.guildly.utils.database

class Table(val name: String) {
    // Fix: Initialize with a truly mutable Kotlin list to avoid UnsupportedOperationException
    var fields: MutableList<TableField> = mutableListOf()

    fun setFields(vararg fields: TableField): Table {
        this.fields = fields.toMutableList()
        return this
    }

    fun addField(field: TableField): Table {
        this.fields.add(field)
        return this
    }

    fun create(): Boolean {
        // Guard clause: You can't create a table without columns
        if (fields.isEmpty()) {
            throw IllegalStateException("Cannot create table '$name' without any fields.")
        }

        // Map fields to their SQL definitions (assuming TableField has a toSql() or similar method)
        // Example output: "id INT PRIMARY KEY, name TEXT"
        val columnsSql = fields.joinToString(", ") { it.toSql() }

        // Fix: Added the 'S' to EXISTS and injected the columns
        val sql = "CREATE TABLE IF NOT EXISTS $name ($columnsSql);"

        return Database.exec(sql)
    }

    fun drop(): Boolean {
        return Database.exec("DROP TABLE IF EXISTS $name;")
    }
}