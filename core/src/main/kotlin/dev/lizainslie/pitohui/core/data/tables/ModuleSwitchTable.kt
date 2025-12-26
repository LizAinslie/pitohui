package dev.lizainslie.pitohui.core.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

// inb4 this shit catches on fire the first time i run it

// i FUCKING CALLED IT lmao

// it broke even more past me never realized how right it was,,

// u can ignore above now sometimes the retard can cook <3

// i fucked it up and fixed it again do NOT use uniqueIndex() !!

object ModuleSwitchTable : CompositeIdTable("module_switches") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("platform_id", 255).entityId()
    val moduleName = varchar("module_name", 255).entityId()
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(platform, communityId, moduleName)
}