# ChessPulse

A Jetpack Compose Android app for chess study and improvement — built for players who want to turn Lichess studies into actionable learning.

**Version:** 1.0 (v1.0)  
**Min SDK:** API 26 (Android 8.0) | **Target SDK:** API 36  
**Language:** Kotlin | **UI:** Jetpack Compose (Material 3)

---

## Features

- **Lichess integration** — fetch and browse public Lichess studies directly in-app via the Lichess REST API
- **Study viewer** — read through chess chapters/steps with move navigation
- **PGN parsing** — parse and display PGN moves using [chesslib](https://github.com/bhlangonijr/chesslib)
- **Auth** — Firebase Authentication sign-in / sign-up flow
- **Progress tracking** — Firestore-backed user profiles and study progress
- **Dark mode** — built-in dark theme with Material 3

## Tech Stack

| Layer | Library |
|---|---|
| **UI** | Jetpack Compose (Material 3), Material Icons |
| **Network** | Retrofit 2 + Gson + OkHttp logging interceptor |
| **Backend** | Firebase Auth, Cloud Firestore (BOM 34.16.0) |
| **Chess logic** | chesslib 1.3.7 |
| **Coroutines** | kotlinx-coroutines-play-services |

## Development Notes

- Layout uses responsive padding; smaller screens scroll login content and pad system bars + keyboard insets
- Ships without native C++ — pure Kotlin + managed libs
- Currently pre-release (v1.0): feedback welcome via GitHub issues

## Contributing

Pull requests welcome — see `JARVIS` branch for active work.  
Open issues for bugs or feature notes; keep PRs scoped.

## License

Closed source — personal/portfolio project by Mohamed Safwat ([@Savotageofficial](https://github.com/Savotageofficial)).
