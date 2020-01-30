package com.ubertob.connectfour

import com.ubertob.connectfour.Column.Companion.a
import com.ubertob.connectfour.Column.Companion.b
import com.ubertob.connectfour.Column.Companion.c
import com.ubertob.connectfour.Player.Nought

/*
 * + empty board
 * + board with player1 chip
 * + pile up chips
 * + player2
 *
 * + Pile
 * + simple winner rules
 * + actual winner rules
 * + console input
 */
fun main() {
    val moves = listOf(
        c, c,
        a, a,
        a, b,
        a, b,
        a, b,
        a, b
    ).iterator()

    fun nextColumn_(): Column = moves.next()

    fun nextColumn(): Column {
        val char = readLine()!!.first()
        return Column(char - 'a')
    }

    val nextBoard = { board: Board ->
        board.place(nextColumn_())
    }
    generateSequence(Board(), nextBoard)
        .onEach { it.show() }
        .takeWhile { it.winner == null }
        .count()
}

data class Column(val index: Int, val name: String = ('a' + index).toString()) {
    operator fun minus(n: Int) = Column(index - n)
    operator fun plus(n: Int) = Column(index + n)

    companion object {
        val a = Column(0)
        val b = Column(1)
        val c = Column(2)
        val d = Column(3)
        val e = Column(4)
        val f = Column(5)
        val g = Column(6)

        val values = listOf(a, b, c, d, e, f, g)
    }
}

data class Row(val index: Int) {
    operator fun minus(n: Int) = Row(index - n)
    operator fun plus(n: Int) = Row(index + n)

    companion object {
        val values = (0..5).map(::Row)
    }
}

enum class Player(val sign: Char) {
    Cross('X'), Nought('O');

    fun opponent() =
        when (this) {
            Nought -> Cross
            Cross  -> Nought
        }
}

data class Coord(val column: Column, val row: Row)

fun Coord.north() = Coord(column, row + 1)
fun Coord.south() = Coord(column, row - 1)
fun Coord.west() = Coord(column - 1, row)
fun Coord.east() = Coord(column + 1, row)

fun Coord.northSequence() = generateSequence(north(), { it.north() })
fun Coord.southSequence() = generateSequence(south(), { it.south() })
fun Coord.westSequence() = generateSequence(west(), { it.west() })
fun Coord.eastSequence() = generateSequence(east(), { it.east() })
fun Coord.northWestSequence() = generateSequence(north().west(), { it.north().west() })
fun Coord.northEastSequence() = generateSequence(north().east(), { it.north().east() })
fun Coord.southWestSequence() = generateSequence(south().west(), { it.south().west() })
fun Coord.southEastSequence() = generateSequence(south().east(), { it.south().east() })

data class Move(val player: Player, val column: Column)

data class Pile(val chips: List<Player>) {
    fun render(row: Row): Char =
        if (chips.size > row.index) chips[row.index].sign else ' '

    companion object {
        val empty = Pile(emptyList())
    }
}

class Board(private val moves: List<Move> = emptyList()) {
    private val player: Player =
        if (moves.isEmpty()) Nought
        else moves.last().player.opponent()

    private val pileByColumn = moves
        .groupBy { it.column }
        .mapValues { Pile(it.value.map { it.player }) }

    val winner: Player? = findWinner()

    fun place(column: Column): Board {
        if (winner != null) return this
        return Board(moves + Move(player, column))
    }

    fun show() {
        Row.values.reversed().forEach { println(it.render()) }
        println("|-+-+-+-+-+-+-|")
        println(
            Column.values.joinToString("+", prefix = "|", postfix = "|") {
                it.name
            }
        )

        if (winner == null) println("\nThere is no winner")
        else println("\nThe winner is $winner")
    }

    private fun Row.render(): String {
        val rowChars = Column.values.map { column ->
            (pileByColumn[column] ?: Pile.empty).render(row = this)
        }
        return rowChars.joinToString(separator = "|", prefix = "|", postfix = "|")
    }

    private fun findWinner(): Player? {
        val move = moves.lastOrNull() ?: return null
        val column = move.column
        val pile = pileByColumn[column] ?: return null
        val row = Row(pile.chips.size - 1)
        if (row.index < 3) return null
        val coord = Coord(column, row)
        val player = playerAt(coord)

        val verticalCount = 1 +
            coord.northSequence().takeWhile { playerAt(it) == player }.count() +
            coord.southSequence().takeWhile { playerAt(it) == player }.count()

        val horizontalCount = 1 +
            coord.westSequence().takeWhile { playerAt(it) == player }.count() +
            coord.eastSequence().takeWhile { playerAt(it) == player }.count()

        val diaginal1Count = 1 +
            coord.northWestSequence().takeWhile { playerAt(it) == player }.count() +
            coord.southEastSequence().takeWhile { playerAt(it) == player }.count()

        val diaginal2Count = 1 +
            coord.northEastSequence().takeWhile { playerAt(it) == player }.count() +
            coord.southWestSequence().takeWhile { playerAt(it) == player }.count()

        return if (
            verticalCount >= 4 || horizontalCount >= 4 ||
            diaginal1Count >= 4 || diaginal2Count >= 4
        ) player else null
    }

    private fun playerAt(coord: Coord): Player? {
        val pile = pileByColumn[coord.column] ?: return null
        return if (pile.chips.size <= coord.row.index) null
        else pile.chips[coord.row.index]
    }
}
