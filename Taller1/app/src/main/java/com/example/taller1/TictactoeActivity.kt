package com.example.taller1

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller1.databinding.ActivityTictactoeBinding

class TictactoeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTictactoeBinding

    var jug1 = true;
    var gameBoard = Array(3) { arrayOf("", "", "") }
    var gameOver = false

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTictactoeBinding.inflate(layoutInflater)
        val view = binding.root; setContentView(view)
        binding.btn1.setOnClickListener { handleMove(0, 0, binding.btn1) }
        binding.btn2.setOnClickListener { handleMove(0, 1, binding.btn2) }
        binding.btn3.setOnClickListener { handleMove(0, 2, binding.btn3) }
        binding.btn4.setOnClickListener { handleMove(1, 0, binding.btn4) }
        binding.btn5.setOnClickListener { handleMove(1, 1, binding.btn5) }
        binding.btn6.setOnClickListener { handleMove(1, 2, binding.btn6) }
        binding.btn7.setOnClickListener { handleMove(2, 0, binding.btn7) }
        binding.btn8.setOnClickListener { handleMove(2, 1, binding.btn8) }
        binding.btn9.setOnClickListener { handleMove(2, 2, binding.btn9) }
        binding.btnNewGame.setOnClickListener {
            gameBoard = Array(3) { arrayOf("", "", "") };jug1 = true;gameOver = false
            val allButtons = listOf(binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9)
            for (button in allButtons) {
                button.text = "";button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray));button.isEnabled = true
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun handleMove(row: Int, col: Int, button: Button) {
        if (button.text.isNotEmpty() || gameOver) return

        val symbol = if (jug1) "X" else "O"
        button.text = symbol
        if (symbol == "X") button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.red)) else button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.blue))
        gameBoard[row][col] = symbol
        jug1 = !jug1 // Toggle turn
        checkWinner()
    }

    private fun checkWinner() {
        val winningCombinations = listOf(
            // Rows
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),
            // Columns
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),
            // Diagonals
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        )

        for (combo in winningCombinations) {
            val (r1, c1) = combo[0]
            val (r2, c2) = combo[1]
            val (r3, c3) = combo[2]

            if (gameBoard[r1][c1] == gameBoard[r2][c2] &&
                gameBoard[r2][c2] == gameBoard[r3][c3] &&
                gameBoard[r1][c1].isNotEmpty()) {

                Toast.makeText(this, "Winner: ${gameBoard[r1][c1]}", Toast.LENGTH_LONG).show()
                disableAllButtons()
                gameOver = true
                return
            }
        }
    }

    private fun disableAllButtons() {
        val allButtons = listOf(binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9)
        for (button in allButtons) {
            button.isEnabled = false
        }
    }

}