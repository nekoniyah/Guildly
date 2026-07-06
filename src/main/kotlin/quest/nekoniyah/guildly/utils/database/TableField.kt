package quest.nekoniyah.guildly.utils.database

data class TableField(
   val key: String,
   val type: String,
   val autoincrement: Boolean = false,
   val primaryKey: Boolean = false,
) {
   /**
    * Converts the field properties into an SQL column definition fragment.
    * e.g., "id INT PRIMARY KEY AUTOINCREMENT" or "username TEXT"
    */
   fun toSql(): String {
      val sb = StringBuilder("$key $type")

      if (primaryKey) {
         sb.append(" PRIMARY KEY")
      }

      if (autoincrement) {
         sb.append(" AUTOINCREMENT")
      }

      return sb.toString()
   }
}