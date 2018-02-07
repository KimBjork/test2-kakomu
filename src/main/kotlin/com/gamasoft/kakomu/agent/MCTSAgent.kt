package com.gamasoft.kakomu.agent

import com.gamasoft.kakomu.model.Evaluator
import com.gamasoft.kakomu.model.GameState
import com.gamasoft.kakomu.model.Move
import com.gamasoft.kakomu.model.Player


class MCTSAgent(val numRounds: Int, val temperature: Double, val boardSize: Int): Agent {
//1.5 is a good starting point temperature
//hotter will explore more not so promising moves
//colder will evaluate better most promising moves
    val bots: Map<Player, Agent>

    init {
        bots = mapOf<Player, Agent>(
                Player.BLACK to RandomBot(boardSize),
                Player.WHITE to RandomBot(boardSize))
    }


    fun selectChild(node: MCTSNode): MCTSNode {
        //Select a child according to the upper confidence bound for trees (UCT) metric.

        val totalRollouts = node.children.sumBy { c -> c.rollouts }
        val logRollouts = Math.log(totalRollouts.toDouble())

        var bestScore = -1.0
        var bestChild: MCTSNode? = null
        //Loop over each child.
        for (child in node.children) {
            // Calculate the UCT score.
            val winPercentage = child.winningPct(node.gameState.nextPlayer)
            val explorationFactor = Math.sqrt(logRollouts / child.rollouts)
            val uctScore = winPercentage + temperature * explorationFactor
            // Check if this is the largest we've seen so far.
            if (uctScore > bestScore) {
                bestScore = uctScore
                bestChild = child
            }
        }

        return bestChild!!
    }

    override fun playNextMove(gameState: GameState): GameState {

        val root = MCTSNode(gameState)

        for (i in 1..numRounds) {
            var node = root

            while (!node.canAddChild() && !node.isTerminal()) {
                node = selectChild(node)
            }

            //Add a new child node into the tree.
            if (node.canAddChild()) {
                node = node.addRandomChild()
            }

            //Simulate a random game from this node.
            val winner = Evaluator.simulateRandomGame(node.gameState, bots)

            var parent: MCTSNode? = node
            //Propagate scores back up the tree.
            while (parent != null) {
                parent.recordWin(winner)
                parent = parent.parent
            }

            if (i % 5000 == 0)
                println(i)
        }

        //Having performed as many MCTS rounds as we have time for, we
        //now pick a move.
        var bestMove: GameState = gameState
        var bestPct = -1.0
        for (child in root.children) {
            val childPct = child.winningPct(gameState.nextPlayer)
            if (childPct > bestPct) {
                bestPct = childPct
                bestMove = child.gameState
            }
            val coords = child.gameState.lastMove!!.humanReadable()
            println("    considered move $coords with win pct $childPct on ${child.rollouts} rollouts")
        }

        if (bestPct <= 0.15) //let's do the right thing
            bestMove = GameState(gameState.board, gameState.nextPlayer,gameState.previous, Move.Resign)

        println("Select move ${bestMove.lastMove!!.humanReadable()} with win pct $bestPct")
        return bestMove

    }
}
