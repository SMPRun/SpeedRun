package net.smprun.speedrun.winner

data class WinnersList(
    val winners: List<WinnerRecord> = emptyList()
) {
    fun getBestTimeWinner(): WinnerRecord? = winners.minByOrNull { it.winTime }
    
    fun getTopWinners(count: Int): List<WinnerRecord> = 
        winners.sortedBy { it.winTime }.take(count)
}
