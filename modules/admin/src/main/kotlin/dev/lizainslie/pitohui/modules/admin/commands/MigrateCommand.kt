package dev.lizainslie.pitohui.modules.admin.commands

import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.DbContext

val MigrateCommand = defineCommand(
    name = "migrate",
    description = "Migrate the database to the latest version",
) {
    handle {
        DbContext.migrate()
        respond("Database migrated to the latest version.")
    }
}
