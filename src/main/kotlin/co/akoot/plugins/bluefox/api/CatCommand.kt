package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.CommandHelp
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.accent
import co.akoot.plugins.bluefox.util.sendWarning
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.BlockState
import org.bukkit.block.BlockType
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType
import org.bukkit.potion.PotionType
import java.time.ZoneId

abstract class CatCommand(
    val plugin: FoxPlugin,
    val id: String,
    val description: String = "A ${plugin.id} command.",
    vararg val aliases: String,
    val onCommand: CatCommand.() -> Unit = {}
) : LiteralArgumentBuilder<CommandSourceStack>(id) {

    init {
        onCommand()
    }

    val win = Command.SINGLE_SUCCESS
    val fail = -1
    open var help: CommandHelp = CommandHelp().description(description)

    @JvmName("leGetSender")
    fun getSender(ctx: CommandContext<CommandSourceStack>): CommandSender {
        return ctx.source.sender
    }

    val CommandContext<CommandSourceStack>.sender: CommandSender get() = source.sender

    fun getPlayersFromEntities(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entities"
    ): List<Player> {
        val resolver = ctx.getArgument(argName, EntitySelectorArgumentResolver::class.java)
        return resolver.resolve(ctx.source).filterIsInstance<Player>()
    }

    val CommandContext<CommandSourceStack>.playersFromEntities: List<Player> get() = getPlayersFromEntities(this)
    fun CommandContext<CommandSourceStack>.playersFromEntities(argName: String): List<Player> = getPlayersFromEntities(this, argName)

    fun getPlayerFromEntities(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entities"
    ): Player? {
        return getPlayersFromEntities(ctx, argName).firstOrNull()
    }

    val CommandContext<CommandSourceStack>.playerFromEntities: Player? get() = getPlayerFromEntities(this)
    fun CommandContext<CommandSourceStack>.playerFromEntities(argName: String): Player? = getPlayerFromEntities(this, argName)

    fun getPlayer(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "player"
    ): Player {
        val player = runCatching { ctx.getArgument(argName, Player::class.java) }.getOrNull()
        return player ?: getPlayerSelector(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.player: Player get() = getPlayer(this)
    fun CommandContext<CommandSourceStack>.player(argName: String): Player = getPlayer(this, argName)

    fun getPlayerSelector(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "player"
    ): Player {
        return getPlayers(ctx, argName).first()
    }

    val CommandContext<CommandSourceStack>.playerFromSelector: Player get() = getPlayerSelector(this)
    fun CommandContext<CommandSourceStack>.playerFromSelector(argName: String): Player = getPlayerSelector(this, argName)

    fun getPlayers(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "players"
    ): List<Player> {
        val resolver = ctx.getArgument(argName, PlayerSelectorArgumentResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    val CommandContext<CommandSourceStack>.players: List<Player> get() = getPlayers(this)
    fun CommandContext<CommandSourceStack>.players(argName: String): List<Player> = getPlayers(this, argName)

    fun getEntities(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entities"
    ): List<Entity> {
        val resolver = ctx.getArgument(argName, EntitySelectorArgumentResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    val CommandContext<CommandSourceStack>.entities: List<Entity> get() = getEntities(this)
    fun CommandContext<CommandSourceStack>.entities(argName: String) = getEntities(this, argName)

    fun getEntity(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "entity"
    ): Entity {
        return getEntities(ctx, argName).first()
    }

    val CommandContext<CommandSourceStack>.entity: Entity get() = getEntity(this)
    fun CommandContext<CommandSourceStack>.entity(argName: String) = getEntity(this, argName)

    fun getWorld(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "world"
    ): World {
        return ctx.getArgument(argName, World::class.java)
    }

    val CommandContext<CommandSourceStack>.world: World get() = getWorld(this)
    fun CommandContext<CommandSourceStack>.world(argName: String) = getWorld(this, argName)

    fun getBlockPosition(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "x y z"
    ): BlockPosition {
        val resolver = ctx.getArgument(argName, BlockPositionResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    val CommandContext<CommandSourceStack>.blockPos: BlockPosition get() = getBlockPosition(this)
    fun CommandContext<CommandSourceStack>.blockPos(argName: String) = getBlockPosition(this, argName)

    fun getPosition(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "x y z"
    ): FinePosition {
        val resolver = ctx.getArgument(argName, FinePositionResolver::class.java)
        return resolver.resolve(ctx.source)
    }

    val CommandContext<CommandSourceStack>.position: FinePosition get() = getPosition(this)
    fun CommandContext<CommandSourceStack>.position(argName: String) = getPosition(this, argName)

    fun getLocation(
        ctx: CommandContext<CommandSourceStack>,
        positionArgName: String = "x y z",
        worldArgName: String = "world"
    ): Location {
        val position = getPosition(ctx, positionArgName)
        val world = getWorld(ctx, worldArgName)
        return Location(world, position.x(), position.y(), position.z())
    }

    val CommandContext<CommandSourceStack>.location: Location get() = getLocation(this)
    fun CommandContext<CommandSourceStack>.location(world: World) = position.let { Location(world, it.x(), it.y(), it.z()) }

    fun subcommand(
        name: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun SubCommand(
        name: String,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { subcommand(name, executes) }

    fun boolean(
        argName: String = "true or false",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Boolean> {
        return Commands.argument(argName, BoolArgumentType.bool())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun Boolean(
        argName: String = "true or false",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { boolean(argName, executes)}

    fun getBoolean(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "true or false"
    ): Boolean {
        return BoolArgumentType.getBool(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.boolean: Boolean get() = getBoolean(this)
    fun CommandContext<CommandSourceStack>.boolean(argName: String) = getBoolean(this, argName)

    fun word(
        argName: String,
        suggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { _, _ -> },
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.word())
            .suggests { ctx, builder ->
                suggestions(ctx, builder)
                builder.buildFuture()
            }
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun string(
        argName: String,
        suggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { _, _ -> },
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.string())
            .suggests { ctx, builder ->
                suggestions(ctx, builder)
                builder.buildFuture()
            }
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun String(
        argName: String,
        suggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { _, _ -> },
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { string(argName, suggestions, executes) }

    fun greedyString(
        argName: String,
        suggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { _, _ -> },
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument(argName, StringArgumentType.greedyString())
            .suggests { ctx, builder ->
                suggestions(ctx, builder)
                builder.buildFuture()
            }
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun GreedyString(
        argName: String,
        suggestions: (ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder) -> Unit = { _, _ -> },
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { greedyString(argName, suggestions, executes) }

    fun getString(ctx: CommandContext<CommandSourceStack>, argName: String): String {
        return StringArgumentType.getString(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.string: String get() = getString(this, "string")
    fun CommandContext<CommandSourceStack>.string(argName: String) = getString(this, argName)

    fun int(
        argName: String = "value",
        min: Int? = null,
        max: Int? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Int> {
        val type = if (min != null && max == null) IntegerArgumentType.integer(min)
        else if (min != null && max != null) IntegerArgumentType.integer(min, max)
        else IntegerArgumentType.integer()
        return Commands.argument(argName, type).executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun Int(
        argName: String = "value",
        min: Int? = null,
        max: Int? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { int(argName, min, max, executes) }

    fun getInt(ctx: CommandContext<CommandSourceStack>, argName: String = "value"): Int {
        return IntegerArgumentType.getInteger(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.int: Int get() = getInt(this)
    fun CommandContext<CommandSourceStack>.int(argName: String) = getInt(this, argName)

    fun float(
        argName: String = "value",
        min: Float? = null,
        max: Float? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Float> {
        val type = if (min != null && max == null) FloatArgumentType.floatArg(min)
        else if (min != null && max != null) FloatArgumentType.floatArg(min, max)
        else FloatArgumentType.floatArg()
        return Commands.argument(argName, type).executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun Float(
        argName: String = "value",
        min: Float? = null,
        max: Float? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ) = then { float(argName, min, max, executes) }

    fun getFloat(ctx: CommandContext<CommandSourceStack>, argName: String = "value"): Float {
        return FloatArgumentType.getFloat(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.float: Float get() = getFloat(this)
    fun CommandContext<CommandSourceStack>.float(argName: String) = getFloat(this, argName)

    fun double(
        argName: String = "value",
        min: Double? = null,
        max: Double? = null,
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Double> {
        val type = if (min != null && max == null) DoubleArgumentType.doubleArg(min)
        else if (min != null && max != null) DoubleArgumentType.doubleArg(min, max)
        else DoubleArgumentType.doubleArg()
        return Commands.argument(argName, type).executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getDouble(ctx: CommandContext<CommandSourceStack>, argName: String = "value"): Double {
        return DoubleArgumentType.getDouble(ctx, argName)
    }

    val CommandContext<CommandSourceStack>.double: Double get() = getDouble(this)
    fun CommandContext<CommandSourceStack>.double(argName: String) = getDouble(this, argName)

    fun offlinePlayer(
        argName: String = "player",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, OfflinePlayer> {
        return Commands.argument(argName, OfflinePlayerArgument())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun sound(
        argName: String = "sound",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Sound> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.SOUND_EVENT))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getSound(ctx: CommandContext<CommandSourceStack>, argName: String = "sound"): Sound {
        return ctx.getArgument(argName, Sound::class.java)
    }

    val CommandContext<CommandSourceStack>.sound: Sound get() = getSound(this)
    fun CommandContext<CommandSourceStack>.sound(argName: String) = getSound(this, argName)

    fun item(
        argName: String = "item",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, ItemType> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.ITEM))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getItem(ctx: CommandContext<CommandSourceStack>, argName: String = "item"): ItemType {
        return ctx.getArgument(argName, ItemType::class.java)
    }

    val CommandContext<CommandSourceStack>.item: ItemType get() = getItem(this)
    fun CommandContext<CommandSourceStack>.item(argName: String) = getItem(this, argName)

    fun block(
        argName: String = "block",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockType> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.BLOCK))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getBlock(ctx: CommandContext<CommandSourceStack>, argName: String = "block"): BlockType {
        return ctx.getArgument(argName, BlockType::class.java)
    }

    val CommandContext<CommandSourceStack>.block: BlockType get() = getBlock(this)
    fun CommandContext<CommandSourceStack>.block(argName: String) = getBlock(this, argName)

    fun biome(
        argName: String = "biome",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Biome> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.BIOME))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getBiome(ctx: CommandContext<CommandSourceStack>, argName: String = "biome"): Biome {
        return ctx.getArgument(argName, Biome::class.java)
    }

    val CommandContext<CommandSourceStack>.biome: Biome get() = getBiome(this)
    fun CommandContext<CommandSourceStack>.biome(argName: String) = getBiome(this, argName)

    fun potion(
        argName: String = "potion",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, PotionType> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.POTION))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getPotion(ctx: CommandContext<CommandSourceStack>, argName: String = "potion"): PotionType {
        return ctx.getArgument(argName, PotionType::class.java)
    }

    val CommandContext<CommandSourceStack>.potion: PotionType get() = getPotion(this)
    fun CommandContext<CommandSourceStack>.potion(argName: String) = getPotion(this, argName)

    fun enchantment(
        argName: String = "enchantment",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Enchantment> {
        return Commands.argument(argName, ArgumentTypes.resource(RegistryKey.ENCHANTMENT))
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getEnchantment(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "enchantment"
    ): Enchantment {
        return ctx.getArgument(argName, Enchantment::class.java)
    }

    val CommandContext<CommandSourceStack>.enchantment: Enchantment get() = getEnchantment(this)
    fun CommandContext<CommandSourceStack>.enchantment(argName: String) = getEnchantment(this, argName)

    fun getOfflinePlayer(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "player"
    ): OfflinePlayer? {
        val player = runCatching { ctx.getArgument(argName, OfflinePlayer::class.java) }.getOrNull()
        return player
    }

    val CommandContext<CommandSourceStack>.offlinePlayer: OfflinePlayer? get() = getOfflinePlayer(this)
    fun CommandContext<CommandSourceStack>.offlinePlayer(argName: String) = getOfflinePlayer(this, argName)

    fun player(
        argName: String = "player",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, Player> {
        return Commands.argument(argName, PlayerArgument())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun players(
        argName: String = "players",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.players())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun entity(
        argName: String = "entity",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.entity())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun entities(
        argName: String = "entities",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> {
        return Commands.argument(argName, ArgumentTypes.entities())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun worldType(
        argName: String = "world type",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, World> {
        return Commands.argument(argName, ArgumentTypes.world())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun world(
        argName: String = "world",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, World> {
        return Commands.argument(argName, WorldArgument()).executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun blockPosition(
        argName: String = "x y z",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> {
        return Commands.argument(argName, ArgumentTypes.blockPosition())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun position(
        argName: String = "x y z",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, FinePositionResolver> {
        return Commands.argument(argName, ArgumentTypes.finePosition())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    // extras

    fun blockState(
        argName: String = "block state",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockState> {
        return Commands.argument(argName, ArgumentTypes.blockState())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun material(
        argName: String = "material",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, BlockState> {
        return Commands.argument(argName, ArgumentTypes.blockState())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getBlockState(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "block state"
    ): BlockState {
        return ctx.getArgument(argName, BlockState::class.java)
    }

    fun getMaterial(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "material"
    ): Material {
        return getBlockState(ctx, argName).type
    }

    val CommandContext<CommandSourceStack>.material: Material get() = getMaterial(this)
    fun CommandContext<CommandSourceStack>.material(argName: String) = getMaterial(this, argName)

    fun timeZone(
        argName: String = "time zone",
        executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }
    ): RequiredArgumentBuilder<CommandSourceStack, ZoneId> {
        return Commands.argument(argName, TimeZoneArgument())
            .executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun getZoneId(
        ctx: CommandContext<CommandSourceStack>,
        argName: String = "time zone"
    ): ZoneId {
        return ctx.getArgument(argName, ZoneId::class.java)
    }

    val CommandContext<CommandSourceStack>.timeZone: ZoneId get() = getZoneId(this)
    fun CommandContext<CommandSourceStack>.timeZone(argName: String) = getZoneId(this, argName)

    fun success(ctx: CommandContext<CommandSourceStack>, message: Text): Boolean {
        message.send(getSender(ctx))
        return true
    }

    fun fail(ctx: CommandContext<CommandSourceStack>, message: Text): Boolean {
        message.send(getSender(ctx))
        return false
    }

    fun noargs(executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }) {
        executes { if (executes(it)) Command.SINGLE_SUCCESS else -1 }
    }

    fun NoArgs(executes: (ctx: CommandContext<CommandSourceStack>) -> Boolean = { false }) = noargs(executes)

    fun getPlayerSender(ctx: CommandContext<CommandSourceStack>, sendError: Boolean = true): Player? {
        val sender = getSender(ctx)
        if (sendError && sender !is Player) {
            sender.sendMessage("You must be a player to run this command.")
        }
        return sender as? Player
    }

    val CommandContext<CommandSourceStack>.playerSender: Player? get() = getPlayerSender(this)

    @JvmName("lePermissionCheck")
    fun permissionCheck(ctx: CommandContext<CommandSourceStack>, node: String? = null): Boolean? {
        val sender = getSender(ctx)
        val finalNode = node?.let { ".$it" } ?: ""
        if (sender.hasPermission(permissionNode(finalNode))) return true
        sender.sendWarning("You do not have permission to run ", accent("/${ctx.input}"))
        return null
    }

    fun permissionNode(node: String): String = "${plugin.id}.command.$id$node"

    fun CommandContext<CommandSourceStack>.permissionCheck(node: String? = null) = permissionCheck(this, node)

    fun suggest(builder: SuggestionsBuilder, suggestions: List<String>) {
        suggestions.stream()
            .filter { entry -> entry.startsWith(builder.remainingLowerCase) }
            .forEach(builder::suggest)
    }

    @JvmName("suggestInt")
    fun suggest(builder: SuggestionsBuilder, suggestions: List<Int>) {
        suggestions.stream()
            .forEach(builder::suggest)
    }

    @JvmName("suggestText")
    fun suggest(builder: SuggestionsBuilder, suggestions: List<Pair<String, Text>>) {
        suggestions.stream()
            .filter { entry -> entry.first.startsWith(builder.remainingLowerCase) }
            .forEach { builder.suggest(it.first, MessageComponentSerializer.message().serialize(it.second.component)) }
    }

    @JvmName("suggestComponent")
    fun suggest(builder: SuggestionsBuilder, suggestions: Map<String, Component>) {
        suggestions.filter { entry -> entry.key.startsWith(builder.remainingLowerCase) }
            .forEach { (string, component) ->
                builder.suggest(string, MessageComponentSerializer.message().serialize(component))
            }
    }

    @JvmName("suggestIntText")
    fun suggest(builder: SuggestionsBuilder, suggestions: List<Pair<Int, Text>>) {
        suggestions.stream()
            .forEach { builder.suggest(it.first, MessageComponentSerializer.message().serialize(it.second.component)) }
    }

    fun suggestRaw(builder: SuggestionsBuilder, suggestions: List<String>) {
        suggestions.stream()
            .filter { entry -> entry.contains(builder.remaining, true) }
            .forEach(builder::suggest)
    }

    @JvmName("suggestIntRaw")
    fun suggestRaw(builder: SuggestionsBuilder, suggestions: List<Int>) {
        suggestions.stream()
            .forEach(builder::suggest)
    }

    @JvmName("suggestTextRaw")
    fun suggestRaw(builder: SuggestionsBuilder, suggestions: List<Pair<String, Text>>) {
        suggestions.stream()
            .filter { entry -> entry.first.contains(builder.remaining, true) }
            .forEach { builder.suggest(it.first, MessageComponentSerializer.message().serialize(it.second.component)) }
    }

    @JvmName("suggestIntTextRaw")
    fun suggestRaw(builder: SuggestionsBuilder, suggestions: List<Pair<Int, Text>>) {
        suggestions.stream()
            .forEach { builder.suggest(it.first, MessageComponentSerializer.message().serialize(it.second.component)) }
    }

    fun then(something: (LiteralArgumentBuilder<CommandSourceStack>) -> ArgumentBuilder<CommandSourceStack, *>): ArgumentBuilder<CommandSourceStack, *> {
        return then(something(this))
    }

    infix fun ArgumentBuilder<CommandSourceStack, *>.then(something: () -> ArgumentBuilder<CommandSourceStack, *>): ArgumentBuilder<CommandSourceStack, *> {
        return then(something())
    }
}