package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitScheduler

fun sync(runnable: Runnable) {
    BlueFox.instance.apply{
        server.scheduler.runTask(this, runnable)
    }
}

fun async(runnable: Runnable) {
    BlueFox.instance.apply{
        server.scheduler.runTaskAsynchronously(this, runnable)
    }
}

fun runLater(ticks: Long = 1, runnable: Runnable) {
    BlueFox.instance.apply{
        server.scheduler.runTaskLater(this, runnable, ticks)
    }
}

fun runLaterAsync(ticks: Long = 1, runnable: Runnable) {
    BlueFox.instance.apply{
        server.scheduler.runTaskLaterAsynchronously(this, runnable, ticks)
    }
}