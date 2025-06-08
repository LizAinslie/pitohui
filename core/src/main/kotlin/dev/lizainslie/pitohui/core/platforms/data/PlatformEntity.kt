//package dev.lizainslie.pitohui.core.platforms.data
//
//import dev.lizainslie.pitohui.core.platforms.CommandContext
//import org.jetbrains.exposed.dao.CompositeEntity
//import org.jetbrains.exposed.dao.id.CompositeID
//import org.jetbrains.exposed.dao.id.CompositeIdTable
//import org.jetbrains.exposed.dao.id.EntityID
//
//abstract class PlatformEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
//    abstract val platformId: String
//    abstract val platform: CommandContext
//}
//
//abstract class PlatformTable(name: String) : CompositeIdTable(name) {
//    val platformId = varchar("platform_id", 255)
//    val platform = enumerationByName<CommandContext>("platform", 32)
//
//    override val primaryKey = PrimaryKey(platformId, platform)
//}