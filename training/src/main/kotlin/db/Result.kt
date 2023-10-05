package db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

object Result : IntIdTable() {
    val player = varchar("player", 10)
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

        fun getAll() = transaction {
            all().toList()
        }

        fun getByPlayer(player: String) = transaction {
            find {
                Result.player eq player
            }.orderBy(Result.id to SortOrder.ASC).toList().map { it.points }
        }

        fun deleteByPlayer(player: String) = transaction {
            Result.deleteWhere { Result.player eq player }
        }

        fun lowest(count: Int): List<String> = transaction {
            all()
                .groupBy { it.player }
                .map { (name, scores) ->
                    name to scores.size
                }
                .sortedBy { it.second }
                .take(count)
                .map { it.first }
        }
    }

    var player by Result.player
    var points by Result.points
}
