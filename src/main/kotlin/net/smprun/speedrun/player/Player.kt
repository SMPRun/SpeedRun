package net.smprun.speedrun.player

import java.util.*

data class Player(
    val uuid: UUID,
    val username: String,
    val wins: Int = 0,
    val bestTime: Long? = null,
    val recentTime: Long? = null
) {
    fun addWin(winTime: Long): Player {
        val newBestTime = if (bestTime == null || winTime < bestTime) winTime else bestTime
        return copy(
            wins = wins + 1,
            bestTime = newBestTime,
            recentTime = winTime
        )
    }
}
