package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.config.Configs
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object DbContext {
    val config by Configs.config<DatabaseConfig>()

    val tables = mutableSetOf<Table>()

    fun connect() {
        Database.connect(
            url = config.url,
            driver = "org.postgresql.Driver",
            user = config.user,
            password = config.password,
        )
    }

    fun generateMigrations(): List<String> =
        transaction {
            val allStatements =
                MigrationUtils.statementsRequiredForDatabaseMigration(*tables.toTypedArray(), withLogs = true)

            val migrationScripts = mutableListOf<String>()

            // Append statements
            allStatements.forEach { statement ->
                // Add semicolon only if it's not already there
                val conditionalSemicolon = if (statement.last() == ';') "" else ";"

                migrationScripts += "$statement$conditionalSemicolon\n"
            }

            migrationScripts
        }

    fun migrate() {
        val migrations = generateMigrations()
        transaction {
            migrations.forEach { statement ->
                // Execute each statement
                exec(statement)
            }
        }
    }
}
