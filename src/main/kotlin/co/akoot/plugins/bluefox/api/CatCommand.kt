package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.util.Text
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.time.ZoneId

abstract class CatCommand(val plugin: FoxPlugin, val id: String) :
    LiteralArgumentBuilder<CommandSourceStack>(id) {

    protected val win = Command.SINGLE_SUCCESS
    protected val fail = -1

    protected fun getSender(ctx: CommandContext<CommandSourceStack>): CommandSender {
        return ctx.source.sender
    }

    protected fun getPlayer(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "player"
    ): Player {
        return getPlayers(ctx, argName).first()
    }

    protected fun getPlayers(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "players"
    ): List<Player> {
        val resolver = ctx.getArgument(argName, PlayerSelectorArgumentResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    protected fun getEntities(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entities"
    ): List<Entity> {
        val resolver = ctx.getArgument(argName, EntitySelectorArgumentResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    protected fun getEntity(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entity"
    ): Entity {
        return getEntities(ctx, argName).first()
    }

    protected fun getWorld(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "world"
    ): World {
        return ctx.getArgument(argName, World::class.java)
    }

    protected fun getBlockPosition(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "x y z"
    ): BlockPosition {
        val resolver = ctx.getArgument(argName, BlockPositionResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    protected fun getPosition(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "x y z"
    ): FinePosition {
        val resolver = ctx.getArgument(argName, FinePositionResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    protected fun getLocation(
        ctx: CommandContext<CommandSourceStack>,
        positionArgName: String = "x y z",
        worldArgName: String = "world"
    ): Location {
        val position = getPosition(ctx, positionArgName)
        val world = getWorld(ctx, worldArgName)
        return Location(world, position.x(), position.y(), position.z())
    }

    protected fun subcommand(
        name: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun boolean(
        argName: String = "true or false",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Boolean> {
        return Commands.argument(argName, BoolArgumentType.bool()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getBoolean(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "true or false"
    ): Boolean {
        return BoolArgumentType.getBool(ctx, argName)
    }

    protected fun word(
        argName: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.word()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun string(
        argName: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.string()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun greedyString(
        argName: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.greedyString())
            .executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getString(ctx: CommandContext<CommandSourceStack>, argName: String): String {
        return StringArgumentType.getString(ctx, argName)
    }

    protected fun int(
        argName: String = "value",
        min: Int? = null,
        max: Int? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Int> {
        val type = if (min != null && max == null) IntegerArgumentType.integer(min)
        else if (min != null && max != null) IntegerArgumentType.integer(min, max)
        else IntegerArgumentType.integer()
        return Commands.argument(argName, type).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getInt(ctx: CommandContext<CommandSourceStack>, argName: String): Int {
        return IntegerArgumentType.getInteger(ctx, argName)
    }

    protected fun float(
        argName: String = "value",
        min: Float? = null,
        max: Float? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Float> {
        val type = if (min != null && max == null) FloatArgumentType.floatArg(min)
        else if (min != null && max != null) FloatArgumentType.floatArg(min, max)
        else FloatArgumentType.floatArg()
        return Commands.argument(argName, type).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getFloat(ctx: CommandContext<CommandSourceStack>, argName: String): Float {
        return FloatArgumentType.getFloat(ctx, argName)
    }

    protected fun double(
        argName: String = "value",
        min: Double? = null,
        max: Double? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Double> {
        val type = if (min != null && max == null) DoubleArgumentType.doubleArg(min)
        else if (min != null && max != null) DoubleArgumentType.doubleArg(min, max)
        else DoubleArgumentType.doubleArg()
        return Commands.argument(argName, type).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getDouble(ctx: CommandContext<CommandSourceStack>, argName: String): Double {
        return DoubleArgumentType.getDouble(ctx, argName)
    }

    protected fun player(
        argName: String = "player",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.player()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun players(
        argName: String = "players",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.players()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun entity(
        argName: String = "entity",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.entity()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun entities(
        argName: String = "entities",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.entities()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun worldType(
        argName: String = "world type",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, World> {
        return Commands.argument(argName, ArgumentTypes.world()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun world(
        argName: String = "world",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, World> {
        return Commands.argument(argName, WorldArgument()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun blockPosition(
        argName: String = "x y z",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> {
        return Commands.argument(argName, ArgumentTypes.blockPosition()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun position(
        argName: String = "x y z",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, FinePositionResolver> {
        return Commands.argument(argName, ArgumentTypes.finePosition()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    // extras

    protected fun blockState(
        argName: String = "block state",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockState> {
        return Commands.argument(argName, ArgumentTypes.blockState()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun material(
        argName: String = "material",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockState> {
        return Commands.argument(argName, ArgumentTypes.blockState()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getBlockState(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "block state"
    ): BlockState {
        return ctx.getArgument(argName, BlockState::class.java)
    }

    protected fun getMaterial(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "material"
    ): Material {
        return getBlockState(ctx, argName).type
    }

    protected fun timeZone(
        argName: String = "time zone",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, ZoneId> {
        return Commands.argument(argName, TimeZoneArgument()).executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    protected fun getZoneId(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "time zone"
    ): ZoneId {
        return ctx.getArgument(argName, ZoneId::class.java)
    }

    protected fun success(ctx: CommandContext<CommandSourceStack>, message: Text): Boolean {
        message.send(getSender(ctx))
        return true
    }

    protected fun fail(ctx: CommandContext<CommandSourceStack>, message: Text): Boolean {
        message.send(getSender(ctx))
        return false
    }

    protected fun noargs(executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }) {
        executes { if(executes(it)) Command.SINGLE_SUCCESS else -1 }
    }
}