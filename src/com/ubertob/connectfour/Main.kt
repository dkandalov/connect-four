package com.ubertob.connectfour

import com.ubertob.connectfour.Column.*
import com.ubertob.connectfour.Player.Cross
import com.ubertob.connectfour.Player.Nought

/*
 * + empty board
 * + board with player1 chip
 * + pile up chips
 * + player2
 */
fun main() {

    val moves = listOf(
        c, c,
        a, a,
        a, b,
        a, b,
        a, b,
        a, b
    )

    val emptyBoard = Board()
    moves
        .fold(emptyBoard) { board, move -> board.place(move) }
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

fun Row.render(moves: List<Move>): String {

    val rowChars = Column.values().map { column ->
        val pile = moves.filter { column == it.column }
        if (pile.size > this.ordinal)
            pile[this.ordinal].player.sign
        else
            ' '
    }

    return rowChars.joinToString(separator = "|", prefix = "|", postfix = "|")
}

class Board(
    private val moves: List<Move> = emptyList()
) {
    private val player: Player =
        if (moves.isEmpty()) Nought
        else moves.last().player.opponent()

    fun show() {
        Row.values().reversed().forEach {
            println(it.render(moves))
        }
        println("|-+-+-+-+-+-+-|")
        println(
            values().joinToString("+", prefix = "|", postfix = "|") {
                it.name
            }
        )
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
