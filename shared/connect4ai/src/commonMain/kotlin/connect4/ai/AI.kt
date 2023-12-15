package connect4.ai

import connect4.game.Connect4Player
import java.time.Instant
import kotlin.random.Random

abstract class AI(val random: Random) : Connect4Player() {
    constructor(seed: Long?) : this(Random(seed ?: Instant.now().toEpochMilli()))
}