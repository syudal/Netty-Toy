package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection


open class DatabaseConnector {
    private lateinit var dataSource: HikariDataSource
    private val user: String
    private val password: String
    private val dbName: String
    private val serverName: String
    private val port: Int

    constructor(dbName: String, user: String, passwd: String) {
        this.dbName = dbName
        this.serverName = DEFAULT_HOST
        this.user = user
        this.password = passwd
        this.port = DEFAULT_PORT
    }

    constructor(dbName: String, serverName: String, user: String, passwd: String, port: Int) {
        this.dbName = dbName
        this.serverName = serverName
        this.user = user
        this.password = passwd
        this.port = port
    }

    fun load() {
        val config = HikariConfig()
        // DB Config
        config.jdbcUrl = String.format("jdbc:%s://%s:%d/%s", DRIVER_MARIADB, serverName, port, dbName)

        config.username = user
        config.password = password

        // DB Options
        config.maximumPoolSize = MAXIMUM_POOL_SIZE
        config.isAutoCommit = true

        config.addDataSourceProperty("characterEncoding", "utf8")
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.addDataSourceProperty("autoReconnect", "true")

        dataSource = HikariDataSource(config)
    }

    fun poolConnection(): Connection {
        return dataSource.connection
    }

    fun close() {
        dataSource.close()
    }

    companion object {
        const val MAXIMUM_POOL_SIZE: Int = 20
        const val DEFAULT_PORT: Int = 3306
        const val DRIVER_MARIADB: String = "mariadb"
        const val DEFAULT_HOST: String = "localhost"
    }
}