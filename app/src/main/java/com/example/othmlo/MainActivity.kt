package com.example.othmlo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private var boardSize: String? = null
    private var currTurn: TextView? = null
    private var subText: TextView? = null
    private var currDisc: BoardPiece = BoardPiece.WHITE
    private var tbLayout: TableLayout? = null

    lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this)
        subText = findViewById(R.id.welcomeSubtext)
        currTurn = findViewById(R.id.currTurn)
        adView = findViewById(R.id.theAd)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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
                if (input.text.toString().toInt() in 4..100 step 2) {
                    boardSize = input.text.toString()
                    subText?.text = ""
                    // Dynamically creates board on screen based on user input
                    // TODO: Add listeners to each of the views added
                    tbLayout = findViewById(R.id.tableLayout)
                    tbLayout?.removeAllViewsInLayout()
                    val board = boardSize?.toInt()
                    for (i in 0 until board!!) {
                        val tr = TableRow(this)
                        var count = 0
                        for (j in 0 until board) {
                            if (i == ((board / 2) -1) && j == (board / 2) - 1 || (i == board / 2) && j == board / 2)
                                tr.addView(createNewImage(R.drawable.black_game_piece, i.toString() + j.toString(), "BLACK"))
                            else if (((i == board / 2) && j == (board / 2) - 1 || i == (board / 2) - 1 && j == board / 2))
                                tr.addView(createNewImage(R.drawable.white_game_piece, i.toString() + j.toString(), "WHITE"))
                            else
                                tr.addView(createNewImage(R.drawable.blank_grid_tile, i.toString() + j.toString(), "EMPTY"))
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
        return View.OnClickListener{
            val currId = view.id.toString()
            placeDisc(currId)
        }
    }

    // TODO: Need to implement
    @SuppressLint("SetTextI18n")
    private fun placeDisc(imgID: String) {
        val board = boardSize?.toInt()
        val row: Int
        val col: Int
        val imgBlack = R.drawable.black_game_piece
        val imgWhite = R.drawable.white_game_piece
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        if (!isValidMove(imgID))
            return

        if (tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())?.tag ==
                BoardPiece.EMPTY.toString()) {
            if (checkWest(imgID)) {
                var colPos = col - 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos >= 0) {
                    currImg = tbLayout?.findViewById((row.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && colPos >= 0) {

                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    colPos--
                    currImg = tbLayout?.findViewById((row.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkEast(imgID)) {
                var colPos = col + 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos < boardSize?.toInt()!!) {
                    currImg = tbLayout?.findViewById((row.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && colPos < board!!) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    colPos++
                    currImg = tbLayout?.findViewById((row.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkSouth(imgID)) {
                var rowPos = row + 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (rowPos < boardSize?.toInt()!!) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + col.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos >= 0) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos++
                    currImg = tbLayout?.findViewById((rowPos.toString() + col.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkNorth(imgID)) {
                var rowPos = row - 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (rowPos >= 0) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + col.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos < board!!) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos--
                    currImg = tbLayout?.findViewById((rowPos.toString() + col.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkNortheast(imgID)) {
                var rowPos = row - 1
                var colPos = col + 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos < boardSize?.toInt()!! && rowPos >= 0) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos >= 0 && colPos < board!!) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos--
                    colPos++
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkSouthwest(imgID)) {
                var rowPos = row + 1
                var colPos = col - 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos >= 0 && rowPos < boardSize?.toInt()!!) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos < board!! && colPos >= 0) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos++
                    colPos--
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkSoutheast(imgID)) {
                var rowPos = row + 1
                var colPos = col + 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos < boardSize?.toInt()!! && rowPos < boardSize?.toInt()!!) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos < board!! && colPos < board) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos++
                    colPos++
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            if (checkNorthwest(imgID)) {
                var rowPos = row - 1
                var colPos = col - 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos >= 0 && rowPos >= 0) {
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                while (currTag != currDisc && currTag != BoardPiece.EMPTY && rowPos < board!! && colPos >= 0) {
                    currImg?.tag = currDisc.toString()
                    if (currDisc == BoardPiece.BLACK)
                        currImg?.setImageResource(imgBlack)
                    else
                        currImg?.setImageResource(imgWhite)
                    rowPos--
                    colPos--
                    currImg = tbLayout?.findViewById((rowPos.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
            }

            val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())

            if (currDisc == BoardPiece.BLACK && BoardPiece.valueOf(currImg?.tag.toString()) == BoardPiece.EMPTY)
                currImg?.setImageResource(imgBlack)
            else if (currDisc == BoardPiece.WHITE && BoardPiece.valueOf(currImg?.tag.toString()) == BoardPiece.EMPTY)
                currImg?.setImageResource(imgWhite)

            currImg?.tag = currDisc.toString()

            if (!isGameOver()) {
                val disc = currDisc
                if (currDisc == BoardPiece.WHITE)
                    currDisc = BoardPiece.BLACK
                else
                    currDisc = BoardPiece.WHITE
                if (!isValidMoveAvailableForDisc()) {
                    prepareNextTurn()
                    return
                }
                currDisc = disc
                prepareNextTurn()
            } else
                subText?.text = checkWinner().toString() + " is the winner!"
        }
    }

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
    private fun isValidMoveAvailableForDisc(): Boolean {
        val board = boardSize?.toInt()
        for (row in 0 until board!!) {
            for (col in 0 until board) {
                val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())
                val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
                val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
                val currTag = BoardPiece.valueOf(currImg?.tag.toString())
                if (currTag == BoardPiece.BLACK)
                    numBlack++
                if (currTag == BoardPiece.WHITE)
                    numWhite++
            }
        }

        return if (numBlack > numWhite)
            BoardPiece.BLACK
        else if (numWhite > numBlack)
            BoardPiece.WHITE
        else
            BoardPiece.TIE
    }

    // TODO: Need to test
    @SuppressLint("SetTextI18n")
    private fun prepareNextTurn() {
        currDisc = if (currDisc == BoardPiece.WHITE)
            BoardPiece.BLACK
        else
            BoardPiece.WHITE

        currTurn?.text = "Current Player: $currDisc"
    }



    // TODO: This is where you left off
    private fun checkNorth(imgID: String): Boolean {
        var numDisc = 0
        var numNotDisc = 0
        var numEmpty = 0
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        var currImg: ImageView?
        if (imgID.first() == '0' || imgID.length < 2) {
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
            currImg = if (row == 0) {
                tbLayout?.findViewById(i.toString().toInt())
            } else {
                tbLayout?.findViewById((row.toString() + i.toString()).toInt())
            }
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
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
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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
        val row: Int
        val col: Int
        if (imgID.first() == '0' || imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10
        }
        var j = col - 1

        for (i in (row - 1) downTo 0) {
            if (i < 0 || j < 0)
                break
            val currImg = tbLayout?.findViewById<ImageView>((i.toString() + j.toString()).toInt())
            val currTag = BoardPiece.valueOf(currImg?.tag.toString())
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