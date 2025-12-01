package dev.lizainslie.pitohui.core.modules

/**
 * ModuleVisibility represents the visibility level of a module within the bot.
 */
enum class ModuleVisibility {
    /**
     * Public modules are accessible to all users.
     */
    PUBLIC,

    /**
     * Moderator modules are accessible only to users with moderator privileges in a community.
     */
    MODERATOR,

    /**
     * Developer modules are accessible only to the bot developers.
     */
    DEVELOPER,
}
