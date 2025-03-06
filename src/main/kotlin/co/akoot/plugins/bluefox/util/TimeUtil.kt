package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.Kolor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtil {

    val MONTH = Calendar.getInstance().get(Calendar.MONTH)

    private val DAYS_IN_MONTH = YearMonth.now().lengthOfMonth()
    private val DAYS_IN_YEAR = YearMonth.now().lengthOfYear()
    private val TIME_REGEX = Regex("((?:[0-9]*[.])?[0-9]+)([a-z]{1,2})")

    private val TIME_MAP_MS = mapOf(
        "ms" to 1,
        "t" to 50,
        "s" to 1000,
        "m" to 60000,
        "h" to 3600000,
        "d" to 86400000,
        "w" to 604800000,
        "mo" to DAYS_IN_MONTH * 86400000,
        "y" to DAYS_IN_YEAR * 86400000
    )

    private val TIME_MAP_MS_LONG = mapOf(
        "milliseconds" to 1,
        "ticks" to 50,
        "seconds" to 1000,
        "minutes" to 60000,
        "hours" to 3600000,
        "days" to 86400000,
        "weeks" to 604800000,
        "months" to DAYS_IN_MONTH * 86400000,
        "years" to DAYS_IN_YEAR * 86400000
    )

    private val TIME_MAP_TICKS = mapOf(
        "t" to 1,
        "s" to 20,
        "m" to 1200,
        "h" to 72000,
        "d" to 1728000,
        "w" to 12096000,
        "mo" to DAYS_IN_MONTH * 1728000,
        "y" to DAYS_IN_YEAR * 1728000
    )

    val FORMAT_SIMPLE = "MM-dd"
    val FORMAT_SIMPLE_YEAR = "MM-dd yyyy"
    val FORMAT_SIMPLE_TIME = "MM-dd hh:mma"

    fun parseTime(string: String, asTicks: Boolean = false): Long {
        var totalTime = 0L

        for (result in TIME_REGEX.findAll(string)) {
            if (result.groupValues.size != 3) continue
            val number = result.groupValues[1].toLongOrNull() ?: continue
            val multiplier =
                (if (asTicks) TIME_MAP_TICKS[result.groupValues[2]] else TIME_MAP_MS[result.groupValues[2]])
                    ?: continue
            totalTime += number * multiplier
        }

        return totalTime
    }

    fun getTimeComponent(
        milliseconds: Long,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Component {
        val timeString = getTimeString(milliseconds - now)
        val formattedTime = formatTime(milliseconds, "MMM d, h:mma z", timeZone)
        return Kolor.NUMBER(formattedTime).copy(milliseconds.toString()).hover(Kolor.TEXT(timeString)).component
    }

    fun formatTime(
        milliseconds: Long,
        pattern: String = FORMAT_SIMPLE_TIME,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dateFormat = SimpleDateFormat(pattern)
        dateFormat.timeZone = timeZone
        return dateFormat.format(Date(milliseconds))
    }

    fun getTimeString(milliseconds: Long): String {
        if (milliseconds == 0L) return "0 seconds" // Handle special case of zero

        val timeUnits = listOf("days", "hours", "minutes", "seconds")
        val timeValues = listOf(
            86400000L,
            3600000L,
            60000L,
            1000L
        ) // Milliseconds in a day, hour, minute, second, and millisecond

        var remainingTime = milliseconds
        val result = StringBuilder()

        for ((index, unit) in timeUnits.withIndex()) {
            val value = remainingTime / timeValues[index]
            if (value > 0) {
                result.append("$value ${if (value == 1L) unit.substringBefore("s") else unit} ")
                remainingTime %= timeValues[index]
            }
        }

        return result.trim().toString()
    }

    fun parseDateTime(
        string: String,
        timeZone: TimeZone = TimeZone.getDefault(),
        now: Long = System.currentTimeMillis()
    ): Long? {
        return when (string) {
            "tomorrow" -> now + parseTime("1d")
            "nextWeek" -> now + parseTime("1w")
            "nextMonth" -> now + parseTime("1mo")
            "nextYear" -> now + parseTime("1y")
            else -> {
                val (pattern, format) = when (string.count { it == '-' }) {
                    4 -> Pair(string.substring(0, string.lastIndexOf("-")), "yyyy-MM-dd-hh:mma")
                    3 -> Pair(string, "yyyy-MM-dd-hh:mma")
                    2 -> Pair(string, "yyyy-MM-dd")
                    0 -> Pair(string, "hh:mma")
                    else -> return null
                }
                val formatter = DateTimeFormatter.ofPattern(format)
                val localDateTime = LocalDateTime.parse(pattern, formatter)
                val zoneId = timeZone.toZoneId()
                localDateTime.atZone(zoneId).toInstant().toEpochMilli()
            }
        }
    }

    fun parseTime(string: String): Long {
        var totalTime = 0L
        for (result in TIME_REGEX.findAll(string)) {
            if (result.groupValues.size != 3) continue
            val number = result.groupValues[1].toLongOrNull() ?: continue
            val multiplier = TIME_MAP_MS[result.groupValues[2]] ?: continue
            totalTime += number * multiplier
        }
        return totalTime
    }

    fun getTimeZone(ip: String): TimeZone? {
        return try {
            val url = URI("https://api.ipdata.co/$ip?api-key=${BlueFox.getAPIKey("ipdata")}").toURL()
            val config = WebUtil.getConfig(url) ?: return null
            val zoneId = config.getString("time_zone.name")
            TimeZone.getTimeZone(zoneId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

val Number.ticks: Long get() = this.toLong()
val Number.seconds: Long get() = this.toDouble().times(20).toLong()
val Number.minutes: Long get() = this.toDouble().times(1200).toLong()
val Number.hours: Long get() = this.toDouble().times(72000).toLong()
