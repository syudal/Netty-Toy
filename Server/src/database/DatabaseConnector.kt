package database

import server.Setting

class DatabaseConnector private constructor(
    dbName: String,
    serverName: String,
    user: String,
    passwd: String,
    port: Int
) :
    DatabaseConnectionPool(dbName, serverName, user, passwd, port) {
    companion object {
        @Volatile
        private var instance: DatabaseConnector? = null

        fun getInstance(): DatabaseConnector {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = DatabaseConnector(
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
    }
}