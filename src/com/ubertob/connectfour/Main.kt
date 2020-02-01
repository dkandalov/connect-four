package com.ubertob.connectfour

import com.ubertob.connectfour.Column.*
import com.ubertob.connectfour.Player.Cross
import com.ubertob.connectfour.Player.Nought

/*
 * + empty board
 * + board with player1 chip
 * + pile up chips
 * + player2
 *
 * + Pile class
 * + find winner
 */
fun main() {

    val moves = listOf(
        c, c,
        a, a,
        a, b,
        a, b,
        a, b,
        d, d
    )

    val emptyBoard = Board()
    moves
        .fold(emptyBoard) { board, move ->
            if (board.hasWinner) board
            else {
                board.show()
                board.place(move)
            }
        }
        .show()
}

enum class Column {
    a, b, c, d, e, f, g
}

enum class Row {
    r1, r2, r3, r4, r5, r6
}

enum class Player(val sign: Char) {
    Cross('X'), Nought('O')
}

data class Move(val player: Player, val column: Column)

data class Pile(val players: List<Player>) {
    fun player(row: Row): Char {
        return if (players.size > row.ordinal)
            players[row.ordinal].sign
        else
            ' '
    }
}

fun Row.render(piles: List<Pile>): String {
    return piles.joinToString(separator = "|", prefix = "|", postfix = "|") {
        it.player(this).toString()
    }
}

data class Position(val column: Column, val row: Row) {
    fun south(): Position? =
        if (row == Row.r1) null
        else copy(row = Row.values()[row.ordinal - 1])

    fun east(): Position? =
        if (column == g) null
        else copy(column = Column.values()[column.ordinal + 1])

    fun west(): Position? =
        if (column == a) null
        else copy(column = Column.values()[column.ordinal - 1])
}

class Board(
    private val moves: List<Move> = emptyList()
) {
    private val player: Player =
        if (moves.isEmpty()) Nought
        else moves.last().player.opponent()

    private val piles = Column.values()
        .map { column -> moves.filter { it.column == column } }
        .map { Pile(it.map { it.player }) }

    val hasWinner: Boolean get() = winner() != null

    private fun winner(): Player? =
        if (moves.isEmpty()) null
        else checkWinner(moves.last().column)

    private fun playerAtPosition(position: Position): Player? =
        piles[position.column.ordinal].run {
            if (players.size > position.row.ordinal)
                players[position.row.ordinal]
            else
                null
        }

    private fun checkWinner(column: Column): Player? {
        val pile = piles[column.ordinal]
        if (pile.players.isEmpty()) return null
        val player = pile.players.last()
        val position = Position(column, Row.values()[pile.players.size - 1])

        val vertical = countDirection(position, Position::south, player) >= 4
        val horizontal = countDirection(position, Position::east, player) +
            countDirection(position, Position::west, player) - 1 >= 4

        return if (vertical || horizontal) player else null
    }

    private fun countDirection(position: Position, f: Position.() -> Position?, player: Player) =
        go(position, f)
            .takeWhile { playerAtPosition(it) == player }
            .count()

    private fun go(position: Position, f: Position.() -> Position?): List<Position> {
        return listOf(
            position,
            position.f(),
            position.f()?.f(),
            position.f()?.f()?.f()
        ).takeWhile { it != null }.filterNotNull()
    }

    fun show() {
        Row.values().reversed().forEach {
            println(it.render(piles))
        }
        println("|-+-+-+-+-+-+-|")
        println(
            values().joinToString("+", prefix = "|", postfix = "|") {
                it.name
            }
        )
        winner().let {
            if (it == null) println("\nThere is no winner")
            else println("\nThe winner is $it")
        }
    }

    fun place(column: Column): Board {
        return Board(moves + Move(player, column))
    }
}

private fun Player.opponent() =
    when (this) {
        Nought -> Cross
        Cross  -> Nought
    }
