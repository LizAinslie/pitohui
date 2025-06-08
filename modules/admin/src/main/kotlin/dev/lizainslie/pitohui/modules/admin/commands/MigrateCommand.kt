package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.defineCommand

val MigrateCommand = defineCommand(
    name = "migrate",
    description = "Migrate the database to the latest version",
) {
    handle { context ->
        context.bot.db.migrate()
        context.respond("Database migrated to the latest version.")
    }
}