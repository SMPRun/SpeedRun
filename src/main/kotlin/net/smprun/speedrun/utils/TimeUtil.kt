package net.smprun.speedrun.utils

object TimeUtil {
    
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val millis = milliseconds % 1000
        
        return when {
            days > 0 -> String.format("%dd %dh %dm %ds %dms", days, hours, minutes, seconds, millis)
            hours > 0 -> String.format("%dh %dm %ds %dms", hours, minutes, seconds, millis)
            minutes > 0 -> String.format("%dm %ds %dms", minutes, seconds, millis)
            else -> String.format("%ds %dms", seconds, millis)
        }
    }
}