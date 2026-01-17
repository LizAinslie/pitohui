package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.core.data.DbContext
import dev.lizainslie.moeka.platforms.discord.Discord

val MigrateCommand =
    defineCommand(
        name = "migrate",
        description = "Migrate the database to the latest version",
    ) {
        platform(Discord)

        handle {
            DbContext.migrate()
            respond("Database migrated to the latest version.")
        }
    }
