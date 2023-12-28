package foo.vide.hslnyt.repo

interface SettingsRepository {
    val locationUpdateFrequencySeconds: Int
    val maxStops: Int
    val maxRadiusMeters: Int

    companion object {
        val Preview = SettingsRepositoryImpl()
    }
}

class SettingsRepositoryImpl : SettingsRepository {
    override val locationUpdateFrequencySeconds = 30
    override val maxRadiusMeters = 2000
    override val maxStops = 50
}