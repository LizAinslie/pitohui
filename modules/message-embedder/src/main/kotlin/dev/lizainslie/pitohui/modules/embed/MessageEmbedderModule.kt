import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord
import sun.net.www.content.text.plain

@SupportPlatforms(Discord::class)
object MessageEmbedderModule : AbstractModule() {
    override val name = "message-embedder"
    override val visibility = ModuleVisibility.MODERATOR
    override val description = "Send embedded messages in communities."
    override val commands = setOf<RootCommand>()
    override val dependencies = setOf(SystemModule.name)
}
