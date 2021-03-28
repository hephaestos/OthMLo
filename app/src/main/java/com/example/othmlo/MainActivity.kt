package com.example.othmlo

import android.app.ActionBar
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.TableRow.LayoutParams
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    var boardSize: String? = null
    var centerText: TextView? = null
    var chipButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chipButton = findViewById(R.id.place_chip)
        chipButton?.setOnClickListener { _ ->
            val gridPiece = findViewById<ImageView>(R.id.imageView4)
            gridPiece.setImageResource(R.drawable.black_piece)
        }
        val img_3 = findViewById<ImageView>(R.id.imageView3)
        img_3.setOnClickListener { _ ->
            img_3.setImageResource(R.drawable.white_piece)
        }

        // TODO: Expand on this to dynamically add elements to tableLayout. Currently adds in elements but need layout params and listeners
//        var tbLayout = findViewById<TableLayout>(R.id.tableLayout)
//        var tr = TableRow(this)
//        var img0 = ImageView(this)
//        img0.setImageResource(R.drawable.black_piece)
//        var img1 = ImageView(this)
//        img1.setImageResource(R.drawable.black_piece)
//        var img2 = ImageView(this)
//        img2.setImageResource(R.drawable.black_piece)
//        var img3 = ImageView(this)
//        img3.setImageResource(R.drawable.black_piece)
//        tr.addView(img0)
//        tr.addView(img1)
//        tr.addView(img2)
//        tr.addView(img3)
//        tbLayout.addView(tr)
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
            .setMessage("Enter an integer 3-10")
            .setPositiveButton("OK") { dialog, _ ->
                if (input.text.toString().toInt() in 3..10) {
                    boardSize = input.text.toString()
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
}