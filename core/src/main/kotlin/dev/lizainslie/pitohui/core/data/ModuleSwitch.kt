package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class ModuleSwitch(id: EntityID<CompositeID>) : CompositeEntity(id) {
    var platformId by ModuleSwitchTable.platformId
    var platform by ModuleSwitchTable.platform
    var moduleName by ModuleSwitchTable.moduleName
    var enabled by ModuleSwitchTable.enabled

    companion object : CompositeEntityClass<ModuleSwitch>(ModuleSwitchTable) {
        fun isModuleEnabled(
            platformId: String,
            platform: Platforms,
            moduleName: String
        ) = find {
            (ModuleSwitchTable.platformId eq platformId) and
            (ModuleSwitchTable.platform eq platform) and
            (ModuleSwitchTable.moduleName eq moduleName)
        }.firstOrNull()?.enabled ?: false

        fun isModuleEnabled(
            platformId: PlatformId,
            moduleName: String
        ) = isModuleEnabled(
            platformId.id,
            platformId.platform,
            moduleName
        )

        fun getSwitch(
            platformId: PlatformId,
            moduleName: String
        ): ModuleSwitch? = find {
            (ModuleSwitchTable.platformId eq platformId.id) and
            (ModuleSwitchTable.platform eq platformId.platform) and
            (ModuleSwitchTable.moduleName eq moduleName)
        }.firstOrNull()

        fun createSwitch(
            platformId: PlatformId,
            moduleName: String,
            enabled: Boolean = true
        ): ModuleSwitch {
            return new(CompositeID { id ->
                id[ModuleSwitchTable.platformId] = platformId.id
                id[ModuleSwitchTable.platform] = platformId.platform
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

object ModuleSwitchTable : CompositeIdTable("module_switches") {
    val platform = enumerationByName<Platforms>("platform", 32).entityId()
    val platformId = varchar("platform_id", 255).uniqueIndex().entityId()
    val moduleName = varchar("module_name", 255).entityId()
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(platform, platformId, moduleName)
}