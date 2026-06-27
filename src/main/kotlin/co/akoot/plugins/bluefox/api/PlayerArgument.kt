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
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import kotlin.text.startsWith

class PlayerArgument: CustomArgumentType.Converted<Player, String> {
    companion object {
        val ERROR_UNKNOWN_PLAYER: DynamicCommandExceptionType = DynamicCommandExceptionType {
            MessageComponentSerializer.message().serialize(Component.text("$it is not online!"))
        }
    }

    override fun convert(nativeType: String): Player {
        return BlueFox.getPlayer(nativeType) ?: throw ERROR_UNKNOWN_PLAYER.create(nativeType)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val playerNames = BlueFox.server.onlinePlayers.map { it.name }.filter { it.startsWith(".") && builder.remainingLowerCase.startsWith(".") || it.startsWith(".") }
        for(name in playerNames) {
            if(name.contains(builder.remaining, true)) {
                builder.suggest(name)
            }
        }
        return builder.buildFuture()
    }
}