package com.gamasoft.kakomu.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GoStringTest {
    @Test
    fun simplestMerge(){
        val b = Board(9,9)

        val p1 = Point(2,2)
        val s1 = GoString(Player.BLACK, stones = setOf(p1), liberties = b.neighbors(p1))


        val p2 = Point(2,3)
        val s2 = GoString(Player.BLACK, stones = setOf(p2), liberties = b.neighbors(p2))

        val newString = s2.mergeWith(s1)

        assertEquals(2, newString.stones.size)
        assertTrue( newString.stones.contains(p1))
        assertTrue( newString.stones.contains(p2))
        assertEquals(6, newString.liberties.size)
    }
}