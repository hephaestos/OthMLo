package com.example.othmlo

import android.app.ActionBar
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.TableRow.LayoutParams
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    var boardSize: String? = null
    var chipButton: Button? = null
    var subText: TextView? = null
    var currDisc: BoardPiece = BoardPiece.WHITE
    var tbLayout: TableLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subText = findViewById(R.id.welcomeSubtext)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_board_size) {
            createBoardDialog(this)
            return true
        }
        return false
    }

    private fun createBoardDialog(context: Context) {
        val textInputLayout = TextInputLayout(context)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        textInputLayout.addView(input)

        val builder = AlertDialog.Builder(context)
            .setTitle("Enter Board Size")
            .setView(textInputLayout)
            .setMessage("Enter an even integer 4-10")
            .setPositiveButton("OK") { dialog, _ ->
                if (input.text.toString().toInt() in 4..10 step 2) {
                    boardSize = input.text.toString()
                    subText?.text = ""
                    // Dynamically creates board on screen based on user input
                    // TODO: Add listeners to each of the views added
                    tbLayout = findViewById<TableLayout>(R.id.tableLayout)
                    tbLayout?.removeAllViewsInLayout()
                    val board = boardSize?.toInt()
                    for (i in 0 until board!!) {
                        val tr = TableRow(this)
                        var count = 0
                        for (j in 0 until board) {
                            if (i == ((board / 2) -1) && j == (board / 2) - 1 || (i == board / 2) && j == board / 2)
                                tr.addView(createNewImage(R.drawable.black_piece, i.toString() + j.toString(), "BLACK"))
                            else if (((i == board / 2) && j == (board / 2) - 1 || i == (board / 2) - 1 && j == board / 2))
                                tr.addView(createNewImage(R.drawable.white_piece, i.toString() + j.toString(), "WHITE"))
                            else
                                tr.addView(createNewImage(R.drawable.grid_blank, i.toString() + j.toString(), "EMPTY"))
                            count++
                        }
                        tr.gravity = Gravity.CENTER
                        tbLayout?.addView(tr)
                    }
                    dialog.cancel()
                } else {
                    // TODO: Logic to keep dialog box open on bad input?
                }
            }
            .setNegativeButton("Cancel") {dialog, _ ->
                dialog.cancel()
            }.create()
        builder.show()
    }

    private fun createNewImage(@DrawableRes resID: Int, imgID: String, tag: String): ImageView {
        val newImg = ImageView(this)
        newImg.id = imgID.toInt()
        newImg.setImageResource(resID)
        newImg.tag = tag
        newImg.setOnClickListener(createImgListener(newImg))
        return newImg
    }

    private fun createImgListener(view: ImageView): View.OnClickListener {
        var clickListener = View.OnClickListener{
            // Debugging statement
            Log.d("HELLLOOOOOOOOOOOOOO", checkWinner().toString())
        }
        return clickListener
    }

    // TODO: Need to implement
//    private fun placeDisc(row: Int, col: Int) {
//        if (!isValidMove(row, col))
//            return
//
//        if (checkWest(row, col, currDisc))
//    }

    // TODO: Need to test
    private fun isValidMove(imgID: String): Boolean {
        return isValidMoveForDisc(imgID)
    }

    // TODO: Need to test
    private fun isValidMoveForDisc(imgID: String): Boolean {
        if (checkNorth(imgID) || checkSouth(imgID) || checkEast(imgID) || checkWest(imgID) ||
                checkNortheast(imgID) || checkNorthwest(imgID) || checkSoutheast(imgID) ||
                    checkSouthwest(imgID))
                        return true
        return false
    }

    // TODO: Need to test
    private fun isValidMoveAvailable(): Boolean {
        return isValidMoveAvailableForDisc()
    }

    // TODO: Need to test
    private fun isValidMoveAvailableForDisc(): Boolean {
        val board = boardSize?.toInt()
        for (row in 0 until board!!) {
            for (col in 0 until board) {
                val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())
                val currTag = BoardPiece.valueOf(currImg?.tag as String)
                if (currTag == BoardPiece.EMPTY)
                    if (isValidMoveForDisc(row.toString() + col.toString()))
                        return true
            }
        }
        return false
    }

    // TODO: Need to test
    private fun isBoardFull(): Boolean {
        val board = boardSize?.toInt()
        for (row in 0 until board!!) {
            for (col in 0 until board) {
                val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())
                val currTag = BoardPiece.valueOf(currImg?.tag as String)
                if (currTag == BoardPiece.EMPTY)
                    return false
            }
        }
        return true
    }

    // TODO: Need to test
    private fun isGameOver(): Boolean {
        val boardStatus = isBoardFull()
        val disc = currDisc

        val gameOverBlack = if (currDisc == BoardPiece.WHITE) {
            currDisc = BoardPiece.BLACK
            !isValidMoveAvailableForDisc()
        } else
            !isValidMoveAvailableForDisc()

        val gameOverWhite = if (currDisc == BoardPiece.BLACK) {
            currDisc = BoardPiece.WHITE
            !isValidMoveAvailableForDisc()
        } else
            !isValidMoveAvailableForDisc()

        currDisc = disc

        return boardStatus || (gameOverBlack && gameOverWhite)
    }

    // TODO: Need to test
    private fun checkWinner(): BoardPiece {
        var numBlack = 0
        var numWhite = 0

        val board = boardSize?.toInt()
        for (row in 0 until board!!) {
            for (col in 0 until board) {
                val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())
                val currTag = BoardPiece.valueOf(currImg?.tag as String)
                if (currTag == BoardPiece.BLACK)
                    numBlack++
                if (currTag == BoardPiece.WHITE)
                    numWhite++
            }
        }

        if (numBlack > numWhite)
            return BoardPiece.BLACK
        else if (numWhite > numBlack)
            return BoardPiece.WHITE
        else
            return BoardPiece.TIE
    }

    // TODO: Need to test
    private fun prepareNextTurn() {
        currDisc = if (currDisc == BoardPiece.WHITE)
            BoardPiece.BLACK
        else
            BoardPiece.WHITE
    }



    // TODO: This is where you left off
    private fun checkNorth(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }

        for (i in (row - 1) downTo 0) {
            if (i < 0)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + col.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
        }
        return false
    }

    private fun checkSouth(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()

        for (i in (row + 1) until board!!) {
            if (i == board)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + col.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
        }
        return false
    }

    private fun checkEast(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()

        for (i in (col + 1) until board!!) {
            if (i == board)
                break
            val currImg = tbLayout?.findViewById<ImageView>((row.toString() + i.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
        }
        return false
    }

    private fun checkWest(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }

        for (i in (col - 1) downTo 0) {
            if (i < 0)
                break
            val currImg = tbLayout?.findViewById<ImageView>((row.toString() + i.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
        }
        return false
    }

    private fun checkNortheast(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()
        var j = col + 1

        for (i in (row - 1) downTo 0) {
            if (i < 0 || j == board)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + j.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
            j++
        }
        return false
    }

    private fun checkSouthwest(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()

        var j = col - 1
        for (i in (row + 1) until board!!) {
            if (i == board || j < 0)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + j.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
            j--
        }
        return false
    }

    private fun checkSoutheast(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()

        var j = col + 1
        for (i in (row + 1) until board!!) {
            if (i == board || j == board)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + j.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
            j++
        }
        return false
    }

    private fun checkNorthwest(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        var row = 0
        var col = 0
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        val board = boardSize?.toInt()
        var j = col - 1

        for (i in (row - 1) downTo 0) {
            if (i < 0 || j < 0)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + j.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag as String)
            if (currTag == currDisc) {
                numDisc++
                if (numDisc > 0 && numNotDisc == 0 || numEmpty > 0)
                    return false
                if (numNotDisc > 0 && numDisc > 0 )
                    return true
            }
            if (currTag != currDisc && currTag != BoardPiece.EMPTY)
                numNotDisc++
            if (currTag == BoardPiece.EMPTY)
                numEmpty++
            j--
        }
        return false
    }

    /**
     * Function courtesy of @Sean
     * https://stackoverflow.com/questions/2967898/retrieving-the-first-digit-of-a-number/2968068
     */
    private fun firstDigit(a: Int): Int {
        var num = a
        while (num > 9)
            num /= 10

        return num
    }
}