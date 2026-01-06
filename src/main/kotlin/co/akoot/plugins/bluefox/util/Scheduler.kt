package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask

fun sync(runnable: Runnable): BukkitTask {
    return BlueFox.instance.let {
        it.server.scheduler.runTask(it, runnable)
    }
}

fun async(runnable: Runnable): BukkitTask {
    return BlueFox.instance.let {
        it.server.scheduler.runTaskAsynchronously(it, runnable)
    }
}

fun runLater(ticks: Long = 1, runnable: Runnable): BukkitTask {
    return BlueFox.instance.let {
        it.server.scheduler.runTaskLater(it, runnable, ticks)
    }
}

fun runLaterAsync(ticks: Long = 1, runnable: Runnable): BukkitTask {
    return BlueFox.instance.let {
        it.server.scheduler.runTaskLaterAsynchronously(it, runnable, ticks)
    }
}

fun loop(period: Long, delay: Long = 0, runnable: Runnable): Int {
    return BlueFox.instance.let {
        it.server.scheduler.scheduleSyncRepeatingTask(it, runnable, delay, period)
    }
}

fun loopAsync(period: Long, delay: Long = 0, runnable: Runnable): Int {
    return BlueFox.instance.let {
        it.server.scheduler.scheduleSyncRepeatingTask(it, runnable, delay, period)
    }
}

class Promise(val task: BukkitTask? = null) {
    fun thenAsync(ticks: Long = 1, runnable: Runnable): Promise {
        return Promise(runLaterAsync(ticks, runnable))
    }

    fun then(ticks: Long = 1, runnable: Runnable): Promise {
        return Promise(runLater(ticks, runnable))
    }
}