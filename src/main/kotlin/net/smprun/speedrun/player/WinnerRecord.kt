package net.smprun.speedrun.player

import java.time.Instant
import java.util.*

data class WinnerRecord(
    val uuid: UUID,
    val username: String,
    val winTime: Long,
    val timestamp: Instant = Instant.now()
)

