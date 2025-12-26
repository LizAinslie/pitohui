package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.DbContext
import dev.lizainslie.pitohui.platforms.discord.Discord

val MigrateCommand = defineCommand(
    name = "migrate",
    description = "Migrate the database to the latest version",
) {
    platform(Discord)

    handle {
        DbContext.migrate()
        respond("Database migrated to the latest version.")
    }
}
