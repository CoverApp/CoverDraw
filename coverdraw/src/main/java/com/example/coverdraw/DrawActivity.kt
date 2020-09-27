package com.example.coverdraw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coverdraw.view.DrawView
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream


class DrawActivity : AppCompatActivity() {

    private lateinit var drawView: DrawView
    private lateinit var currentAlertDialog: AlertDialog.Builder
    private lateinit var  dialogLineWidth: AlertDialog
    private var defaultColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawView = findViewById(R.id.drawView)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.clearId -> drawView.clear()
            R.id.saveId -> drawView.saveToInternalStorage()
            R.id.colorId -> openColourPicker()
            R.id.lineStrokeId -> showLineWidthDialog()
            R.id.undoId -> drawView.undo()
            R.id.redoId -> drawView.redo()
            else -> Log.e("ERROR", "Error Occured")
        }

        return super.onOptionsItemSelected(item)
    }

    fun showLineWidthDialog(){
        currentAlertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.stroke_width_dialog, null)
        val widthSeekBar = view.findViewById<SeekBar>(R.id.strokeWidthId)
        val setLineWidthButton = view.findViewById<Button>(R.id.widthDialogButton)
        val widthImageView = view.findViewById<ImageView>(R.id.editStrokeImageId)
        setLineWidthButton.setOnClickListener {
            drawView.setLineWidth(widthSeekBar.progress)
            dialogLineWidth.dismiss()
            //currentAlertDialog
        }
        widthSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            val bm = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                
                val p = Paint()
                p.color = drawView.getDrawingColor()
                p.strokeCap = Paint.Cap.ROUND
                p.strokeWidth = progress.toFloat()

                bm.eraseColor(Color.WHITE)
                canvas.drawLine(30F,50F, 370F, 50F, p)
                widthImageView.setImageBitmap(bm)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // called when tracking the seekbar is started
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // called when tracking the seekbar is stopped
            }
        })
        widthSeekBar.progress = drawView.getLineWidth().toInt()
        currentAlertDialog.setView(view)
        dialogLineWidth = currentAlertDialog.create()
        dialogLineWidth.setTitle("Set Line Width")
        dialogLineWidth.show()
    }

    private fun openColourPicker() {
        val ambilWarnaDialog =
            AmbilWarnaDialog(this, defaultColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {
                    Toast.makeText(this@DrawActivity, "Unavailable", Toast.LENGTH_LONG).show()
                }

                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    defaultColor = color
                    drawView.setDrawingColor(color)
                }
            })
        ambilWarnaDialog.show() // add
    }

    private fun saveAsBitmapIntent(){
        val bStream = ByteArrayOutputStream()
        val bitmapSave = drawView.bitmap
        bitmapSave.compress(Bitmap.CompressFormat.PNG, 100, bStream)
        val byteArray = bStream.toByteArray()
        val returnIntent = Intent()
        returnIntent.putExtra("cover_bitmap", byteArray)
        setResult(Activity.RESULT_OK,returnIntent)
        finish()
    }
}