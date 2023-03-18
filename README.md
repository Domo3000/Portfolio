# Portfolio

Hi, this is my personal portfolio!

Under construction.

## Technology

It's a full-stack application written in `Kotlin`.

### Backend

`Kotlin/JVM`

[Application](server/src/main/kotlin/Application.kt) uses [Ktor](https://ktor.io/) to start a Server.

It uses `kotlinx.html` to generate a basic HTML.

### Frontend

`Kotlin/JS` using the React wrapper

I split it up into several modules as I didn't want the Javascript files to get too large.

#### Browser

This is the main module.

[client.kt](browser/src/main/kotlin/client.kt) is the main entry point where the [Overview](browser/src/main/kotlin/overview/Overview.kt) gets rendered.

#### Connect4

This is a small game that uses `ktor-client-websockets`.

[client.kt](connect4/prod/src/main/kotlin/client.kt) waits until it receives a Connect4 Event until renders the game.

### Build

Task `shadowJar` uses [Gradle Shadow](https://github.com/johnrengelman/shadow) to generate a fat JAR.