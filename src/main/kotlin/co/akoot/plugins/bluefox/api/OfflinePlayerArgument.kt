package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.concurrent.CompletableFuture
import kotlin.text.startsWith

class OfflinePlayerArgument: CustomArgumentType.Converted<OfflinePlayer, String> {
    companion object {
        val ERROR_UNKNOWN_PLAYER: DynamicCommandExceptionType = DynamicCommandExceptionType {
            MessageComponentSerializer.message().serialize(Component.text("$it has never joined!"))
        }
    }

    override fun convert(nativeType: String): OfflinePlayer {
        return BlueFox.getOfflinePlayer(nativeType) ?: throw ERROR_UNKNOWN_PLAYER.create(nativeType)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val offlinePlayerNames = BlueFox.cachedOfflinePlayerNames
        if(!builder.remainingLowerCase.startsWith(".")) offlinePlayerNames.removeIf { it.startsWith(".") }
        for(name in offlinePlayerNames) {
            if(name.contains(builder.remainingLowerCase, true)) {
                builder.suggest(name)
            }
        }
        return builder.buildFuture()
    }
}