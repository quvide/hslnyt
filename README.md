# Pysäkkilive

Pysäkkilive is a native Android application designed to give real-time information on bus stop
departures near the user in the HSL region. It is implemented using
[Jetpack Compose](https://developer.android.com/jetpack/compose).

The motivation of developing this application in addition to the existing route planners is to

1. keep the application as *lightweight as possible*,
2. to require *minimal user interaction* and
3. to be *information dense*.

The official HSL application currently takes 224 MB of space when installed and is relatively
resource intensive. It also requires manual user interaction (searching on the map) to discover
stops. In comparison, this app currently requires under 1 MB of storage space.

## Screenshots

![docs/Screenshot_20231225_153416.png](docs/Screenshot_20231225_153416.png)

## Planned features

* Support for all Digitraffic regions
* Map with updating vehicle positions
* Separate station UI
* Internationalization

## Licensing

This project is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html#license-text).
A copy is provided in [LICENSE](LICENSE).