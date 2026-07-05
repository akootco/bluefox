package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.delegating.default
import co.akoot.plugins.bluefox.api.delegating.of
import co.akoot.plugins.bluefox.extensions.mkdirp
import co.akoot.plugins.bluefox.extensions.touch
import co.akoot.plugins.bluefox.extensions.username
import co.akoot.plugins.bluefox.util.or
import co.akoot.plugins.bluefox.util.parse
import co.akoot.plugins.bluefox.util.prefix
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.OfflinePlayer
import java.io.File
import java.sql.Date
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Types
import java.time.LocalDate
import java.util.*

class Profile(val uuid: String, val username: String) {
    constructor(uuid: UUID, username: String) : this(uuid.toString(), username)
    constructor(player: OfflinePlayer) : this(player.uniqueId.toString(), player.username)

    val folder = File("users").resolve(uuid).mkdirp()
    val settings = FoxConfig(folder.resolve("settings.conf").touch("{}"))
    val data = FoxConfig(folder.resolve("data.conf").touch("{}"))

    val aliases: List<String> by data default listOf()
    fun addAlias(alias: String) = data.append("aliases", alias)
    fun removeAlias(alias: String) = data.remove("aliases", alias)

    val macros: List<String> by data default listOf()
    fun addMacro(macro: String) = data.append("macros", macro)
    fun removeMacro(macro: String) = data.remove("macros", macro)

    val notes: List<String> by data default listOf()
    fun addNote(note: String) = data.append("notes", note)
    fun removeNote(note: String) = data.remove("notes", note)

    val mail: List<String> by data default listOf()
    fun addMail(mail: String) = data.append("mail", mail)
    fun deleteMail(mail: String) = data.remove("mail", mail)

    val unlockedTitles: List<String> by data default listOf()
    fun giveTitle(title: String) = data.append("titles", title)
    fun removeTitle(title: String) = data.remove("titles", title)
    fun hasTitle(title: String) = unlockedTitles.contains(title)

    val unlockedChatThemes: List<String> by data default listOf()
    fun giveChatTheme(chatTheme: String) = data.append("chatThemes", chatTheme)
    fun removeChatTheme(chatTheme: String) = data.remove("chatThemes", chatTheme)
    fun hasChatTheme(chatTheme: String) = unlockedChatThemes.contains(chatTheme)

    val unlockedPalettes: List<String> by data default listOf()
    fun givePalette(palette: String) = data.append("palettes", palette)
    fun removePalette(palette: String) = data.remove("palettes", palette)
    fun hasPalette(palette: String) = unlockedPalettes.contains(palette)

    val ignoredPlayers: List<String> by data default listOf()
    fun ignorePlayer(player: OfflinePlayer) = data.append("ignoredPlayers", player.uniqueId.toString())
    fun unignorePlayer(player: OfflinePlayer) = data.remove("ignoredPlayers", player.uniqueId.toString())
    fun isIgnoring(player: OfflinePlayer) = ignoredPlayers.contains(player.uniqueId.toString())

    val friends: List<String> by data default listOf()
    fun friend(player: OfflinePlayer) = data.append("friends", player.uniqueId.toString())
    fun unfriend(player: OfflinePlayer) = data.remove("friends", player.uniqueId.toString())
    fun isFriendsWith(player: OfflinePlayer) = friends.contains(player.uniqueId.toString())

    val marriageProposals: List<String> by data default listOf()
    fun proposeBy(player: OfflinePlayer) = data.append("marriageProposals", player.uniqueId.toString())
    fun unProposeBy(player: OfflinePlayer) = data.remove("marriageProposals", player.uniqueId.toString())
    fun isProposedBy(player: OfflinePlayer) = marriageProposals.contains(player.uniqueId.toString())

    val suicidePact: List<String> by data default listOf()
    fun suicidePactWith(player: OfflinePlayer) = data.append("suicidePact", player.uniqueId.toString())
    fun unSuicidePactWith(player: OfflinePlayer) = data.remove("suicidePact", player.uniqueId.toString())
    fun isInSuicidePactWith(player: OfflinePlayer) = suicidePact.contains(player.uniqueId.toString())

    val ignoredDiscordUsers: List<Long> by data default listOf()
    fun ignoreDiscordUser(id: Long) = data.append("ignoredDiscordUsers", id)
    fun unignoreDiscordUser(id: Long) = data.remove("ignoredDiscordUsers", id)

    var spouse: OfflinePlayer?
        get() = data.getString("spouse")?.let { BlueFox.server.getOfflinePlayer(it) }
        set(value) = data.set("spouse", value?.uniqueId?.toString())

    var lastDmTarget: OfflinePlayer?
        get() = data.getString("lastDmTarget")?.let { BlueFox.server.getOfflinePlayer(it) }
        set(value) = data.set("lastDmTarget", value?.uniqueId?.toString())

    enum class TitleStyle {
        NORMAL, SMALL_TEXT, NONE
    }

    var nickname: String by settings default ""
    var title: String by settings default "Guest"
    var status: String by settings default ""
    var titleStyle: TitleStyle by settings of TitleStyle::valueOf default TitleStyle.NORMAL
    var heartSymbol: String by settings default "❤"
    var lastWords: String by data default ""
    var lastChangelogVersion: String by data default ""
    var chatFormat: String by settings default ""
    var chatTint: String by settings default ""
    var chatTintIntensity: Double by settings default -1.0
    var universalChatFormat: String by settings default ""
    var universalChatTint: String by settings default ""
    var universalChatTintIntensity: Double by settings default -1.0

    var favColor: String by settings default ""
    var skullColor: String by settings default ""
    var heartColor: String by settings default ""
    var chatColor: String by settings default ""
    var bracketColor: String by settings default ""

    var diamondsEaten: Int by data default 0
    var netheriteIngotsEaten: Int by data default 0
    var swordsSwallowed: Int by data default 0

    var afk: Boolean by data default false
    var muted: Boolean by data default false

    var pingsEnabled: Boolean by settings default true
    var swearFilter: Boolean by settings default true
    var changelogEnabled: Boolean by settings default true
    var autoAfk: Boolean by settings default true
    var sendFoundDiamondsMessages: Boolean by settings default true
    var seeFoundDiamondsMessages: Boolean by settings default true
    var tpEnabled: Boolean by settings default true
    var tpaEnabled: Boolean by settings default true
    var keepInv: Boolean by settings default true
    var deathWish: Boolean by settings default false
    var mountable: Boolean by settings default true
    var bedrock: Boolean by settings default false
    var alwaysHungry: Boolean by settings default false
    var chatDisabled: Boolean by settings default false
    var tintChat: Boolean by settings default true

    var incomingChatLanguage: String by settings default ""
    var outgoingChatLanguage: String by settings default ""
    var dateFormat: String by settings default ""
    var timeFormat: String by settings default ""

    var timeZone: TimeZone by settings of TimeZone::getTimeZone from TimeZone::getID default TimeZone.getDefault()

    var id: Int? = getId() ?: setId()
        set(value) {
            setId(value)
            field = value
        }

    var token: String? = null
        get() = getToken()
        set(value) {
            setToken(value)
            field = value
        }

    var bio: Bio? = getBio()
        set(value) {
            setBio(value)
            field = value
        }

    data class Birthday(val month: Int? = null, val day: Int? = null, val year: Int? = null)
    open class Pronouns(
        val name: String,
        val they: String = "they",
        val them: String = "them",
        val their: String = "their",
        val theirs: String = "theirs",
        val theyAre: String = "they're",
        val were: String = "were",
        val themself: String = "themself",
    ) {
        companion object {
            val values: List<Pronouns> = BlueFox.query("SELECT * FROM pronouns")
                .executeQuery()
                .use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Pronouns(
                                    name = rs.getString("name"),
                                    they = rs.getString("they"),
                                    them = rs.getString("them"),
                                    their = rs.getString("their"),
                                    theirs = rs.getString("theirs"),
                                    theyAre = rs.getString("they_are"),
                                    were = rs.getString("were"),
                                    themself = rs.getString("themself"),
                                )
                            )
                        }
                    }
                }
        }

        val id get() = values.indexOf(this)
        fun process(string: String): String {
            return string
                .replace("#they", they)
                .replace("#them", them)
                .replace("#their", their)
                .replace("#theirs", theirs)
                .replace("#they're", theyAre)
                .replace("#were", were)
                .replace("#themself", themself)
        }
    }

    open class Generation(val name: String, val yearMin: Int, val yearMax: Int) {
        companion object {
            val values: List<Generation> = BlueFox.query("SELECT * FROM generation")
                .executeQuery()
                .use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Generation(
                                    name = rs.getString("name"),
                                    yearMin = rs.getInt("year_min"),
                                    yearMax = rs.getInt("year_max"),
                                )
                            )
                        }
                    }
                }
        }

        val id get() = values.indexOf(this)
    }

    data class Bio(
        val birthday: Birthday? = null,
        val pronouns: Pronouns? = null,
        val generation: Generation? = null,
        val about: String? = null
    )

    @JvmName("getLeBio")
    fun getBio(): Bio? {
        val id = id ?: return null
        BlueFox.query(
            """
            select * from player_bio where player = ?;
        """
        ).use { statement ->
            statement.setInt(1, id)
            statement.executeQuery().use { result ->
                return if (result.next()) {
                    Bio(
                        Birthday(
                            result.getInt("birth_month").takeIf { it != 0 },
                            result.getInt("birth_day").takeIf { it != 0 },
                            result.getInt("birth_year").takeIf { it != 0 }),
                        Pronouns.values.getOrNull(result.getInt("pronouns").takeIf { it != 0 } ?: -1),
                        Generation.values.getOrNull(result.getInt("generation").takeIf { it != 0 } ?: -1),
                        result.getString("about")
                    )
                } else {
                    null
                }
            }
        }
    }

    @JvmName("setLeBio")
    fun setBio(bio: Bio?): Bio? {
        val id = id ?: return null
        if (bio == null) {
            BlueFox.query("delete FROM player_bio where player = ?").use { statement ->
                statement.setInt(1, id)
                statement.executeUpdate()
            }
            return null
        }
        return try {
            BlueFox.query(
                """
                INSERT INTO player_bio (player, birth_month, birth_day, birth_year, generation, pronouns, about) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                    birthday = VALUES(birthday),
                    generation = VALUES(generation),
                    pronouns = VALUES(pronouns),
                    about = VALUES(about);
            """
            ).use { stmt ->
                stmt.setInt(1, id)
                bio.birthday?.month?.let { month -> stmt.setInt(2, month) } ?: stmt.setInt(2, Types.INTEGER)
                bio.birthday?.day?.let { day -> stmt.setInt(3, day) } ?: stmt.setInt(3, Types.INTEGER)
                bio.birthday?.year?.let { year -> stmt.setInt(4, year) } ?: stmt.setInt(4, Types.INTEGER)
                bio.generation?.let { generation -> stmt.setInt(5, generation.id) } ?: stmt.setInt(5, Types.INTEGER)
                bio.pronouns?.let { pronouns -> stmt.setInt(6, pronouns.id) } ?: stmt.setInt(6, Types.INTEGER)
                bio.about?.let { about -> stmt.setString(7, about) } ?: stmt.setInt(7, Types.VARCHAR)
                stmt.executeUpdate()
            }
            bio
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            null
        }
    }

    @JvmName("getLeId")
    fun getId(): Int? {

        BlueFox.query(
            """
            select id from player_id where uuid = ?;
        """
        ).use { statement ->
            statement.setString(1, uuid)
            statement.executeQuery().use { result ->
                return if (result.next()) {
                    result.getInt("id")
                } else {
                    null
                }
            }
        }
    }

    @JvmName("setLeId")
    fun setId(id: Int? = null): Int? {
        return try {
            if (id == null) {
                // let MySQL auto-generate ID

                BlueFox.query(
                    """
                INSERT INTO player_id (uuid) 
                VALUES (?)
                ON DUPLICATE KEY UPDATE uuid = uuid
            """
                ).use { stmt ->
                    stmt.setString(1, uuid)
                    stmt.executeUpdate()
                }
                getId()
            } else {
                // try to insert with a specific ID

                BlueFox.query(
                    """
                INSERT INTO player_id (id, uuid) 
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE uuid = uuid
            """
                ).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.setString(2, uuid)
                    val affected = stmt.executeUpdate()
                    affected > 0
                }
                id
            }
        } catch (_: SQLIntegrityConstraintViolationException) {
            // happens if the ID is already used
            null
        }
    }

    @JvmName("getLeToken")
    fun getToken(): String? {
        val id = id ?: return null
        val now = Date.valueOf(LocalDate.now())

        BlueFox.query(
            """
            select token, expires from player_token where player = ?;
        """
        ).use { statement ->
            statement.setInt(1, id)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    var token = result.getString("token")
                    val expires = result.getDate("expires")
                    if (expires.before(now)) {
                        token = BlueFox.generateToken()
                        setToken(token)
                    }
                    return token
                } else {
                    return null
                }
            }
        }
    }

    @JvmName("setLeToken")
    fun setToken(token: String? = null): String? {
        val id = id ?: return null
        val newToken = token ?: BlueFox.generateToken()
        val date = Date.valueOf(LocalDate.now().plusDays(30))
        return try {
            BlueFox.query(
                """
                INSERT INTO player_token (player, token, expires) 
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                    token = VALUES(token),
                    expires = VALUES(expires);
            """
            ).use { stmt ->
                stmt.setInt(1, id)
                stmt.setString(2, newToken)
                stmt.setDate(3, date)
                stmt.executeUpdate()
            }
            newToken
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            null
        }
    }

    fun parseTheme(
        format: String,
        tint: TextColor? = null,
        tintIntensity: Double = 1.0,
        nick: String = nickname,
        name: String = username,
        title: String = this.title,
        bracketColor: String = this.bracketColor,
        chatColor: String = this.chatColor
    ): Component {
        return format
            .replace("{bracketColor}", bracketColor prefix "&")
            .replace("{title}", title)
            .replace("{nick}", nick or name)
            .replace("{name}", name)
            .replace("{chatColor}", chatColor prefix "&")
            .parse(tint, tintIntensity)
    }

    fun parseTheme(format: String, profile: Profile, tint: TextColor? = null, tintIntensity: Double = 1.0) = parseTheme(
        format = format,
        tint = tint,
        tintIntensity = tintIntensity,
        nick = profile.nickname,
        name = profile.username,
        title = profile.title,
        bracketColor = profile.bracketColor,
        chatColor = profile.chatColor
    )
}