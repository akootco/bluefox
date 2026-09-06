package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.delegating.default
import co.akoot.plugins.bluefox.api.delegating.deserialize
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
    var titleStyle: TitleStyle by settings deserialize TitleStyle::valueOf default TitleStyle.NORMAL
    var heartSymbol: String by settings default "❤"
    var lastWords: String by data default ""
    var lastChangelogVersion: String by data default ""
    var chatFormat: String by settings default ""
    var chatTint: String by settings default ""
    var chatTintIntensity: Double by settings default -1.0
    var globalChatFormat: String by settings default ""
    var globalChatTint: String by settings default ""
    var globalChatTintIntensity: Double by settings default -1.0

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

    var timeZone: TimeZone by settings deserialize TimeZone::getTimeZone serialize TimeZone::getID default TimeZone.getDefault()
    var pronouns: Pronouns by settings deserialize Pronouns::deserialize serialize Pronouns::serialized default Pronouns.default

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

    data class Birthday(val month: Int? = null, val day: Int? = null, val year: Int? = null) {
        companion object {
            val default = Birthday()
            fun deserialize(string: String): Birthday {
                val date = string.split("/")
                if(date.size == 1) return Birthday(month = date[0].toIntOrNull())
                if(date.size == 2) return Birthday(month = date[0].toIntOrNull(), day = date[1].toIntOrNull())
                if(date.size == 3) return Birthday(month = date[0].toIntOrNull(), day = date[1].toIntOrNull(), year = date[2].toIntOrNull())
                return default
            }
        }
    }
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

            val default: Pronouns = Pronouns("default")

            val values: List<Pronouns> = listOf(
                default,
                from("male", "he/him/his/his/he's/was/himself"),
                from("female", "she/her/hers/hers/she's/was/herself"),
                from("maleMajesty", "his majesty/his majesty/his majesty's/his majesty's/his majesty is/was/himself"),
                from("femaleMajesty", "her majesty/her majesty/her majesty's/her majesty's/her majesty is/was/herself"),
                from("majesty", "their majesty/their majesty/their majesty's/their majesty's/their majesty is/was/themself"),
            )

            fun deserialize(string: String): Pronouns {
                if (string.isEmpty()) return default
                if(!string.contains("/")) return values.find { it.name == string } ?: default
                return from("custom", string)
            }

            fun from(name: String, string: String): Pronouns {
                val parts = string.split("/")
                if(parts.size != 7) return default
                return Pronouns(name, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6])
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

        val serialized: String get() {
            return if(name == "custom")
                listOf(they, them, their, theirs, theyAre, were, themself)
                    .joinToString("/")
            else name
        }
    }

    open class Generation(val name: String, val yearMin: Int, val yearMax: Int) {
        companion object {
            val values: List<Generation> = listOf(
                Generation("The Lost Generation", 1883, 1910),
                Generation("The Greatest Generation", 1901, 1927),
                Generation("The Silent Generation"	 ,1928 ,1945),
                Generation("Boomer"	 ,1946 ,1964),
                Generation("X",1965, 1980),
                Generation( "Millennial", 1981, 1996),
                Generation("Zoomer"	 ,1997 ,2010),
                Generation("Gen Alpha", 2010, 2024),
                Generation("Gen Beta", 2025, 2039),
                Generation("Gen Gamma", 2040, 2054),
            )
        }

        val id get() = values.indexOf(this)
    }

    data class Bio(
        val birthday: Birthday? = null,
        val pronouns: Pronouns? = null,
        val generation: Generation? = null,
        val about: String? = null
    )

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