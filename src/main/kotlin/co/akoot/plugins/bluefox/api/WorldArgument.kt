package co.akoot.plugins.bluefox.api

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
import org.bukkit.World
import java.util.concurrent.CompletableFuture

class WorldArgument: CustomArgumentType.Converted<World, String> {
    companion object {
        val ERROR_INVALID_WORLD: DynamicCommandExceptionType = DynamicCommandExceptionType {
            MessageComponentSerializer.message().serialize(Component.text("$it is not a world!"))
        }
    }

    override fun convert(nativeType: String): World {
        return Bukkit.getWorld(nativeType) ?: throw ERROR_INVALID_WORLD.create(nativeType)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for(world in Bukkit.getWorlds()) {
            val name = world.name
            if(name.startsWith(builder.remainingLowerCase)) {
                builder.suggest(name)
            }
        }
        return builder.buildFuture()
    }
}