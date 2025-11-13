package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class ModuleSwitch(id: EntityID<CompositeID>) : CompositeEntity(id) {
    var communityId by ModuleSwitchTable.communityId
    var platform by ModuleSwitchTable.platform
    var moduleName by ModuleSwitchTable.moduleName
    var enabled by ModuleSwitchTable.enabled

    companion object : CompositeEntityClass<ModuleSwitch>(ModuleSwitchTable) {
        fun isModuleEnabled(
            communityId: String,
            platform: PlatformKey,
            moduleName: String
        ) = find {
            (ModuleSwitchTable.communityId eq communityId) and
            (ModuleSwitchTable.platform eq platform.key) and
            (ModuleSwitchTable.moduleName eq moduleName)
        }.firstOrNull()?.enabled ?: false

        fun isModuleEnabled(
            communityId: PlatformId,
            moduleName: String
        ) = isModuleEnabled(
            communityId.id,
            communityId.platform,
            moduleName
        )

        fun getSwitch(
            communityId: PlatformId,
            moduleName: String
        ): ModuleSwitch? = find {
            (ModuleSwitchTable.communityId eq communityId.id) and
            (ModuleSwitchTable.platform eq communityId.platform.key) and
            (ModuleSwitchTable.moduleName eq moduleName)
        }.firstOrNull()

        fun createSwitch(
            communityId: PlatformId,
            moduleName: String,
            enabled: Boolean = true
        ): ModuleSwitch {
            return new(CompositeID { id ->
                id[ModuleSwitchTable.communityId] = communityId.id
                id[ModuleSwitchTable.platform] = communityId.platform.key
                id[ModuleSwitchTable.moduleName] = moduleName
            }) {
                this.enabled = enabled
            }
        }
    }
}

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
