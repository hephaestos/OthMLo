package com.example.othmlo

import android.app.ActionBar
import android.content.Context
import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    var boardSize: String? = null
    var chipButton: Button? = null
    var subText: TextView? = null

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
                    val tbLayout = findViewById<TableLayout>(R.id.tableLayout)
                    val board = boardSize?.toInt()
                    for (i in 0 until board!!) {
                        val tr = TableRow(this)
                        var count = 0
                        for (j in 0 until board) {
                            if (i == ((board / 2) -1) && j == (board / 2) - 1 || (i == board / 2) && j == board / 2)
                                tr.addView(createNewImage(R.drawable.black_piece))
                            else if (((i == board / 2) && j == (board / 2) - 1 || i == (board / 2) - 1 && j == board / 2))
                                tr.addView(createNewImage(R.drawable.white_piece))
                            else
                                tr.addView(createNewImage(R.drawable.grid_blank))
                            count++
                        }
                        tr.gravity = Gravity.CENTER
                        tbLayout.addView(tr)
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

    private fun createNewImage(@DrawableRes resID: Int): ImageView {
        val newImg = ImageView(this)
        newImg.id = ViewCompat.generateViewId()
        newImg.setImageResource(resID)
        return newImg
    }
}