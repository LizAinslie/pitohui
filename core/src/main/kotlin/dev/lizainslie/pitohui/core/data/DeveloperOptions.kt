package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class DeveloperOptions(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val platform by DeveloperOptionsTable.platform
    val userId by DeveloperOptionsTable.userId
    var stealth by DeveloperOptionsTable.stealth

    companion object : CompositeEntityClass<DeveloperOptions>(DeveloperOptionsTable) {
        fun isUserDeveloper(
            userId: String,
            platform: PlatformKey
        ) = find {
            (DeveloperOptionsTable.userId eq userId) and
                    (ModuleSwitchTable.platform eq platform.key)
        }.any()

        fun isUserDeveloper(
            userId: PlatformId
        ) = isUserDeveloper(
            userId.id,
            userId.platform
        )

        fun isUserStealth(
            userId: String,
            platform: PlatformKey
        ) = find {
            (DeveloperOptionsTable.userId eq userId) and
                    (ModuleSwitchTable.platform eq platform.key)
        }.firstOrNull()?.stealth ?: false

        fun isUserStealth(
            userId: PlatformId
        ) = isUserStealth(
            userId.id,
            userId.platform
        )

        fun getDeveloperOptions(
            userId: String,
            platform: PlatformKey
        ) = find {
            (DeveloperOptionsTable.userId eq userId) and
                    (ModuleSwitchTable.platform eq platform.key)
        }.firstOrNull()

        fun getDeveloperOptions(
            userId: PlatformId
        ) = getDeveloperOptions(
            userId.id,
            userId.platform
        )
    }
}

object DeveloperOptionsTable : CompositeIdTable("developer_options") {
    val platform = varchar("platform", 32).entityId()
    val userId = varchar("user_id", 255).uniqueIndex().entityId()
    val stealth = bool("stealth").default(false)

    override val primaryKey = PrimaryKey(platform, userId)
}