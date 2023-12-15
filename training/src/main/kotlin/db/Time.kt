package db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration

object Time : IntIdTable() {
    val player = varchar("player", 20)
    val milliseconds = long("milliseconds")
}

private fun List<TimeDao>.toMillis() = map { it.milliseconds }.average() / 1000.0

class TimeDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TimeDao>(Time) {
        fun insert(player: String, duration: Duration) = transaction {
            new {
                this.player = player
                this.milliseconds = duration.inWholeMilliseconds
            }
        }

        fun getAll() = transaction {
            all().toList().groupBy { it.player }.mapValues { it.value.toMillis() }
        }

        fun getByPlayer(player: String) = transaction {
            find {
                Time.player eq player
            }.orderBy(Time.id to SortOrder.ASC).toList().toMillis()
        }
    }

    var player by Time.player
    var milliseconds by Time.milliseconds
}
