package dev.lizainslie.pitohui.core.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object CustomCommunityPrefixTable : CompositeIdTable("custom_community_prefixes") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("platform_id", 255).entityId()
    val prefix = varchar("prefix", 16)

    override val primaryKey = PrimaryKey(platform, communityId)
}