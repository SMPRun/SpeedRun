package net.smprun.speedrun.winner

data class WinnersList(
    val winners: List<WinnerRecord> = emptyList()
) {
    fun getBestTimeWinner(): WinnerRecord? = winners.minByOrNull { it.winTime }
}
