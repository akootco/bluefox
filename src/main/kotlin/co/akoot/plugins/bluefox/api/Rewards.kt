package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.extensions.touch

object Rewards {
    private val config = FoxConfig(BlueFox.instance.dataFolder.resolve("rewards.conf").touch("{}"))
    object LoginStreak {
        val dailyAmount: Double by config default 10.0 from "loginStreak"
        val minimum: Double by config default 35.0 from "streakBonus.minutesPerWeek"
    }

    object Role {
        val loyalist: Double by config default 1.5 from "role"
        val devoted: Double by config default 2.0 from "role"
        val pro: Double by config default 2.0 from "role"
        val vip: Double by config default 3.0 from "role"
        val investor: Double by config default 4.0 from "role"
        val stakeholder: Double by config default 5.0 from "role"
    }

    object Donation {
        val donator: Double by config default 110.0 from "donation"
        val generousDonator: Double by config default 2020.0 from "donation"
        val bountifulDonator: Double by config default 4200.0 from "donation"
        val nobleDonator: Double by config default 6700.0 from "donation"
        val philanthropist: Double by config default 15000.0 from "donation"
    }
}