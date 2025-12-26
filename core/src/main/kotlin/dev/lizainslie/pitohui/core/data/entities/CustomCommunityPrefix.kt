package dev.lizainslie.pitohui.core.data.entities

import dev.lizainslie.pitohui.core.data.tables.CustomCommunityPrefixTable
import dev.lizainslie.pitohui.core.platforms.PlatformId
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class CustomCommunityPrefix(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<CustomCommunityPrefix>(CustomCommunityPrefixTable) {
        fun getPrefix(communityId: PlatformId): CustomCommunityPrefix? {
            return find {
                (CustomCommunityPrefixTable.platform eq communityId.platform.key) and
                (CustomCommunityPrefixTable.communityId eq communityId.id)
            }.firstOrNull()
        }
    }

    var platform by CustomCommunityPrefixTable.platform
    var communityId by CustomCommunityPrefixTable.communityId
    var prefix by CustomCommunityPrefixTable.prefix
}