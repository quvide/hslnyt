package foo.vide.hslnyt.util

object FormatTime {
    fun timeToHHMMSS(unixTS: Long?): String {
        if (unixTS == null) return "??:??:??"
        val hours = (unixTS / (60 * 60))
        val minutes = (unixTS - hours * 60 * 60) / 60
        val seconds = (unixTS - hours * 60 * 60 - minutes * 60)

        val sH = (hours % 24).toString().padStart(2, '0')
        val sM = minutes.toString().padStart(2, '0')
        val sS = seconds.toString().padStart(2, '0')
        return "$sH:$sM:$sS"
    }

    fun timeToMMSS(unixTS: Long?): String {
        if (unixTS == null) return "??:??"

        val minutes = unixTS / 60
        val seconds = (unixTS - minutes * 60)

        val sM = minutes.toString().padStart(2, '0')
        val sS = seconds.toString().padStart(2, '0')
        return "$sM:$sS"
    }
}