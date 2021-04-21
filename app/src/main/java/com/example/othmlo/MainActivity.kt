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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.textfield.TextInputLayout

// const val AD_UNIT_ID = "ca-app-pub-8621865123944893/4945704105" // This is the real AD_UNIT_ID
const val AD_UNIT_ID = "ca-app-pub-3940256099942544/8691691433"

/**
 * Driver activity for the Othello app. Allows users to play a game of Othello with dynamic
 * board sizes.
 * @author Brandon Thomas & Daniel Floyd
 * @version 1.0.0
 */
class MainActivity : AppCompatActivity() {
    private var boardSize: String? = null // Size of board selected by user
    private var currTurn: TextView? = null // Which color currently needs to place disc
    private var subText: TextView? = null // Text underneath game board
    private var currDisc: BoardPiece = BoardPiece.WHITE // Current player's disc color
    private var tbLayout: TableLayout? = null // Layout managing game board
    private var completedGameCount: Int = 0 // Number of games completed. Ties in with ads
    private var othInterstitialAd: InterstitialAd? = null // Ad
    private var MAINTAG = "MainActivity"
    private var boardMenu: MenuItem? = null // Reference to menu item, to change text when needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subText = findViewById(R.id.welcomeSubtext)
        currTurn = findViewById(R.id.currTurn)

        MobileAds.initialize(this) {} // Start up mobile ad service
        loadAd()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        boardMenu = menu?.findItem(R.id.menu_board_size) // Get our reference to the menu item
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_board_size) {
            createBoardDialog(this) // Allow user to select board size
            return true
        }
        return false
    }

    /**
     * Function to load in an ad using Google's Mobile Ad SDK
     */
    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this, AD_UNIT_ID, adRequest, object: InterstitialAdLoadCallback() {
            /**
             * If ad fails to load, set interstitial ad to null
             */
            override fun onAdFailedToLoad(adError: LoadAdError) {
                    othInterstitialAd = null
                    Log.d(MAINTAG, adError.message)
                }

            /**
             * Otherwise, set the variable accordingly with the loaded ad
             */
            override fun onAdLoaded(intAd: InterstitialAd) {
                    othInterstitialAd = intAd
                    Log.d(MAINTAG, "Ad was loaded")
                }
            }
        )
    }

    /**
     * Helper function to actually display the add when relevant later on in the program
     */
    private fun showInterstitialAd() {
        if (othInterstitialAd != null) {
            othInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(MAINTAG, "Ad was dismissed")
                    othInterstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.d(MAINTAG, error.message)
                    othInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(MAINTAG, "Ad was shown")
                }
            }
            othInterstitialAd?.show(this) // Display the fullscreen ad
        }
    }

    /**
     * Helper function to prompt user to enter an Othello board size. Input must be an even integer
     * and fall within a certain range in order to create/fill the initial board
     * @param context Parent view where dialog box will be displayed
     */
    private fun createBoardDialog(context: Context) {
        val textInputLayout = TextInputLayout(context)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER // Keyboard shown should only be numbers
        textInputLayout.addView(input)

        val builder = AlertDialog.Builder(context)
            .setTitle("Enter Board Size")
            .setView(textInputLayout)
            .setMessage("Enter an even integer 4-10")
            .setPositiveButton("OK") { dialog, _ ->
                // If user input is valid, go ahead with creating the Othello board
                if (input.text.toString().toInt() in 4..10 step 2) {
                    boardSize = input.text.toString()
                    subText?.text = ""
                    // Dynamically creates board on screen based on user input
                    tbLayout = findViewById(R.id.tableLayout)
                    // Make sure old board is cleared out (if present)
                    tbLayout?.removeAllViewsInLayout()
                    val board = boardSize?.toInt()
                    // Iterate through tablerows and create images based on starting board size
                    for (i in 0 until board!!) {
                        val tr = TableRow(this)
                        for (j in (0 until board)) {
                            // Two of middle pieces should be black to start game
                            if (i == ((board / 2) -1) && j == (board / 2) - 1 || (i == board / 2) && j == board / 2)
                                tr.addView(createNewImage(R.drawable.black_piece, i.toString() + j.toString(), "BLACK"))
                            // Two of middle pieces should also be white to start game
                            else if (((i == board / 2) && j == (board / 2) - 1 || i == (board / 2) - 1 && j == board / 2))
                                tr.addView(createNewImage(R.drawable.white_piece, i.toString() + j.toString(), "WHITE"))
                            // Everything else is empty board pieces/images to start
                            else
                                tr.addView(createNewImage(R.drawable.grid_blank, i.toString() + j.toString(), "EMPTY"))
                        }
                        tr.gravity = Gravity.CENTER // Center all items in their tablerows
                        tbLayout?.addView(tr) // Add new row to table layout
                    }
                    dialog.cancel()
                    boardMenu?.title = "Board Size"
                }
            }
            .setNegativeButton("Cancel") {dialog, _ ->
                dialog.cancel()
            }.create()
        builder.show()
    }

    /**
     * Helper function to create new image assets when board is initialized
     * @param resID The resource ID for the new image view (Options are White, Black, or Empty)
     * @param imgID The unique ID to be given to this image in format ## where first # is row
     * and second # is column
     * @param tag String to easily determine if image is black, white, or empty board piece
     * @return ImageView the view created by this helper function
     */
    private fun createNewImage(@DrawableRes resID: Int, imgID: String, tag: String): ImageView {
        val newImg = ImageView(this)
        newImg.id = imgID.toInt()
        newImg.setImageResource(resID) // Set appropriate image
        newImg.tag = tag
        newImg.setOnClickListener(createImgListener(newImg)) // Allow image to be tapped by user
        return newImg
    }

    /**
     * Helper function to set functionality of click listener for the board images/pieces
     * @param view The ImageView which is having the listener added
     * @return View.OnClickListener The listener to be assigned to the given imageView
     */
    private fun createImgListener(view: ImageView): View.OnClickListener {
        return View.OnClickListener{
            val currId = view.id.toString()
            placeDisc(currId) // When image is tapped, start place disc logic for that location
        }
    }

    /**
     * Main logic of Othello game which decides if/how disk is placed on the current board
     * @param inmgID The unique ID of the image/board piece which was tapped
     */
    @SuppressLint("SetTextI18n")
    private fun placeDisc(imgID: String) {
        val board = boardSize?.toInt()
        val row: Int
        val col: Int
        val imgBlack = R.drawable.black_piece
        val imgWhite = R.drawable.white_piece
        // If the ID this function receives is only single digit, we know the row is 0
        // This is necessary due to converting strings beginning with 0 into integers
        if (imgID.length < 2) {
            row = 0
            col = imgID.toInt()
        } else {
            row = firstDigit(imgID.toInt())
            col = imgID.toInt() % 10 // Grab last digit of imgID, which is the column
        }
        // First check if current tapped image is even a valid move, if not, do nothing
        if (!isValidMove(imgID))
            return

        // Check if tapped image is an EMPTY board piece first
        if (tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())?.tag ==
                BoardPiece.EMPTY.toString()) {
            // The rest of these if statements simply directionally check if a disc will "take"
            // the opposing player's discs, and if so, replaces them with current player's color
            if (checkWest(imgID)) {
                var colPos = col - 1
                var currImg: ImageView? = null
                var currTag: BoardPiece? = null
                if (colPos >= 0) {
                    currImg = tbLayout?.findViewById((row.toString() + colPos.toString()).toInt())
                    currTag = BoardPiece.valueOf(currImg?.tag.toString())
                }
                // While we have not encountered an EMPTY position, continue replacing discs
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

            // Logic below is to set the image of the position where the user actually tapped
            // to their board piece color
            val currImg = tbLayout?.findViewById<ImageView>((row.toString() + col.toString()).toInt())

            if (currDisc == BoardPiece.BLACK && BoardPiece.valueOf(currImg?.tag.toString()) == BoardPiece.EMPTY)
                currImg?.setImageResource(imgBlack)
            else if (currDisc == BoardPiece.WHITE && BoardPiece.valueOf(currImg?.tag.toString()) == BoardPiece.EMPTY)
                currImg?.setImageResource(imgWhite)

            currImg?.tag = currDisc.toString()

            // Check if the game has ended after a player places a disc
            if (!isGameOver()) {
                val disc = currDisc
                currDisc = if (currDisc == BoardPiece.WHITE)
                    BoardPiece.BLACK
                else
                    BoardPiece.WHITE
                // Ensures next player's turn is not given if they do not have any valid moves
                if (!isValidMoveAvailableForDisc()) {
                    prepareNextTurn()
                    return
                }
                currDisc = disc
                prepareNextTurn()
            } else {
                // If the game is over and players have completed increment of two games, play ad
                if (++completedGameCount % 2 == 0)
                    showInterstitialAd()
                // Logic to display winner of game in subtext field
                if (checkWinner().toString() == "TIE")
                    subText?.text = "It's a TIE!"
                else
                    subText?.text = checkWinner().toString() + " is the winner!"
                boardMenu?.title = "Start New Game"
            }
        }
    }

    /**
     * Helper function to determine if there is a valid move for the given image/location
     * @param imgID The given image/location to check for validity
     * @return The output, either true or false, of the isValidMoveForDisc function
     */
    private fun isValidMove(imgID: String): Boolean {
        return isValidMoveForDisc(imgID)
    }

    /**
     * Checks all of the cardinal directions to see if there is a valid move available to play
     * the current color disc at the selected location
     * @param imgID the image/location where the disc will be hypothetically placed
     * @return true if there is a valid move, false otherwise
     */
    private fun isValidMoveForDisc(imgID: String): Boolean {
        if (checkNorth(imgID) || checkSouth(imgID) || checkEast(imgID) || checkWest(imgID) ||
                checkNortheast(imgID) || checkNorthwest(imgID) || checkSoutheast(imgID) ||
                    checkSouthwest(imgID))
                        return true
        return false
    }

    /**
     * Checks to see if there are any valid moves for the current player anywhere on the board.
     * Function is used elsewhere to determine if player will be given a turn or if the game is over
     * @return Boolean true if there is a valid move somewhere on the board, false otherwise
     */
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

    /**
     * Simple function to determine if the board is currently full. Simply checks to see if any
     * of the image assets on the board have a tag as "EMPTY"
     * @return Boolean true if the current board has no EMPTY places, otherwise false
     */
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

    /**
     * Checks to see if either the board is already full, or if neither player is currently
     * capable of placing a disc at a valid location
     * @return true if neither player can make a move or if the board is full, false otherwise
     */
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

    /**
     * Checks to see who the winner of the current board is if the game is over. If there are more
     * black than white pieces, black wins and vice versa, otherwise it is a tie.
     * @return The BoardPiece (either BLACK, WHITE, or TIE) which is the winner of the game
     */
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

        return when {
            numBlack > numWhite -> BoardPiece.BLACK
            numWhite > numBlack -> BoardPiece.WHITE
            else -> BoardPiece.TIE
        }
    }

    /**
     * Simply changes the current disc value and sets the subtext of the board to the correct
     * player
     */
    @SuppressLint("SetTextI18n")
    private fun prepareNextTurn() {
        currDisc = if (currDisc == BoardPiece.WHITE)
            BoardPiece.BLACK
        else
            BoardPiece.WHITE

        currTurn?.text = "Current Player: $currDisc"
    }

    /**
     * Helper function to see if discs can be captured in the North direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the South direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the East direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the West direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the Northeast direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the Southwest direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the Southeast direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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

    /**
     * Helper function to see if discs can be captured in the Northwest direction.
     * @param imdID The image/board location which is being checked
     * @return true if pieces can be captured in this direction, otherwise false
     */
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
     * Pulls the first digit out from an integer
     * @param a The integer value which is being evaluated
     * @return The first integer value of parameter a
     */
    private fun firstDigit(a: Int): Int {
        var num = a
        while (num > 9)
            num /= 10

        return num
    }
}