package db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

object Result : IntIdTable() {
    val player = varchar("player", 20)
    val points = double("points")
}

class ResultDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ResultDao>(Result) {
        fun insert(player: String, points: Double) = transaction {
            new {
                this.player = player
                this.points = points
            }
        }

        fun insert(player: String, points: List<Double>) = transaction {
            Result.batchInsert(points) {
                this[Result.player] = player
                this[Result.points] = it
            }
        }

        fun getAll() = transaction {
            all().toList().groupBy { it.player }
        }

        fun deleteByPlayer(player: String) = transaction {
            Result.deleteWhere { Result.player eq player }
        }
    }

    var player by Result.player
    var points by Result.points
}
