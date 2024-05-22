package database

import server.Setting
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class Database private constructor(
    dbName: String, serverName: String, user: String, passwd: String, port: Int
) : DatabaseConnector(dbName, serverName, user, passwd, port) {
    companion object {
        @Volatile
        private var instance: Database? = null

        fun getInstance(): Database {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = Database(
                            Setting().dbName,
                            Setting().serverName,
                            Setting().dbUser,
                            Setting().dbPasswd,
                            Setting().dbPort
                        )
                    }
                }
            }
            return instance!!
        }

        private fun bind(propSet: PreparedStatement, vararg commands: Any?): Int {
            for (i in 1..commands.size) {
                val command = commands[i - 1]
                if (command != null) {
                    try {
                        when (command) {
                            is Number -> {
                                // Specific to only setByte calls, default Integer
                                when (command) {
                                    is Byte -> propSet.setByte(i, command)
                                    is Short -> propSet.setShort(i, command)

                                    // Specific to only setLong calls, default Integer
                                    is Long -> propSet.setLong(i, command)
                                    is Double -> propSet.setDouble(i, command)

                                    // Almost all types are INT(11), so default to this
                                    else -> propSet.setInt(i, command.toInt())
                                }
                                // If it is otherwise a String, we only require setString
                            }

                            is String -> propSet.setString(i, command)
                            is Boolean -> propSet.setBoolean(i, command)
                            else -> {}
                        }
                    } catch (ex: SQLException) {
                        ex.printStackTrace(System.err)
                    }
                } else {
                    return -4
                }
            }
            return 1
        }

        fun execute(con: Connection, propSet: PreparedStatement?, vararg commands: Any): Int {
            if (propSet != null) {
                val result = bind(propSet, *commands)

                if (result > 0) {
                    val rowsAffected = propSet.executeUpdate()
                    if (rowsAffected == 0) {
                        var query = propSet.toString()
                        // The only valid DML statement for re-insertion is UPDATE.
                        if (!query.contains("DELETE FROM") && !query.contains("INSERT INTO")) {
                            // Substring based on if the query contains '?' IN params or not
                            query = if (query.contains("', parameters")) query.substring(
                                query.indexOf("UPDATE"),
                                query.indexOf("', parameters")
                            )
                            else query.substring(query.indexOf("UPDATE"))

                            // Begin the new query, starting by converting an update to an insert
                            var newQuery = StringBuilder(query.replace("UPDATE", "INSERT INTO"))

                            // Substring the FRONT rows (prior to WHERE condition)
                            val rows: String = if (newQuery.toString()
                                    .contains("WHERE")
                            ) newQuery.substring(newQuery.indexOf("SET ") + "SET ".length, newQuery.indexOf("WHERE "))
                            else newQuery.substring(newQuery.indexOf("SET ") + "SET ".length)

                            // Construct an array of every front row
                            val frontRows =
                                rows.replace(" = ?, ", ", ").replace(" = ? ", ", ").split(", ").toTypedArray()
                            // Not all queries perform an UPDATE with a WHERE condition, allocate empty back rows
                            var backRows = arrayOf<String>()
                            // If the query does contain a WHERE condition, parse the back rows (everything after WHERE)
                            if (newQuery.toString().contains("WHERE")) {
                                val backRowsString = newQuery.substring(newQuery.indexOf("WHERE ") + "WHERE ".length)
                                backRows = backRowsString.replace(" = ? AND ", ", ").replace(" = ?", ", ").split(", ")
                                    .toTypedArray()
                            }
                            // Merge the front and back rows into one table, these are all columns being inserted
                            val rowData = arrayOfNulls<String>(frontRows.size + backRows.size)
                            System.arraycopy(frontRows, 0, rowData, 0, frontRows.size)
                            System.arraycopy(backRows, 0, rowData, frontRows.size, backRows.size)

                            // Begin transforming the query - clear the rest of the string, transform to (Col1, Col2, Col3)
                            newQuery = StringBuilder(newQuery.substring(0, newQuery.indexOf("SET ")))
                            newQuery.append("(")
                            for (row in rowData) {
                                newQuery.append(row).append(", ")
                            }
                            // Trim the remaining , added at the end of the last column
                            newQuery = StringBuilder(newQuery.substring(0, newQuery.length - ", ".length))

                            // Begin appending the VALUES(?, ?) for the total size there is rows
                            newQuery.append(") VALUES(")
                            for (row in rowData) {
                                newQuery.append("?, ")
                            }
                            // Trim the remaining , added at the end of the last column
                            newQuery = StringBuilder(newQuery.substring(0, newQuery.length - ", ".length))
                            newQuery.append(")")

                            return execute(con, con.prepareStatement(newQuery.toString()), *commands)
                        }
                    }
                    return rowsAffected
                }
                return result
            }
            return -1
        }
    }
}