package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.logging.logTag
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DbContext {
    val config by Configs.config<DatabaseConfig>()

    val tables = mutableSetOf<Table>()

    private val log = LoggerFactory.getLogger(javaClass)

    fun connect() {
        log.info("Connecting to database")
        Database.connect(
            url = config.url,
            driver = "org.postgresql.Driver",
            user = config.user,
            password = config.password,
        )
    }

    fun generateMigrations(tablesToMigrate: Set<Table> = tables): List<String> =
        transaction {
            log.info("Generating migration scripts for ${tablesToMigrate.size} tables")
            val allStatements =
                MigrationUtils.statementsRequiredForDatabaseMigration(*tablesToMigrate.toTypedArray(), withLogs = true)

            val migrationScripts = mutableListOf<String>()

            // Append statements
            allStatements.forEach { statement ->
                // Add semicolon only if it's not already there
                val conditionalSemicolon = if (statement.last() == ';') "" else ";"

                migrationScripts += "$statement$conditionalSemicolon\n"
            }

            migrationScripts
        }

    fun migrate(tablesToMigrate: Set<Table> = tables) {
        val migrations = generateMigrations(tablesToMigrate)
        transaction {
            log.info("Applying ${migrations.size} migration statements to database")
            migrations.forEach { statement ->
                // Execute each statement
                logTag("sql") {
                    log.debug("Executing migration statement: {}", statement)
                    exec(statement)
                }
            }
        }
    }
}
