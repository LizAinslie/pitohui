package dev.lizainslie.pitohui.core.data.entities

import dev.lizainslie.pitohui.core.data.tables.ModuleSwitchTable
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
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
            return new(CompositeID.Companion { id ->
                id[ModuleSwitchTable.communityId] = communityId.id
                id[ModuleSwitchTable.platform] = communityId.platform.key
                id[ModuleSwitchTable.moduleName] = moduleName
            }) {
                this.enabled = enabled
            }
        }
    }
}